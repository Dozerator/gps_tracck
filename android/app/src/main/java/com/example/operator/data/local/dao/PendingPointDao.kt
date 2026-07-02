package com.example.operator.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.operator.data.local.entity.PendingPointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingPointDao {

    @Insert
    suspend fun insert(point: PendingPointEntity): Long

    @Query("SELECT * FROM pending_points WHERE status = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingPoints(): List<PendingPointEntity>

    @Query("SELECT COUNT(*) FROM pending_points WHERE status = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT * FROM pending_points ORDER BY createdAt DESC")
    fun getAllPoints(): Flow<List<PendingPointEntity>>

    @Query("UPDATE pending_points SET status = 'SYNCED' WHERE id = :id")
    suspend fun markAsSynced(id: Long)

    /**
     * Фиксирует неудачную попытку отправки. Пока число попыток меньше [maxAttempts],
     * точка остаётся PENDING и будет подхвачена следующим прогоном воркера; после
     * исчерпания попыток переходит в терминальный статус FAILED и больше не
     * выбирается автоматически (пока не будет сброшена через [resetFailedToRetry]).
     */
    @Query(
        """
        UPDATE pending_points
        SET syncAttempts = syncAttempts + 1,
            lastSyncAttempt = :time,
            status = CASE WHEN syncAttempts + 1 >= :maxAttempts THEN 'FAILED' ELSE 'PENDING' END
        WHERE id = :id
        """
    )
    suspend fun markAttemptFailed(id: Long, time: Long = System.currentTimeMillis(), maxAttempts: Int = 5)

    /** Ручной повтор для точек, исчерпавших лимит попыток (например, из экрана очереди). */
    @Query("UPDATE pending_points SET status = 'PENDING', syncAttempts = 0 WHERE status = 'FAILED'")
    suspend fun resetFailedToRetry()

    /** Синхронизированные точки старше часа больше не нужны локально. */
    @Query("DELETE FROM pending_points WHERE status = 'SYNCED' AND createdAt < :olderThan")
    suspend fun deleteSyncedOlderThan(olderThan: Long)
}
