package com.example.operator.data.local.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\r\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\bH\'J\u000e\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\f0\bH\'J\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\n0\tH\u00a7@\u00a2\u0006\u0002\u0010\u000eJ\u0016\u0010\u000f\u001a\u00020\u00052\u0006\u0010\u0010\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\u0011J\u0016\u0010\u0012\u001a\u00020\u00032\u0006\u0010\u0013\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J*\u0010\u0014\u001a\u00020\u00032\u0006\u0010\u0013\u001a\u00020\u00052\b\b\u0002\u0010\u0015\u001a\u00020\u00052\b\b\u0002\u0010\u0016\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\u0017J\u000e\u0010\u0018\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u000e\u00a8\u0006\u0019"}, d2 = {"Lcom/example/operator/data/local/dao/PendingPointDao;", "", "deleteSyncedOlderThan", "", "olderThan", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllPoints", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/example/operator/data/local/entity/PendingPointEntity;", "getPendingCount", "", "getPendingPoints", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insert", "point", "(Lcom/example/operator/data/local/entity/PendingPointEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "markAsSynced", "id", "markAttemptFailed", "time", "maxAttempts", "(JJILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "resetFailedToRetry", "app_debug"})
@androidx.room.Dao()
public abstract interface PendingPointDao {
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    com.example.operator.data.local.entity.PendingPointEntity point, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM pending_points WHERE status = \'PENDING\' ORDER BY createdAt ASC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getPendingPoints(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.example.operator.data.local.entity.PendingPointEntity>> $completion);
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM pending_points WHERE status = \'PENDING\'")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.lang.Integer> getPendingCount();
    
    @androidx.room.Query(value = "SELECT * FROM pending_points ORDER BY createdAt DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.example.operator.data.local.entity.PendingPointEntity>> getAllPoints();
    
    @androidx.room.Query(value = "UPDATE pending_points SET status = \'SYNCED\' WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object markAsSynced(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * Фиксирует неудачную попытку отправки. Пока число попыток меньше [maxAttempts],
     * точка остаётся PENDING и будет подхвачена следующим прогоном воркера; после
     * исчерпания попыток переходит в терминальный статус FAILED и больше не
     * выбирается автоматически (пока не будет сброшена через [resetFailedToRetry]).
     */
    @androidx.room.Query(value = "\n        UPDATE pending_points\n        SET syncAttempts = syncAttempts + 1,\n            lastSyncAttempt = :time,\n            status = CASE WHEN syncAttempts + 1 >= :maxAttempts THEN \'FAILED\' ELSE \'PENDING\' END\n        WHERE id = :id\n        ")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object markAttemptFailed(long id, long time, int maxAttempts, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * Ручной повтор для точек, исчерпавших лимит попыток (например, из экрана очереди).
     */
    @androidx.room.Query(value = "UPDATE pending_points SET status = \'PENDING\', syncAttempts = 0 WHERE status = \'FAILED\'")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object resetFailedToRetry(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * Синхронизированные точки старше часа больше не нужны локально.
     */
    @androidx.room.Query(value = "DELETE FROM pending_points WHERE status = \'SYNCED\' AND createdAt < :olderThan")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteSyncedOlderThan(long olderThan, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}