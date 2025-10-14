package com.netflixpp_streaming.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: String? = null,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String = "USER",
    @SerializedName("createdAt") val createdAt: String? = null
)

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: User,
    @SerializedName("message") val message: String? = null
)