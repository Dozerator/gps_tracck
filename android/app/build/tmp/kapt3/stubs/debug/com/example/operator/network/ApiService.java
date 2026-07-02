package com.example.operator.network;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u001e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0007J(\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\b\b\u0001\u0010\n\u001a\u00020\u000b2\b\b\u0001\u0010\u0005\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\r\u00a8\u0006\u000e"}, d2 = {"Lcom/example/operator/network/ApiService;", "", "login", "Lretrofit2/Response;", "Lcom/example/operator/model/TokenResponse;", "request", "Lcom/example/operator/model/LoginRequest;", "(Lcom/example/operator/model/LoginRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "sendLocationPoint", "Lcom/example/operator/model/LocationPointResponse;", "bearerToken", "", "Lcom/example/operator/model/LocationPointRequest;", "(Ljava/lang/String;Lcom/example/operator/model/LocationPointRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface ApiService {
    
    @retrofit2.http.POST(value = "api/auth/login")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object login(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.example.operator.model.LoginRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.example.operator.model.TokenResponse>> $completion);
    
    @retrofit2.http.POST(value = "api/location/point")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object sendLocationPoint(@retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String bearerToken, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.example.operator.model.LocationPointRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.example.operator.model.LocationPointResponse>> $completion);
}