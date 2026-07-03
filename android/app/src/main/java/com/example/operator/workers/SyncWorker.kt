package com.example.operator.workers

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.operator.OperatorApp
import com.example.operator.auth.AuthManager
import com.example.operator.data.local.AppDatabase
import com.example.operator.data.local.entity.PendingPointEntity
import com.example.operator.model.LocationPointRequest
import com.example.operator.utils.isoUtc
import java.util.concurrent.TimeUnit

/**
 * Синхронизирует локальную очередь точек с сервером при появлении сети.
 * Точка остаётся PENDING и повторяется в следующих прогонах, пока не будет
 * достигнут [MAX_SYNC_ATTEMPTS] — тогда она переходит в терминальный статус FAILED.
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val dao = AppDatabase.getInstance(context).pendingPointDao()
    private val authManager = AuthManager(context)
    private val apiService = (context.applicationContext as OperatorApp).retrofitClient.apiService

    override suspend fun doWork(): Result {
        val token = authManager.getToken() ?: return Result.retry()
        val pendingPoints = dao.getPendingPoints()

        if (pendingPoints.isEmpty()) {
            cleanupOldSynced()
            return Result.success()
        }

        var hasRetryableFailures = false

        pendingPoints.forEach { point ->
            try {
                val response = apiService.sendLocationPoint(
                    "Bearer $token",
                    point.toRequest()
                )
                if (response.isSuccessful) {
                    dao.markAsSynced(point.id)
                } else {
                    dao.markAttemptFailed(point.id, maxAttempts = MAX_SYNC_ATTEMPTS)
                    if (point.syncAttempts + 1 < MAX_SYNC_ATTEMPTS) hasRetryableFailures = true
                }
            } catch (e: Exception) {
                dao.markAttemptFailed(point.id, maxAttempts = MAX_SYNC_ATTEMPTS)
                if (point.syncAttempts + 1 < MAX_SYNC_ATTEMPTS) hasRetryableFailures = true
            }
        }

        cleanupOldSynced()

        return if (hasRetryableFailures) Result.retry() else Result.success()
    }

    private suspend fun cleanupOldSynced() {
        dao.deleteSyncedOlderThan(System.currentTimeMillis() - SYNCED_RETENTION_MS)
    }

    companion object {
        const val WORK_NAME = "sync_pending_points"
        const val MAX_SYNC_ATTEMPTS = 5

        // Локальная таблица теперь служит и историей за смену (экран "История"), а не
        // только очередью на отправку — поэтому окно хранения увеличено с 1 часа до 12,
        // чтобы синхронизированные точки не исчезали раньше, чем закончится смена.
        private const val SYNCED_RETENTION_MS = 12 * 60 * 60 * 1000L // 12 часов

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    15, TimeUnit.SECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}

fun PendingPointEntity.toRequest(): LocationPointRequest = LocationPointRequest(
    lat = lat,
    lon = lon,
    accuracy = accuracy,
    timestamp = isoUtc(timestamp),
    objectType = objectType,
    directionDegrees = directionDegrees,
    directionLabel = directionLabel,
    threatLevel = threatLevel,
    // Записи до этой фичи (миграция v2→v3) могли не иметь trackId — считаем каждую
    // такую точку отдельным треком из одного отчёта, а не блокируем синхронизацию.
    trackId = trackId ?: "${userId}_${objectType}_$timestamp"
)
