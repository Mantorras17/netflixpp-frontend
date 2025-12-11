package com.netflixpp_streaming.util

import android.content.Context
import android.widget.ImageView
import androidx.core.net.toUri
import coil.load
import com.netflixpp_streaming.R
import com.netflixpp_streaming.model.Movie
import com.netflixpp_streaming.service.MeshProtocol

object MovieUtils {

    fun formatDuration(durationSeconds: Int): String {
        val hours = durationSeconds / 3600
        val minutes = (durationSeconds % 3600) / 60
        val seconds = durationSeconds % 60

        return when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun loadThumbnail(imageView: ImageView, thumbnailUrl: String?) {
        imageView.load(thumbnailUrl?.toUri()) {
            placeholder(R.drawable.bg_movie_placeholder)
            error(R.drawable.bg_movie_placeholder)
            crossfade(true)
        }
    }

    fun shouldUseMesh(context: Context, movie: Movie): Boolean {
        // Check if mesh is available and has better performance
        val isMeshAvailable = MeshProtocol.isRunning() && MeshProtocol.isMovieAvailableInMesh(movie.id!!)
        val userPrefersMesh = Prefs.getSharedPreferences(context).getBoolean("pref_use_mesh", true)

        return isMeshAvailable && userPrefersMesh
    }

    fun getOptimalMovieUrl(movie: Movie, preferQuality: String = "1080p"): String? {
        return when (preferQuality) {
            "1080p" -> movie.movieUrl1080p ?: movie.movieUrl360p
            "360p" -> movie.movieUrl360p ?: movie.movieUrl1080p
            else -> movie.movieUrl1080p ?: movie.movieUrl360p
        }
    }

    fun formatFileSize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB")
        var size = bytes.toDouble()
        var unitIndex = 0

        while (size > 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }

        return "%.1f %s".format(size, units[unitIndex])
    }

    fun formatDurationReadable(durationSeconds: Int): String {
        val hours = durationSeconds / 3600
        val minutes = (durationSeconds % 3600) / 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "${durationSeconds}s"
        }
    }

    fun getQualityLabel(movieUrl1080p: String?, movieUrl360p: String?): String {
        val qualities = mutableListOf<String>()
        if (movieUrl1080p != null) qualities.add("HD")
        if (movieUrl360p != null) qualities.add("SD")
        return qualities.joinToString(" • ")
    }
}