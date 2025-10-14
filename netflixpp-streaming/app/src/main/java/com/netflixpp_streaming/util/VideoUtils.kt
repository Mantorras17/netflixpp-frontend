package com.netflixpp_streaming.util

import android.content.Context
import android.widget.ImageView
import androidx.core.net.toUri
import coil.load
import com.netflixpp_streaming.R
import com.netflixpp_streaming.model.Video
import com.netflixpp_streaming.service.MeshProtocol
import java.util.concurrent.TimeUnit

object VideoUtils {

    fun formatDuration(duration: Int): String {
        val hours = TimeUnit.SECONDS.toHours(duration.toLong())
        val minutes = TimeUnit.SECONDS.toMinutes(duration.toLong()) - TimeUnit.HOURS.toMinutes(hours)
        val seconds = duration - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(minutes)

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun loadThumbnail(imageView: ImageView, thumbnailUrl: String?) {
        imageView.load(thumbnailUrl?.toUri()) {
            placeholder(R.drawable.bg_video_placeholder)
            error(R.drawable.bg_video_placeholder)
            crossfade(true)
        }
    }

    fun shouldUseMesh(context: Context, video: Video): Boolean {
        // Check if mesh is available and has better performance
        val isMeshAvailable = MeshProtocol.isRunning() && MeshProtocol.isVideoAvailableInMesh(video.id!!)
        val userPrefersMesh = Prefs.getSharedPreferences(context).getBoolean("pref_use_mesh", true)

        return isMeshAvailable && userPrefersMesh
    }

    fun getOptimalVideoUrl(video: Video, preferQuality: String = "1080p"): String? {
        return when (preferQuality) {
            "1080p" -> video.videoUrl1080p ?: video.videoUrl360p
            "360p" -> video.videoUrl360p ?: video.videoUrl1080p
            else -> video.videoUrl1080p ?: video.videoUrl360p
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
}