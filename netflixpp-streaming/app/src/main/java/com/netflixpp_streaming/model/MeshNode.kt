package com.netflixpp_streaming.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Enhanced Mesh Node/Peer representation
 */
data class MeshNode(
    @SerializedName("id") val id: String,
    @SerializedName("ipAddress") val ipAddress: String,
    @SerializedName("port") val port: Int = 8088,
    @SerializedName("deviceName") val deviceName: String,
    @SerializedName("deviceType") val deviceType: DeviceType = DeviceType.MOBILE,
    @SerializedName("availableMovies") val availableMovies: List<String> = emptyList(), // Movie IDs
    @SerializedName("bandwidth") val bandwidth: Long = 0, // bytes per second
    @SerializedName("latency") val latency: Long = 0, // milliseconds
    @SerializedName("connectionQuality") val connectionQuality: ConnectionQuality = ConnectionQuality.UNKNOWN,
    @SerializedName("isSharing") val isSharing: Boolean = false,
    @SerializedName("uploadSpeed") val uploadSpeed: Long = 0, // bytes per second
    @SerializedName("downloadSpeed") val downloadSpeed: Long = 0, // bytes per second
    @SerializedName("dataShared") val dataShared: Long = 0, // total bytes shared
    @SerializedName("dataReceived") val dataReceived: Long = 0, // total bytes received
    @SerializedName("connectedAt") val connectedAt: Long = System.currentTimeMillis(),
    @SerializedName("lastSeenAt") var lastSeenAt: Long = System.currentTimeMillis(),
    @SerializedName("batteryLevel") val batteryLevel: Int? = null, // 0-100
    @SerializedName("isCharging") val isCharging: Boolean = false,
    @SerializedName("signalStrength") val signalStrength: Int = 0, // 0-100
    @SerializedName("isActive") var isActive: Boolean = true,
    @Transient var currentTransferRate: Long = 0 // current bytes per second
) : Serializable {

    enum class DeviceType {
        MOBILE, TABLET, TV, DESKTOP, UNKNOWN
    }

    enum class ConnectionQuality {
        EXCELLENT, GOOD, FAIR, POOR, UNKNOWN
    }
    
    fun getFormattedBandwidth(): String {
        val mbps = bandwidth.toDouble() / (1024.0 * 1024.0)
        return String.format("%.2f MB/s", mbps)
    }
    
    fun getFormattedLatency(): String {
        return "${latency}ms"
    }
    
    fun getFormattedDataShared(): String {
        val mb = dataShared.toDouble() / (1024.0 * 1024.0)
        val gb = mb / 1024.0
        return if (gb >= 1.0) {
            String.format("%.2f GB", gb)
        } else {
            String.format("%.2f MB", mb)
        }
    }
    
    fun getFormattedDataReceived(): String {
        val mb = dataReceived.toDouble() / (1024.0 * 1024.0)
        val gb = mb / 1024.0
        return if (gb >= 1.0) {
            String.format("%.2f GB", gb)
        } else {
            String.format("%.2f MB", mb)
        }
    }
    
    fun getConnectionTimeMinutes(): Long {
        return (System.currentTimeMillis() - connectedAt) / 60000
    }
    
    fun isRecentlyActive(): Boolean {
        return (System.currentTimeMillis() - lastSeenAt) < 30000 // 30 seconds
    }
}