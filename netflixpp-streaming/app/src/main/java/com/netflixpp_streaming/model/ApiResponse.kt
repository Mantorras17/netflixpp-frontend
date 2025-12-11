package com.netflixpp_streaming.model

import com.google.gson.annotations.SerializedName

/**
 * Generic API response wrapper
 */
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null,
    @SerializedName("error") val error: ApiError? = null,
    @SerializedName("timestamp") val timestamp: Long? = null,
    @SerializedName("requestId") val requestId: String? = null
)

/**
 * API error details
 */
data class ApiError(
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("details") val details: String? = null,
    @SerializedName("field") val field: String? = null, // For validation errors
    @SerializedName("statusCode") val statusCode: Int? = null
)

/**
 * Paginated response
 */
data class PaginatedResponse<T>(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("data") val data: List<T>,
    @SerializedName("pagination") val pagination: Pagination
)

/**
 * Pagination metadata
 */
data class Pagination(
    @SerializedName("currentPage") val currentPage: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("totalItems") val totalItems: Int,
    @SerializedName("hasNext") val hasNext: Boolean,
    @SerializedName("hasPrevious") val hasPrevious: Boolean
)