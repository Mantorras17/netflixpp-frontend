package com.netflixpp_streaming.api

import com.netflixpp_streaming.model.*
import com.netflixpp_streaming.model.LoginRequest
import com.netflixpp_streaming.model.LoginResponse
import com.netflixpp_streaming.model.ApiResponse
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @GET("videos")
    fun getVideos(): Call<List<Video>>

    @GET("videos/{id}")
    fun getVideo(@Path("id") id: String): Call<Video>

    @POST("users/{userId}/my-list")
    fun addToMyList(@Path("userId") userId: String, @Body videoId: Map<String, String>): Call<ApiResponse<Unit>>

    @DELETE("users/{userId}/my-list/{videoId}")
    fun removeFromMyList(@Path("userId") userId: String, @Path("videoId") videoId: String): Call<ApiResponse<Unit>>

    @GET("users/{userId}/my-list")
    fun getMyList(@Path("userId") userId: String): Call<List<Video>>
}