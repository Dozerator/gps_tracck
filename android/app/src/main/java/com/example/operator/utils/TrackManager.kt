package com.example.operator.utils

import com.example.operator.data.local.entity.PendingPointEntity

/**
 * Решает, продолжает ли новая точка существующий трек или начинает новый.
 * Трек = один и тот же пользователь + тип объекта + промежуток менее 10 минут
 * между соседними отметками.
 */
object TrackManager {
    private const val TRACK_WINDOW_MS = 10 * 60 * 1000L

    fun generateTrackId(
        userId: String,
        objectType: String,
        timestamp: Long,
        lastPoint: PendingPointEntity?
    ): String {
        return if (
            lastPoint != null &&
            lastPoint.userId == userId &&
            lastPoint.objectType == objectType &&
            (timestamp - lastPoint.timestamp) < TRACK_WINDOW_MS
        ) {
            // Продолжаем существующий трек.
            lastPoint.trackId ?: generateNewTrackId(userId, objectType)
        } else {
            generateNewTrackId(userId, objectType)
        }
    }

    private fun generateNewTrackId(userId: String, objectType: String): String {
        return "${userId}_${objectType}_${System.currentTimeMillis()}"
    }
}
