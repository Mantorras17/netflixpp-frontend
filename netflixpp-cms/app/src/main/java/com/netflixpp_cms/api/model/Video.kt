package com.netflixpp_cms.model

import com.google.gson.annotations.SerializedName

data class Video(
    @SerializedName("id") val id: String? = null,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("genre") val genre: String,
    @SerializedName("duration") val duration: Int, // in seconds
    @SerializedName("year") val year: Int,
    @SerializedName("videoUrl1080p") val videoUrl1080p: String? = null,
    @SerializedName("videoUrl360p") val videoUrl360p: String? = null,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerializedName("uploadDate") val uploadDate: String? = null
)