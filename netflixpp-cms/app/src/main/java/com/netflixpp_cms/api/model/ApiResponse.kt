package com.netflixpp_cms.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: T? = null,

    @SerializedName("error")
    val error: String? = null,

    @SerializedName("statusCode")
    val statusCode: Int? = null
) {
    companion object {
        fun <T> success(data: T? = null, message: String? = "Success"): ApiResponse<T> {
            return ApiResponse(
                success = true,
                message = message,
                data = data,
                statusCode = 200
            )
        }

        fun <T> error(error: String, statusCode: Int = 500): ApiResponse<T> {
            return ApiResponse(
                success = false,
                error = error,
                statusCode = statusCode
            )
        }
    }
}