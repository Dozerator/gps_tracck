package com.example.operator.auth;

/**
 * Хранит JWT-токен в SharedPreferences и умеет проверять его срок годности
 * по полю "exp" из payload токена — без обращения к серверу.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0004\u0018\u0000 \u00132\u00020\u0001:\u0001\u0013B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u0007\u001a\u00020\bJ\u0012\u0010\t\u001a\u0004\u0018\u00010\n2\u0006\u0010\u000b\u001a\u00020\fH\u0002J\b\u0010\r\u001a\u0004\u0018\u00010\fJ\b\u0010\u000e\u001a\u0004\u0018\u00010\fJ\u0006\u0010\u000f\u001a\u00020\u0010J\u0010\u0010\u0011\u001a\u00020\u00102\u0006\u0010\u000b\u001a\u00020\fH\u0002J\u000e\u0010\u0012\u001a\u00020\b2\u0006\u0010\u000b\u001a\u00020\fR\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcom/example/operator/auth/AuthManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "prefs", "Landroid/content/SharedPreferences;", "clearToken", "", "decodePayload", "Lorg/json/JSONObject;", "token", "", "getToken", "getUserLogin", "isLoggedIn", "", "isTokenExpired", "saveToken", "Companion", "app_debug"})
public final class AuthManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.SharedPreferences prefs = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS_NAME = "operator_auth_prefs";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_TOKEN = "jwt_token";
    @org.jetbrains.annotations.NotNull()
    public static final com.example.operator.auth.AuthManager.Companion Companion = null;
    
    public AuthManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    public final void saveToken(@org.jetbrains.annotations.NotNull()
    java.lang.String token) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getToken() {
        return null;
    }
    
    public final void clearToken() {
    }
    
    public final boolean isLoggedIn() {
        return false;
    }
    
    /**
     * Логин текущего пользователя из поля "sub" JWT-токена, для локального отображения.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getUserLogin() {
        return null;
    }
    
    private final boolean isTokenExpired(java.lang.String token) {
        return false;
    }
    
    private final org.json.JSONObject decodePayload(java.lang.String token) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"Lcom/example/operator/auth/AuthManager$Companion;", "", "()V", "KEY_TOKEN", "", "PREFS_NAME", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}