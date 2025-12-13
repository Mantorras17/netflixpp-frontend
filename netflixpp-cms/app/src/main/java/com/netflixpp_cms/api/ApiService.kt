package com.netflixpp_cms.api

import com.netflixpp_cms.model.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // ============ AUTHENTICATION ============
    @POST("auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("auth/register")
    fun register(@Body registerRequest: RegisterRequest): Call<ApiResponse<String>>

    @POST("auth/change-password")
    fun changePassword(@Body passwords: ChangePasswordRequest): Call<ApiResponse<String>>

    @POST("auth/reset-password")
    fun resetPassword(@Body request: ResetPasswordRequest): Call<ApiResponse<String>>

    @GET("auth/me")
    fun getCurrentUser(): Call<User>

    @POST("auth/logout")
    fun logout(): Call<ApiResponse<String>>

    @GET("auth/validate-token")
    fun validateToken(): Call<TokenValidationResponse>

    // ============ MOVIES (Public) ============
    @GET("movies")
    fun getMovies(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("sort") sort: String = "newest"
    ): Call<MoviesResponse>

    @GET("movies/search")
    fun searchMovies(@Query("q") query: String): Call<List<Movie>>

    @GET("movies/categories")
    fun getCategories(): Call<List<String>>

    @GET("movies/genres")
    fun getGenres(): Call<List<String>>

    @GET("movies/statistics")
    fun getMovieStatistics(): Call<MovieStatistics>

    @GET("movies/featured")
    fun getFeaturedMovies(): Call<List<Movie>>

    @GET("movies/recent")
    fun getRecentMovies(@Query("limit") limit: Int = 10): Call<List<Movie>>

    @GET("movies/category/{category}")
    fun getMoviesByCategory(@Path("category") category: String): Call<List<Movie>>

    @GET("movies/genre/{genre}")
    fun getMoviesByGenre(@Path("genre") genre: String): Call<List<Movie>>

    // ============ ADMIN - MOVIE MANAGEMENT ============
    @Multipart
    @POST("admin/movies")
    fun uploadMovie(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("category") category: RequestBody,
        @Part("genre") genre: RequestBody,
        @Part("year") year: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part file: MultipartBody.Part
    ): Call<MovieUploadResponse>

    @GET("admin/movies/{id}")
    fun getMovie(@Path("id") id: Int): Call<Movie>

    @PUT("admin/movies/{id}")
    fun updateMovie(
        @Path("id") id: Int,
        @Body updates: Map<String, Any>
    ): Call<ApiResponse<String>>

    @DELETE("admin/movies/{id}")
    fun deleteMovie(@Path("id") id: Int): Call<ApiResponse<String>>

    @POST("admin/movies/{id}/chunks")
    fun generateChunks(@Path("id") id: Int): Call<ChunkGenerationResponse>

    // ============ ADMIN - USER MANAGEMENT ============
    @GET("admin/users")
    fun getUsers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Call<UsersResponse>

    @GET("admin/users/{id}")
    fun getUser(@Path("id") id: Int): Call<User>

    @POST("admin/users")
    fun createUser(@Body user: CreateUserRequest): Call<ApiResponse<User>>

    @PUT("admin/users/{id}")
    fun updateUser(
        @Path("id") id: Int,
        @Body updates: Map<String, @JvmSuppressWildcards Any>
    ): Call<Map<String, @JvmSuppressWildcards Any>>

    @DELETE("admin/users/{id}")
    fun deleteUser(@Path("id") id: Int): Call<ApiResponse<String>>

    @POST("admin/users/{id}/reset-password")
    fun resetUserPassword(@Path("id") id: Int): Call<PasswordResetResponse>

    // ============ ADMIN - SYSTEM MANAGEMENT ============
    @GET("admin/stats")
    fun getSystemStats(): Call<SystemStats>

    @GET("admin/storage")
    fun getStorageInfo(): Call<StorageInfo>

    @GET("admin/logs")
    fun getLogs(
        @Query("type") type: String = "system",
        @Query("limit") limit: Int = 100
    ): Call<LogsResponse>

    @POST("admin/cleanup")
    fun cleanupSystem(): Call<CleanupResponse>

    @GET("admin/health")
    fun healthCheck(): Call<HealthResponse>

    // ============ STREAMING INFO (for monitoring) ============
    @GET("stream/chunks/{movieId}")
    fun getChunksInfo(@Path("movieId") movieId: String): Call<ChunksInfo>

    @GET("stream/chunk/{movieId}/{chunkIndex}")
    @Streaming
    fun getChunk(
        @Path("movieId") movieId: String,
        @Path("chunkIndex") chunkIndex: Int
    ): Call<ResponseBody>

    @GET("stream/manifest/{movieId}")
    fun getStreamManifest(@Path("movieId") movieId: String): Call<StreamManifest>

    // ============ MESH/P2P INFO (for monitoring) ============
    @GET("mesh/peers")
    fun getActivePeers(): Call<List<PeerInfo>>

    @GET("mesh/chunks/{movieId}")
    fun getMeshChunks(@Path("movieId") movieId: String): Call<Map<String, Any>>
}