package com.example.operator.utils;

/**
 * Отслеживает наличие интернета через современный ConnectivityManager.NetworkCallback API
 * (без deprecated ConnectivityManager.CONNECTIVITY_ACTION / getActiveNetworkInfo).
 * Живёт всё время жизни процесса — держится как синглтон в OperatorApp.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\r\u001a\u00020\tH\u0002R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\nR\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/example/operator/utils/NetworkMonitor;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "connectivityManager", "Landroid/net/ConnectivityManager;", "isOnline", "Lkotlinx/coroutines/flow/StateFlow;", "", "()Lkotlinx/coroutines/flow/StateFlow;", "monitorScope", "Lkotlinx/coroutines/CoroutineScope;", "isCurrentlyOnline", "app_debug"})
public final class NetworkMonitor {
    @org.jetbrains.annotations.NotNull()
    private final android.net.ConnectivityManager connectivityManager = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope monitorScope = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isOnline = null;
    
    public NetworkMonitor(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isOnline() {
        return null;
    }
    
    private final boolean isCurrentlyOnline() {
        return false;
    }
}