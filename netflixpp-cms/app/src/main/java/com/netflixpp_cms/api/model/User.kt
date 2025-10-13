package com.netflixpp_cms.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class User(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("role")
    val role: String = "USER",

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null,

    @SerializedName("isActive")
    val isActive: Boolean = true
) {
    fun getFormattedCreatedAt(): String {
        return if (!createdAt.isNullOrEmpty()) {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val date = inputFormat.parse(createdAt)
                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                "Unknown date"
            }
        } else {
            "Unknown date"
        }
    }
}