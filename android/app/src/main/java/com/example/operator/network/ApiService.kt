package com.example.operator.network

import com.example.operator.model.LocationPointRequest
import com.example.operator.model.LocationPointResponse
import com.example.operator.model.LoginRequest
import com.example.operator.model.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>

    @POST("api/location/point")
    suspend fun sendLocationPoint(
        @Header("Authorization") bearerToken: String,
        @Body request: LocationPointRequest
    ): Response<LocationPointResponse>
}
