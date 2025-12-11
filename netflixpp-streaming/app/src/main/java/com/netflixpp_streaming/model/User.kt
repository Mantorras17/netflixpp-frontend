package com.netflixpp_streaming.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Enhanced User model with profile and preferences
 */
data class User(
    @SerializedName("id") val id: String? = null,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String = "USER", // USER, ADMIN, MODERATOR
    @SerializedName("profilePictureUrl") val profilePictureUrl: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("phoneNumber") val phoneNumber: String? = null,
    @SerializedName("dateOfBirth") val dateOfBirth: String? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("lastLogin") val lastLogin: String? = null,
    @SerializedName("isActive") val isActive: Boolean = true,
    @SerializedName("isVerified") val isVerified: Boolean = false,
    @SerializedName("preferences") val preferences: UserPreferences? = null,
    @SerializedName("statistics") val statistics: UserStatistics? = null
) : Serializable

/**
 * User preferences for movie playback and app settings
 */
data class UserPreferences(
    @SerializedName("defaultQuality") var defaultQuality: String = "auto",
    @SerializedName("autoPlayNext") var autoPlayNext: Boolean = true,
    @SerializedName("autoPlayPreviews") var autoPlayPreviews: Boolean = true,
    @SerializedName("subtitlesEnabled") var subtitlesEnabled: Boolean = false,
    @SerializedName("subtitlesLanguage") var subtitlesLanguage: String = "en",
    @SerializedName("audioLanguage") var audioLanguage: String = "en",
    @SerializedName("dataSaverMode") var dataSaverMode: Boolean = false,
    @SerializedName("downloadQuality") var downloadQuality: String = "720p",
    @SerializedName("meshEnabled") var meshEnabled: Boolean = true,
    @SerializedName("maxMeshConnections") var maxMeshConnections: Int = 5,
    @SerializedName("notificationsEnabled") var notificationsEnabled: Boolean = true,
    @SerializedName("emailNotifications") var emailNotifications: Boolean = true,
    @SerializedName("pushNotifications") var pushNotifications: Boolean = true,
    @SerializedName("theme") var theme: String = "dark"
) : Serializable

/**
 * User viewing statistics
 */
data class UserStatistics(
    @SerializedName("totalWatchTime") val totalWatchTime: Long = 0, // in seconds
    @SerializedName("moviesWatched") val moviesWatched: Int = 0,
    @SerializedName("dataStreamed") val dataStreamed: Long = 0, // in bytes
    @SerializedName("dataShared") val dataShared: Long = 0, // in bytes via mesh
    @SerializedName("favoriteGenre") val favoriteGenre: String? = null,
    @SerializedName("averageRating") val averageRating: Double = 0.0,
    @SerializedName("myListCount") val myListCount: Int = 0,
    @SerializedName("downloadsCount") val downloadsCount: Int = 0
) : Serializable