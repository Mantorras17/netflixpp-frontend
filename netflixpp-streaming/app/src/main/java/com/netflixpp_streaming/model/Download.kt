package com.netflixpp_streaming.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Download status for offline movies
 */
enum class DownloadStatus {
    PENDING,      // Queued but not started
    DOWNLOADING,  // Currently downloading
    PAUSED,       // Download paused by user
    COMPLETED,    // Successfully downloaded
    FAILED,       // Download failed
    CANCELLED     // Cancelled by user
}

/**
 * Download quality options
 */
enum class DownloadQuality(val displayName: String, val fileSizeMultiplier: Double) {
    HIGH("High (1080p)", 1.0),
    MEDIUM("Medium (720p)", 0.6),
    LOW("Low (360p)", 0.3)
}

/**
 * Download movie information
 */
data class Download(
    @SerializedName("id") val id: String,
    @SerializedName("movieId") val movieId: String,
    @SerializedName("movieTitle") val movieTitle: String,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerializedName("quality") val quality: DownloadQuality,
    @SerializedName("status") var status: DownloadStatus,
    @SerializedName("progress") var progress: Int = 0, // 0-100
    @SerializedName("downloadedBytes") var downloadedBytes: Long = 0,
    @SerializedName("totalBytes") val totalBytes: Long,
    @SerializedName("localFilePath") var localFilePath: String? = null,
    @SerializedName("downloadStartTime") val downloadStartTime: Long = System.currentTimeMillis(),
    @SerializedName("downloadCompleteTime") var downloadCompleteTime: Long? = null,
    @SerializedName("expiryDate") var expiryDate: Long? = null, // 48 hours after download
    @SerializedName("error") var error: String? = null,
    
    // Transient fields (not serialized)
    @Transient var downloadSpeed: Long = 0, // bytes per second
    @Transient var estimatedTimeRemaining: Long = 0 // seconds
) : Serializable {
    
    fun getProgressPercentage(): Int = progress
    
    fun isExpired(): Boolean {
        return expiryDate?.let { it < System.currentTimeMillis() } ?: false
    }
    
    fun getRemainingExpiryTime(): Long? {
        return expiryDate?.let { (it - System.currentTimeMillis()) / 1000 } // seconds
    }
    
    fun getFormattedSize(): String {
        val mb = totalBytes / (1024.0 * 1024.0)
        val gb = mb / 1024.0
        return if (gb >= 1.0) {
            String.format("%.2f GB", gb)
        } else {
            String.format("%.2f MB", mb)
        }
    }
    
    fun getFormattedDownloadedSize(): String {
        val mb = downloadedBytes / (1024.0 * 1024.0)
        return String.format("%.2f MB", mb)
    }
    
    fun getFormattedSpeed(): String {
        val mbps = downloadSpeed / (1024.0 * 1024.0)
        return String.format("%.2f MB/s", mbps)
    }
    
    fun getFormattedTimeRemaining(): String {
        if (estimatedTimeRemaining <= 0) return "Calculating..."
        
        val hours = estimatedTimeRemaining / 3600
        val minutes = (estimatedTimeRemaining % 3600) / 60
        val seconds = estimatedTimeRemaining % 60
        
        return when {
            hours > 0 -> String.format("%dh %dm", hours, minutes)
            minutes > 0 -> String.format("%dm %ds", minutes, seconds)
            else -> String.format("%ds", seconds)
        }
    }
}