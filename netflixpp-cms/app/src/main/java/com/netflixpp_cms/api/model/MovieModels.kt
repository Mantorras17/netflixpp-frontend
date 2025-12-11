package com.netflixpp_cms.model

import com.google.gson.annotations.SerializedName

data class Movie(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("category") val category: String,
    @SerializedName("genre") val genre: String,
    @SerializedName("year") val year: Int,
    @SerializedName("duration") val duration: Int,
    @SerializedName("filePath1080") val filePath1080: String?,
    @SerializedName("filePath360") val filePath360: String?
)

data class MoviesResponse(
    @SerializedName("movies") val movies: List<Movie>,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("pages") val pages: Int
)

data class MovieResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("category") val category: String,
    @SerializedName("genre") val genre: String,
    @SerializedName("year") val year: Int,
    @SerializedName("duration") val duration: Int
)

data class MovieUpdateRequest(
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("genre") val genre: String? = null,
    @SerializedName("year") val year: Int? = null,
    @SerializedName("duration") val duration: Int? = null
)

data class MovieUploadResponse(
    @SerializedName("status") val status: String,
    @SerializedName("movieId") val movieId: Int,
    @SerializedName("message") val message: String?
)

data class ChunkGenerationResponse(
    @SerializedName("status") val status: String,
    @SerializedName("movieId") val movieId: Int,
    @SerializedName("chunksGenerated") val chunksGenerated: Int? = null,
    @SerializedName("message") val message: String? = null
)

data class MovieStatistics(
    @SerializedName("totalMovies") val totalMovies: Int,
    @SerializedName("totalViews") val totalViews: Int,
    @SerializedName("categoryCounts") val categoryCounts: Map<String, Int>?,
    @SerializedName("genreCounts") val genreCounts: Map<String, Int>?
)

data class ChunksInfo(
    @SerializedName("movieId") val movieId: String,
    @SerializedName("totalChunks") val totalChunks: Int,
    @SerializedName("chunkSize") val chunkSize: Long,
    @SerializedName("quality") val quality: String
)

data class StreamManifest(
    @SerializedName("movieId") val movieId: String,
    @SerializedName("availableQualities") val availableQualities: List<String>,
    @SerializedName("duration") val duration: Int,
    @SerializedName("format") val format: String
)