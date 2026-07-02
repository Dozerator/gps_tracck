package com.example.operator.workers;

/**
 * Синхронизирует локальную очередь точек с сервером при появлении сети.
 * Точка остаётся PENDING и повторяется в следующих прогонах, пока не будет
 * достигнут [MAX_SYNC_ATTEMPTS] — тогда она переходит в терминальный статус FAILED.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u0000 \u00102\u00020\u0001:\u0001\u0010B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u000b\u001a\u00020\fH\u0082@\u00a2\u0006\u0002\u0010\rJ\u000e\u0010\u000e\u001a\u00020\u000fH\u0096@\u00a2\u0006\u0002\u0010\rR\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0011"}, d2 = {"Lcom/example/operator/workers/SyncWorker;", "Landroidx/work/CoroutineWorker;", "context", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "authManager", "Lcom/example/operator/auth/AuthManager;", "dao", "Lcom/example/operator/data/local/dao/PendingPointDao;", "cleanupOldSynced", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "doWork", "Landroidx/work/ListenableWorker$Result;", "Companion", "app_debug"})
public final class SyncWorker extends androidx.work.CoroutineWorker {
    @org.jetbrains.annotations.NotNull()
    private final com.example.operator.data.local.dao.PendingPointDao dao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.operator.auth.AuthManager authManager = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String WORK_NAME = "sync_pending_points";
    public static final int MAX_SYNC_ATTEMPTS = 5;
    private static final long SYNCED_RETENTION_MS = 3600000L;
    @org.jetbrains.annotations.NotNull()
    public static final com.example.operator.workers.SyncWorker.Companion Companion = null;
    
    public SyncWorker(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    androidx.work.WorkerParameters params) {
        super(null, null);
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object doWork(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super androidx.work.ListenableWorker.Result> $completion) {
        return null;
    }
    
    private final java.lang.Object cleanupOldSynced(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fR\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/example/operator/workers/SyncWorker$Companion;", "", "()V", "MAX_SYNC_ATTEMPTS", "", "SYNCED_RETENTION_MS", "", "WORK_NAME", "", "schedule", "", "context", "Landroid/content/Context;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        public final void schedule(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
        }
    }
}