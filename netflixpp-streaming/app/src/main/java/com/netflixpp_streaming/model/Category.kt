package com.netflixpp_streaming.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Enhanced Category model for movie grouping
 */
data class Category(
    @SerializedName("id") val id: String? = null,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("type") val type: CategoryType = CategoryType.GENRE,
    @SerializedName("movies") val movies: List<Movie> = emptyList(),
    @SerializedName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerializedName("order") val order: Int = 0,
    @SerializedName("isVisible") val isVisible: Boolean = true,
    @SerializedName("movieCount") val movieCount: Int = movies.size,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
) : Serializable

/**
 * Category type enumeration
 */
enum class CategoryType {
    @SerializedName("genre") GENRE,
    @SerializedName("trending") TRENDING,
    @SerializedName("new_releases") NEW_RELEASES,
    @SerializedName("recommended") RECOMMENDED,
    @SerializedName("continue_watching") CONTINUE_WATCHING,
    @SerializedName("featured") FEATURED,
    @SerializedName("popular") POPULAR,
    @SerializedName("top_rated") TOP_RATED,
    @SerializedName("my_list") MY_LIST,
    @SerializedName("recently_added") RECENTLY_ADDED,
    @SerializedName("custom") CUSTOM
}

/**
 * Category list response
 */
data class CategoryListResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("categories") val categories: List<Category>,
    @SerializedName("totalCount") val totalCount: Int = categories.size
)