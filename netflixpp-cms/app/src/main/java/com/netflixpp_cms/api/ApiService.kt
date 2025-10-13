package com.netflixpp_cms.api

import com.netflixpp_cms.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // Authentication
    @POST("auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    // Video Management
    @GET("videos")
    fun getVideos(): Call<ApiResponse<List<Video>>>

    @Multipart
    @POST("videos/upload")
    fun uploadVideo(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("genre") genre: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part("year") year: RequestBody,
        @Part videoFile: MultipartBody.Part
    ): Call<ApiResponse<Video>>

    @DELETE("videos/{id}")
    fun deleteVideo(@Path("id") id: String): Call<ApiResponse<Unit>>

    // User Management
    @GET("users")
    fun getUsers(): Call<ApiResponse<List<User>>>

    @POST("users")
    fun createUser(@Body user: User): Call<ApiResponse<User>>

    @DELETE("users/{id}")
    fun deleteUser(@Path("id") id: String): Call<ApiResponse<Unit>>

    // Check server status
    @GET("health")
    fun healthCheck(): Call<ApiResponse<String>>
}