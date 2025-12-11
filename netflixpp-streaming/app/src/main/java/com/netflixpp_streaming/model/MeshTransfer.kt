package com.netflixpp_streaming.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Active mesh transfer information
 */
data class MeshTransfer(
    @SerializedName("id") val id: String,
    @SerializedName("movieId") val movieId: String,
    @SerializedName("movieTitle") val movieTitle: String,
    @SerializedName("peerId") val peerId: String,
    @SerializedName("peerName") val peerName: String,
    @SerializedName("chunkId") val chunkId: Int,
    @SerializedName("totalChunks") val totalChunks: Int,
    @SerializedName("progress") var progress: Int = 0, // 0-100
    @SerializedName("bytesTransferred") var bytesTransferred: Long = 0,
    @SerializedName("totalBytes") val totalBytes: Long,
    @SerializedName("startTime") val startTime: Long = System.currentTimeMillis(),
    @SerializedName("transferRate") var transferRate: Long = 0, // bytes per second
    @SerializedName("estimatedTimeRemaining") var estimatedTimeRemaining: Long = 0, // seconds
    @SerializedName("isUpload") val isUpload: Boolean = false // true if uploading to peer, false if downloading
) : Serializable {
    
    fun getFormattedProgress(): String {
        return "$progress%"
    }
    
    fun getFormattedTransferRate(): String {
        val mbps = transferRate.toDouble() / (1024.0 * 1024.0)
        return String.format("%.2f MB/s", mbps)
    }
    
    fun getFormattedSize(): String {
        val mb = totalBytes.toDouble() / (1024.0 * 1024.0)
        return String.format("%.2f MB", mb)
    }
    
    fun getFormattedTransferred(): String {
        val mb = bytesTransferred.toDouble() / (1024.0 * 1024.0)
        return String.format("%.2f MB", mb)
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