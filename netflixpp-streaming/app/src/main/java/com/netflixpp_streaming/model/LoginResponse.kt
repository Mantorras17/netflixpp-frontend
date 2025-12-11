package com.netflixpp_streaming.model

import com.google.gson.annotations.SerializedName

/**
 * Login response model
 */
data class LoginResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("token") val token: String,
    @SerializedName("refreshToken") val refreshToken: String? = null,
    @SerializedName("expiresIn") val expiresIn: Long? = null, // Token expiration in seconds
    @SerializedName("user") val user: User,
    @SerializedName("message") val message: String? = null
)

/**
 * Registration response
 */
data class RegisterResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("message") val message: String,
    @SerializedName("user") val user: User? = null,
    @SerializedName("requiresVerification") val requiresVerification: Boolean = false
)

/**
 * Token refresh response
 */
data class RefreshTokenResponse(
    @SerializedName("token") val token: String,
    @SerializedName("refreshToken") val refreshToken: String? = null,
    @SerializedName("expiresIn") val expiresIn: Long? = null
)

/**
 * Logout response
 */
data class LogoutResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("message") val message: String
)