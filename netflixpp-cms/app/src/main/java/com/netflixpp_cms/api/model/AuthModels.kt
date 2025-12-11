package com.netflixpp_cms.model

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("email") val email: String
)

data class ChangePasswordRequest(
    @SerializedName("oldPassword") val oldPassword: String,
    @SerializedName("newPassword") val newPassword: String
)

data class ResetPasswordRequest(
    @SerializedName("email") val email: String
)

data class TokenValidationResponse(
    @SerializedName("valid") val valid: Boolean,
    @SerializedName("username") val username: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("error") val error: String?
)