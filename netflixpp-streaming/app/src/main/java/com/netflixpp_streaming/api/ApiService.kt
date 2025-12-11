package com.netflixpp_streaming.api

import com.netflixpp_streaming.model.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @GET("movies")
    fun getMovies(): Call<List<Movie>>

    @GET("movies/{id}")
    fun getMovie(@Path("id") id: String): Call<Movie>

    @GET("movies/search")
    fun searchMovies(
        @Query("q") query: String,
        @Query("genre") genre: String? = null,
        @Query("year") year: Int? = null,
        @Query("minRating") minRating: Double? = null
    ): Call<List<Movie>>

    @GET("movies/genres")
    fun getGenres(): Call<List<String>>

    @POST("users/{userId}/my-list")
    fun addToMyList(@Path("userId") userId: String, @Body movieId: Map<String, String>): Call<ApiResponse<Unit>>

    @DELETE("users/{userId}/my-list/{movieId}")
    fun removeFromMyList(@Path("userId") userId: String, @Path("movieId") movieId: String): Call<ApiResponse<Unit>>

    @GET("users/{userId}/my-list")
    fun getMyList(@Path("userId") userId: String): Call<List<Movie>>
}