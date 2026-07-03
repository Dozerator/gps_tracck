package com.example.operator.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.operator.data.local.entity.PendingPointEntity
import com.example.operator.data.local.entity.TrackSummary
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

    /** Синхронизированные точки старше окна хранения (см. SyncWorker) больше не нужны локально. */
    @Query("DELETE FROM pending_points WHERE status = 'SYNCED' AND createdAt < :olderThan")
    suspend fun deleteSyncedOlderThan(olderThan: Long)

    /** Последняя известная точка пользователя по типу объекта — для решения, продолжается ли трек. */
    @Query(
        """
        SELECT * FROM pending_points
        WHERE userId = :userId AND objectType = :objectType
        ORDER BY timestamp DESC LIMIT 1
        """
    )
    suspend fun getLastPoint(userId: String, objectType: String): PendingPointEntity?

    /** Все точки одного трека, по времени. */
    @Query("SELECT * FROM pending_points WHERE trackId = :trackId ORDER BY timestamp ASC")
    fun getTrackPoints(trackId: String): Flow<List<PendingPointEntity>>

    /** История за смену — все локально известные точки не старше [since]. */
    @Query("SELECT * FROM pending_points WHERE createdAt > :since ORDER BY createdAt DESC")
    fun getShiftHistory(since: Long): Flow<List<PendingPointEntity>>

    /**
     * Сводка по каждому треку. threatLevel — уровень САМОЙ серьёзной точки трека, а не
     * произвольной строки группы: агрегатный запрос с "голой" колонкой рядом с несколькими
     * MIN()/MAX() даёт неопределённый результат в SQLite (правило "bare column берёт
     * значение из строки агрегата" действует только при РОВНО одном MIN/MAX в запросе).
     * Коррелированный подзапрос с ORDER BY по приоритету угрозы — однозначен.
     */
    @Query(
        """
        SELECT
            trackId AS trackId,
            objectType AS objectType,
            (SELECT p2.threatLevel FROM pending_points p2
             WHERE p2.trackId = p1.trackId
             ORDER BY CASE p2.threatLevel
                 WHEN 'THREAT' THEN 3
                 WHEN 'ATTENTION' THEN 2
                 WHEN 'OBSERVATION' THEN 1
                 ELSE 0
             END DESC
             LIMIT 1) AS threatLevel,
            COUNT(*) AS pointCount,
            MIN(timestamp) AS startTime,
            MAX(timestamp) AS endTime
        FROM pending_points p1
        WHERE trackId IS NOT NULL
        GROUP BY trackId
        ORDER BY startTime DESC
        """
    )
    fun getAllTracks(): Flow<List<TrackSummary>>
}
