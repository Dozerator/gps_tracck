package com.example.operator.data.repository

import android.content.Context
import com.example.operator.auth.AuthManager
import com.example.operator.data.local.dao.PendingPointDao
import com.example.operator.data.local.entity.PendingPointEntity
import com.example.operator.model.Direction
import com.example.operator.model.LocationPointRequest
import com.example.operator.model.ObjectType
import com.example.operator.model.ThreatLevel
import com.example.operator.network.ApiService
import com.example.operator.utils.NetworkMonitor
import com.example.operator.utils.isoUtc
import com.example.operator.workers.SyncWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

sealed class SendResult {
    object SentOnline : SendResult()
    object SavedOffline : SendResult()
    data class Error(val message: String) : SendResult()
}

class LocationRepository(
    private val dao: PendingPointDao,
    private val apiService: ApiService,
    private val authManager: AuthManager,
    private val networkMonitor: NetworkMonitor,
    private val context: Context
) {
    val pendingCount: Flow<Int> = dao.getPendingCount()
    val allPoints: Flow<List<PendingPointEntity>> = dao.getAllPoints()
    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    suspend fun sendPoint(
        lat: Double,
        lon: Double,
        accuracy: Float,
        timestampMillis: Long,
        objectType: ObjectType,
        direction: Direction,
        threatLevel: ThreatLevel = ThreatLevel.OBSERVATION
    ): SendResult {
        val token = authManager.getToken()

        if (networkMonitor.isOnline.value && token != null) {
            return try {
                val response = apiService.sendLocationPoint(
                    "Bearer $token",
                    LocationPointRequest(
                        lat = lat,
                        lon = lon,
                        accuracy = accuracy,
                        timestamp = isoUtc(timestampMillis),
                        objectType = objectType.apiValue,
                        direction = direction.apiValue,
                        threatLevel = threatLevel.apiValue
                    )
                )
                if (response.isSuccessful) {
                    SendResult.SentOnline
                } else {
                    // Сервер ответил ошибкой — не теряем точку, кладём в очередь.
                    saveToQueue(lat, lon, accuracy, timestampMillis, objectType, direction, threatLevel)
                    SendResult.SavedOffline
                }
            } catch (e: Exception) {
                // Сеть считалась доступной, но запрос не прошёл — тоже в очередь.
                saveToQueue(lat, lon, accuracy, timestampMillis, objectType, direction, threatLevel)
                SendResult.SavedOffline
            }
        }

        saveToQueue(lat, lon, accuracy, timestampMillis, objectType, direction, threatLevel)
        return SendResult.SavedOffline
    }

    private suspend fun saveToQueue(
        lat: Double,
        lon: Double,
        accuracy: Float,
        timestampMillis: Long,
        objectType: ObjectType,
        direction: Direction,
        threatLevel: ThreatLevel
    ): Long {
        return dao.insert(
            PendingPointEntity(
                lat = lat,
                lon = lon,
                accuracy = accuracy,
                userId = authManager.getUserLogin() ?: "",
                timestamp = timestampMillis,
                objectType = objectType.apiValue,
                direction = direction.apiValue,
                threatLevel = threatLevel.apiValue
            )
        )
    }

    /** Повторить точки, ранее исчерпавшие лимит попыток, и сразу запустить синхронизацию. */
    suspend fun retryFailed() {
        dao.resetFailedToRetry()
        triggerSync()
    }

    fun triggerSync() {
        SyncWorker.schedule(context)
    }
}
