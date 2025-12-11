package com.netflixpp_cms.model

import com.google.gson.annotations.SerializedName

data class CreateUserRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String = "user"
)

data class UsersResponse(
    @SerializedName("users") val users: List<User>,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total") val total: Int
)

data class PasswordResetResponse(
    @SerializedName("status") val status: String,
    @SerializedName("userId") val userId: Int,
    @SerializedName("newPassword") val newPassword: String,
    @SerializedName("message") val message: String
)

data class SystemStats(
    @SerializedName("totalUsers") val totalUsers: Int?,
    @SerializedName("totalMovies") val totalMovies: Int?,
    @SerializedName("totalViews") val totalViews: Int?,
    @SerializedName("diskUsage") val diskUsage: Long?,
    @SerializedName("activeStreams") val activeStreams: Int?,
    @SerializedName("serverUptime") val serverUptime: Long?
)

data class StorageInfo(
    @SerializedName("totalSpace") val totalSpace: Long,
    @SerializedName("usedSpace") val usedSpace: Long,
    @SerializedName("freeSpace") val freeSpace: Long,
    @SerializedName("movieCount") val movieCount: Int,
    @SerializedName("chunkCount") val chunkCount: Int?
)

data class LogsResponse(
    @SerializedName("logs") val logs: List<LogEntry>,
    @SerializedName("type") val type: String,
    @SerializedName("count") val count: Int
)

data class LogEntry(
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("level") val level: String,
    @SerializedName("message") val message: String,
    @SerializedName("source") val source: String? = null
)

data class CleanupResponse(
    @SerializedName("status") val status: String,
    @SerializedName("filesRemoved") val filesRemoved: Int,
    @SerializedName("spaceFreed") val spaceFreed: Long,
    @SerializedName("message") val message: String?
)

data class HealthResponse(
    @SerializedName("status") val status: String,
    @SerializedName("database") val database: String?,
    @SerializedName("storage") val storage: String?,
    @SerializedName("uptime") val uptime: Long?,
    @SerializedName("message") val message: String?
)

data class PeerInfo(
    @SerializedName("peerId") val peerId: String,
    @SerializedName("address") val address: String,
    @SerializedName("chunks") val chunks: List<String>?,
    @SerializedName("status") val status: String
)