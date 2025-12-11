package com.netflixpp_streaming.model

import com.google.gson.annotations.SerializedName

/**
 * User rating/review model
 */
data class Rating(
    @SerializedName("id") val id: String? = null,
    @SerializedName("userId") val userId: String,
    @SerializedName("movieId") val movieId: String,
    @SerializedName("rating") val rating: Double, // 0.0 to 5.0
    @SerializedName("review") val review: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("helpful") val helpful: Int = 0,
    @SerializedName("notHelpful") val notHelpful: Int = 0
)

/**
 * Submit rating request
 */
data class SubmitRatingRequest(
    @SerializedName("movieId") val movieId: String,
    @SerializedName("rating") val rating: Double,
    @SerializedName("review") val review: String? = null
)