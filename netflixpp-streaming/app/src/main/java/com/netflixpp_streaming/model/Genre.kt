package com.netflixpp_streaming.model

import com.google.gson.annotations.SerializedName

/**
 * Genre model
 */
data class Genre(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerializedName("movieCount") val movieCount: Int = 0
)