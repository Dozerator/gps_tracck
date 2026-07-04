package com.example.operator.data.repository

import android.content.Context
import com.example.operator.auth.AuthManager
import com.example.operator.data.local.dao.PendingPointDao
import com.example.operator.data.local.entity.PendingPointEntity
import com.example.operator.data.local.entity.PointStatus
import com.example.operator.data.local.entity.TrackSummary
import com.example.operator.model.LocationPointRequest
import com.example.operator.model.ObjectType
import com.example.operator.model.ThreatLevel
import com.example.operator.network.ApiService
import com.example.operator.utils.NetworkMonitor
import com.example.operator.utils.TrackManager
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

    /** История точек за смену (по умолчанию — последние 12 часов), для экрана истории. */
    fun getShiftHistory(sinceMillis: Long): Flow<List<PendingPointEntity>> = dao.getShiftHistory(sinceMillis)

    fun getAllTracks(): Flow<List<TrackSummary>> = dao.getAllTracks()

    /** Очистить синхронизированную историю (экран истории), не трогая PENDING/FAILED. */
    suspend fun clearSyncedHistory() = dao.clearAllSynced()

    suspend fun sendPoint(
        lat: Double,
        lon: Double,
        accuracy: Float,
        timestampMillis: Long,
        objectType: ObjectType,
        directionDegrees: Int,
        directionLabel: String,
        threatLevel: ThreatLevel = ThreatLevel.OBSERVATION
    ): SendResult {
        val userId = authManager.getUserLogin().orEmpty()
        val lastPoint = dao.getLastPoint(userId, objectType.apiValue)
        val trackId = TrackManager.generateTrackId(userId, objectType.apiValue, timestampMillis, lastPoint)
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
                        directionDegrees = directionDegrees,
                        directionLabel = directionLabel,
                        threatLevel = threatLevel.apiValue,
                        trackId = trackId
                    )
                )
                if (response.isSuccessful) {
                    // Точка ушла на сервер, но всё равно сохраняем её локально (сразу как
                    // SYNCED) — иначе экран истории/треков на телефоне не видел бы половину
                    // отправленных отметок, а следующий вызов getLastPoint() не нашёл бы её
                    // для продолжения трека.
                    saveLocally(
                        lat, lon, accuracy, userId, timestampMillis, objectType,
                        directionDegrees, directionLabel, threatLevel, trackId, PointStatus.SYNCED
                    )
                    SendResult.SentOnline
                } else {
                    saveLocally(
                        lat, lon, accuracy, userId, timestampMillis, objectType,
                        directionDegrees, directionLabel, threatLevel, trackId, PointStatus.PENDING
                    )
                    SendResult.SavedOffline
                }
            } catch (e: Exception) {
                saveLocally(
                    lat, lon, accuracy, userId, timestampMillis, objectType,
                    directionDegrees, directionLabel, threatLevel, trackId, PointStatus.PENDING
                )
                SendResult.SavedOffline
            }
        }

        saveLocally(
            lat, lon, accuracy, userId, timestampMillis, objectType,
            directionDegrees, directionLabel, threatLevel, trackId, PointStatus.PENDING
        )
        return SendResult.SavedOffline
    }

    private suspend fun saveLocally(
        lat: Double,
        lon: Double,
        accuracy: Float,
        userId: String,
        timestampMillis: Long,
        objectType: ObjectType,
        directionDegrees: Int,
        directionLabel: String,
        threatLevel: ThreatLevel,
        trackId: String,
        status: String
    ): Long {
        return dao.insert(
            PendingPointEntity(
                lat = lat,
                lon = lon,
                accuracy = accuracy,
                userId = userId,
                timestamp = timestampMillis,
                objectType = objectType.apiValue,
                directionDegrees = directionDegrees,
                directionLabel = directionLabel,
                threatLevel = threatLevel.apiValue,
                status = status,
                trackId = trackId
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
