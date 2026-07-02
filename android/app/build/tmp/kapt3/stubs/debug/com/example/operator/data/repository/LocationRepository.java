package com.example.operator.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0088\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B-\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\fJ\u000e\u0010\u001a\u001a\u00020\u001bH\u0086@\u00a2\u0006\u0002\u0010\u001cJF\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020 2\u0006\u0010\"\u001a\u00020#2\u0006\u0010$\u001a\u00020\u001e2\u0006\u0010%\u001a\u00020&2\u0006\u0010\'\u001a\u00020(2\u0006\u0010)\u001a\u00020*H\u0082@\u00a2\u0006\u0002\u0010+JH\u0010,\u001a\u00020-2\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020 2\u0006\u0010\"\u001a\u00020#2\u0006\u0010$\u001a\u00020\u001e2\u0006\u0010%\u001a\u00020&2\u0006\u0010\'\u001a\u00020(2\b\b\u0002\u0010)\u001a\u00020*H\u0086@\u00a2\u0006\u0002\u0010+J\u0006\u0010.\u001a\u00020\u001bR\u001d\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\u000f0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00150\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0016R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00180\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0012\u00a8\u0006/"}, d2 = {"Lcom/example/operator/data/repository/LocationRepository;", "", "dao", "Lcom/example/operator/data/local/dao/PendingPointDao;", "apiService", "Lcom/example/operator/network/ApiService;", "authManager", "Lcom/example/operator/auth/AuthManager;", "networkMonitor", "Lcom/example/operator/utils/NetworkMonitor;", "context", "Landroid/content/Context;", "(Lcom/example/operator/data/local/dao/PendingPointDao;Lcom/example/operator/network/ApiService;Lcom/example/operator/auth/AuthManager;Lcom/example/operator/utils/NetworkMonitor;Landroid/content/Context;)V", "allPoints", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/example/operator/data/local/entity/PendingPointEntity;", "getAllPoints", "()Lkotlinx/coroutines/flow/Flow;", "isOnline", "Lkotlinx/coroutines/flow/StateFlow;", "", "()Lkotlinx/coroutines/flow/StateFlow;", "pendingCount", "", "getPendingCount", "retryFailed", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "saveToQueue", "", "lat", "", "lon", "accuracy", "", "timestampMillis", "objectType", "Lcom/example/operator/model/ObjectType;", "direction", "Lcom/example/operator/model/Direction;", "threatLevel", "Lcom/example/operator/model/ThreatLevel;", "(DDFJLcom/example/operator/model/ObjectType;Lcom/example/operator/model/Direction;Lcom/example/operator/model/ThreatLevel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "sendPoint", "Lcom/example/operator/data/repository/SendResult;", "triggerSync", "app_debug"})
public final class LocationRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.example.operator.data.local.dao.PendingPointDao dao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.operator.network.ApiService apiService = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.operator.auth.AuthManager authManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.operator.utils.NetworkMonitor networkMonitor = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<java.lang.Integer> pendingCount = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<java.util.List<com.example.operator.data.local.entity.PendingPointEntity>> allPoints = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isOnline = null;
    
    public LocationRepository(@org.jetbrains.annotations.NotNull()
    com.example.operator.data.local.dao.PendingPointDao dao, @org.jetbrains.annotations.NotNull()
    com.example.operator.network.ApiService apiService, @org.jetbrains.annotations.NotNull()
    com.example.operator.auth.AuthManager authManager, @org.jetbrains.annotations.NotNull()
    com.example.operator.utils.NetworkMonitor networkMonitor, @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.lang.Integer> getPendingCount() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.operator.data.local.entity.PendingPointEntity>> getAllPoints() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isOnline() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object sendPoint(double lat, double lon, float accuracy, long timestampMillis, @org.jetbrains.annotations.NotNull()
    com.example.operator.model.ObjectType objectType, @org.jetbrains.annotations.NotNull()
    com.example.operator.model.Direction direction, @org.jetbrains.annotations.NotNull()
    com.example.operator.model.ThreatLevel threatLevel, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.operator.data.repository.SendResult> $completion) {
        return null;
    }
    
    private final java.lang.Object saveToQueue(double lat, double lon, float accuracy, long timestampMillis, com.example.operator.model.ObjectType objectType, com.example.operator.model.Direction direction, com.example.operator.model.ThreatLevel threatLevel, kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    /**
     * Повторить точки, ранее исчерпавшие лимит попыток, и сразу запустить синхронизацию.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object retryFailed(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    public final void triggerSync() {
    }
}