package com.example.operator.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Статусы точки в локальной очереди отправки. */
object PointStatus {
    const val PENDING = "PENDING"
    const val SYNCED = "SYNCED"
    const val FAILED = "FAILED"
}

@Entity(tableName = "pending_points")
data class PendingPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val lat: Double,
    val lon: Double,
    val accuracy: Float,
    val userId: String,
    val timestamp: Long,
    val objectType: String,      // "UAV" или "QUAD"
    val directionDegrees: Int,   // 0..359, 0 = север, по часовой стрелке
    val directionLabel: String,  // "СЕВЕРО-ВОСТОК (47°)"
    val threatLevel: String,     // "OBSERVATION","ATTENTION","THREAT"
    val status: String = PointStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val syncAttempts: Int = 0,
    val lastSyncAttempt: Long? = null,
    val trackId: String? = null  // группировка в трек — см. utils/TrackManager.kt
)
