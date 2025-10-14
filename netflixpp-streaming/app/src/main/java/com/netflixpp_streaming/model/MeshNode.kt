package com.netflixpp_streaming.model

data class MeshNode(
    val ipAddress: String,
    val deviceName: String,
    val availableVideos: Int,
    val bandwidth: Long // in MB/s
)