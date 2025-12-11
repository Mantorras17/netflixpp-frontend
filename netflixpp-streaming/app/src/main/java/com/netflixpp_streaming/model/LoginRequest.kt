package com.netflixpp_streaming.model

import com.google.gson.annotations.SerializedName

/**
 * Login request model
 */
data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("deviceId") val deviceId: String? = null,
    @SerializedName("deviceName") val deviceName: String? = null,
    @SerializedName("fcmToken") val fcmToken: String? = null, // For push notifications
    @SerializedName("rememberMe") val rememberMe: Boolean = false
)

/**
 * Registration request model
 */
data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("phoneNumber") val phoneNumber: String? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("acceptTerms") val acceptTerms: Boolean = true
)

/**
 * Change password request
 */
data class ChangePasswordRequest(
    @SerializedName("currentPassword") val currentPassword: String,
    @SerializedName("newPassword") val newPassword: String,
    @SerializedName("confirmPassword") val confirmPassword: String
)

/**
 * Reset password request
 */
data class ResetPasswordRequest(
    @SerializedName("email") val email: String
)

/**
 * Update profile request
 */
data class UpdateProfileRequest(
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phoneNumber") val phoneNumber: String? = null,
    @SerializedName("dateOfBirth") val dateOfBirth: String? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("profilePictureUrl") val profilePictureUrl: String? = null
)

/**
 * Update preferences request
 */
data class UpdatePreferencesRequest(
    @SerializedName("preferences") val preferences: UserPreferences
)