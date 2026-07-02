package com.example.operator;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0018\u001a\u00020\u0019H\u0016R\u001b\u0010\u0003\u001a\u00020\u00048FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0007\u0010\b\u001a\u0004\b\u0005\u0010\u0006R\u001b\u0010\t\u001a\u00020\n8FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b\r\u0010\b\u001a\u0004\b\u000b\u0010\fR\u001b\u0010\u000e\u001a\u00020\u000f8FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0012\u0010\b\u001a\u0004\b\u0010\u0010\u0011R\u001b\u0010\u0013\u001a\u00020\u00148FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0017\u0010\b\u001a\u0004\b\u0015\u0010\u0016\u00a8\u0006\u001a"}, d2 = {"Lcom/example/operator/OperatorApp;", "Landroid/app/Application;", "()V", "authManager", "Lcom/example/operator/auth/AuthManager;", "getAuthManager", "()Lcom/example/operator/auth/AuthManager;", "authManager$delegate", "Lkotlin/Lazy;", "database", "Lcom/example/operator/data/local/AppDatabase;", "getDatabase", "()Lcom/example/operator/data/local/AppDatabase;", "database$delegate", "locationRepository", "Lcom/example/operator/data/repository/LocationRepository;", "getLocationRepository", "()Lcom/example/operator/data/repository/LocationRepository;", "locationRepository$delegate", "networkMonitor", "Lcom/example/operator/utils/NetworkMonitor;", "getNetworkMonitor", "()Lcom/example/operator/utils/NetworkMonitor;", "networkMonitor$delegate", "onCreate", "", "app_debug"})
public final class OperatorApp extends android.app.Application {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy authManager$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy networkMonitor$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy database$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy locationRepository$delegate = null;
    
    public OperatorApp() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.operator.auth.AuthManager getAuthManager() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.operator.utils.NetworkMonitor getNetworkMonitor() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.operator.data.local.AppDatabase getDatabase() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.operator.data.repository.LocationRepository getLocationRepository() {
        return null;
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
}