package com.example.operator.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val login: String,
    val password: String
)

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Long
)

data class LocationPointRequest(
    val lat: Double,
    val lon: Double,
    val accuracy: Float?,
    val timestamp: String,
    @SerializedName("object_type") val objectType: String,
    val direction: String,
    @SerializedName("threat_level") val threatLevel: String
)

data class LocationPointResponse(
    val id: Long,
    @SerializedName("user_id") val userId: Long,
    val lat: Double,
    val lon: Double,
    val accuracy: Float?,
    val timestamp: String,
    @SerializedName("sent_at") val sentAt: String,
    @SerializedName("object_type") val objectType: String,
    val direction: String,
    @SerializedName("threat_level") val threatLevel: String
)
