package com.netflixpp_streaming.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Enhanced Movie/Movie model
 */
data class Movie(
    @SerializedName("id") val id: String? = null,
    @SerializedName("title") val title: String,
    @SerializedName("originalTitle") val originalTitle: String? = null,
    @SerializedName("description") val description: String,
    @SerializedName("shortDescription") val shortDescription: String? = null,
    @SerializedName("genre") val genre: String,
    @SerializedName("genres") val genres: List<String>? = null,
    @SerializedName("duration") val duration: Int, // in seconds
    @SerializedName("year") val year: Int,
    @SerializedName("releaseDate") val releaseDate: String? = null,
    @SerializedName("movieUrl1080p") val movieUrl1080p: String? = null,
    @SerializedName("movieUrl360p") val movieUrl360p: String? = null,
    @SerializedName("movieUrlMesh") val movieUrlMesh: String? = null,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerializedName("posterUrl") val posterUrl: String? = null,
    @SerializedName("backdropUrl") val backdropUrl: String? = null,
    @SerializedName("trailerUrl") val trailerUrl: String? = null,
    @SerializedName("uploadDate") val uploadDate: String? = null,
    @SerializedName("rating") val rating: Double = 0.0,
    @SerializedName("voteCount") val voteCount: Int = 0,
    @SerializedName("viewCount") val viewCount: Int = 0,
    @SerializedName("popularity") val popularity: Double = 0.0,
    @SerializedName("isInMyList") val isInMyList: Boolean = false,
    @SerializedName("isFeatured") val isFeatured: Boolean = false,
    @SerializedName("isTrending") val isTrending: Boolean = false,
    @SerializedName("isNewRelease") val isNewRelease: Boolean = false,
    @SerializedName("ageRating") val ageRating: String? = null, // G, PG, PG-13, R, etc.
    @SerializedName("language") val language: String = "en",
    @SerializedName("subtitles") val subtitles: List<String>? = null,
    @SerializedName("audioTracks") val audioTracks: List<String>? = null,
    @SerializedName("cast") val cast: List<Person>? = null,
    @SerializedName("director") val director: String? = null,
    @SerializedName("producers") val producers: List<String>? = null,
    @SerializedName("studio") val studio: String? = null,
    @SerializedName("tags") val tags: List<String>? = null,
    @SerializedName("fileSize1080p") val fileSize1080p: Long? = null, // in bytes
    @SerializedName("fileSize360p") val fileSize360p: Long? = null,
    @SerializedName("checksumSHA256") val checksumSHA256: String? = null,
    @SerializedName("chunkCount") val chunkCount: Int? = null,
    @SerializedName("meshAvailable") val meshAvailable: Boolean = false,
    @SerializedName("meshPeers") val meshPeers: Int = 0,
    @SerializedName("watchProgress") val watchProgress: WatchProgress? = null,
    @SerializedName("metadata") val metadata: MovieMetadata? = null
) : Serializable

/**
 * Person model for cast and crew
 */
data class Person(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("role") val role: String? = null, // actor, director, producer
    @SerializedName("character") val character: String? = null, // For actors
    @SerializedName("profileUrl") val profileUrl: String? = null
) : Serializable

/**
 * Watch progress tracking
 */
data class WatchProgress(
    @SerializedName("userId") val userId: String,
    @SerializedName("movieId") val movieId: String,
    @SerializedName("currentTime") val currentTime: Int, // seconds
    @SerializedName("duration") val duration: Int, // seconds
    @SerializedName("percentage") val percentage: Int, // 0-100
    @SerializedName("lastWatched") val lastWatched: String? = null,
    @SerializedName("completed") val completed: Boolean = false
) : Serializable

/**
 * Movie metadata
 */
data class MovieMetadata(
    @SerializedName("movieCodec") val movieCodec: String? = null, // H.264, H.265, VP9
    @SerializedName("audioCodec") val audioCodec: String? = null, // AAC, MP3, Opus
    @SerializedName("resolution") val resolution: String? = null, // 1920x1080, 640x360
    @SerializedName("frameRate") val frameRate: Double? = null, // 24, 30, 60
    @SerializedName("bitrate") val bitrate: Long? = null, // in kbps
    @SerializedName("aspectRatio") val aspectRatio: String? = null, // 16:9, 4:3
    @SerializedName("hasSubtitles") val hasSubtitles: Boolean = false,
    @SerializedName("hasAudioDescription") val hasAudioDescription: Boolean = false,
    @SerializedName("is3D") val is3D: Boolean = false,
    @SerializedName("isHDR") val isHDR: Boolean = false
) : Serializable

/**
 * Movie list response
 */
data class MovieListResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("movies") val movies: List<Movie>,
    @SerializedName("totalCount") val totalCount: Int = movies.size,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("pageSize") val pageSize: Int = 20,
    @SerializedName("hasMore") val hasMore: Boolean = false
)