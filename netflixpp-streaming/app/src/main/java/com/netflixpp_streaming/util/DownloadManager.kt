package com.netflixpp_streaming.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.netflixpp_streaming.model.Download
import com.netflixpp_streaming.model.DownloadQuality
import com.netflixpp_streaming.model.DownloadStatus
import com.netflixpp_streaming.model.Movie
import java.io.File
import java.util.UUID

object DownloadManager {
    private const val PREFS_DOWNLOADS = "downloads"
    private const val DOWNLOAD_DIR = "netflixpp_downloads"
    private const val EXPIRY_DURATION = 48 * 60 * 60 * 1000L // 48 hours
    
    private val listeners = mutableListOf<DownloadListener>()

    interface DownloadListener {
        fun onDownloadStarted(download: Download)
        fun onDownloadProgress(download: Download)
        fun onDownloadCompleted(download: Download)
        fun onDownloadFailed(download: Download)
        fun onDownloadCancelled(download: Download)
    }

    fun addListener(listener: DownloadListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: DownloadListener) {
        listeners.remove(listener)
    }

    fun createDownload(context: Context, movie: Movie, quality: DownloadQuality): Download {
        val downloadId = UUID.randomUUID().toString()
        val totalBytes = when (quality) {
            DownloadQuality.HIGH -> movie.fileSize1080p ?: 1024L * 1024L * 1024L * 2 // 2GB default
            DownloadQuality.MEDIUM -> ((movie.fileSize1080p ?: 1024L * 1024L * 1024L * 2) * 0.6).toLong()
            DownloadQuality.LOW -> movie.fileSize360p ?: 512L * 1024L * 1024L // 512MB default
        }
        
        val download = Download(
            id = downloadId,
            movieId = movie.id ?: "",
            movieTitle = movie.title,
            thumbnailUrl = movie.thumbnailUrl,
            quality = quality,
            status = DownloadStatus.PENDING,
            totalBytes = totalBytes,
            expiryDate = System.currentTimeMillis() + EXPIRY_DURATION
        )
        
        saveDownload(context, download)
        return download
    }

    fun getAllDownloads(context: Context): List<Download> {
        val prefs = Prefs.getSharedPreferences(context)
        val downloadsJson = prefs.getString(PREFS_DOWNLOADS, null) ?: return emptyList()
        
        val type = object : TypeToken<List<Download>>() {}.type
        return Gson().fromJson(downloadsJson, type)
    }

    fun getDownload(context: Context, downloadId: String): Download? {
        return getAllDownloads(context).find { it.id == downloadId }
    }

    fun getDownloadsByStatus(context: Context, status: DownloadStatus): List<Download> {
        return getAllDownloads(context).filter { it.status == status }
    }

    fun getCompletedDownloads(context: Context): List<Download> {
        return getAllDownloads(context).filter { 
            it.status == DownloadStatus.COMPLETED && !it.isExpired() 
        }
    }

    fun isMovieDownloaded(context: Context, movieId: String): Boolean {
        return getAllDownloads(context).any { 
            it.movieId == movieId && it.status == DownloadStatus.COMPLETED && !it.isExpired()
        }
    }

    fun saveDownload(context: Context, download: Download) {
        val downloads = getAllDownloads(context).toMutableList()
        val existingIndex = downloads.indexOfFirst { it.id == download.id }
        
        if (existingIndex != -1) {
            downloads[existingIndex] = download
        } else {
            downloads.add(download)
        }
        
        saveAllDownloads(context, downloads)
    }

    fun updateDownloadStatus(context: Context, downloadId: String, status: DownloadStatus) {
        val download = getDownload(context, downloadId) ?: return
        download.status = status
        saveDownload(context, download)
        
        when (status) {
            DownloadStatus.DOWNLOADING -> listeners.forEach { it.onDownloadStarted(download) }
            DownloadStatus.COMPLETED -> listeners.forEach { it.onDownloadCompleted(download) }
            DownloadStatus.FAILED -> listeners.forEach { it.onDownloadFailed(download) }
            DownloadStatus.CANCELLED -> listeners.forEach { it.onDownloadCancelled(download) }
            else -> {}
        }
    }

    fun updateDownloadProgress(context: Context, downloadId: String, progress: Int, downloadedBytes: Long) {
        val download = getDownload(context, downloadId) ?: return
        download.progress = progress
        download.downloadedBytes = downloadedBytes
        
        // Calculate download speed and ETA
        val timeElapsed = (System.currentTimeMillis() - download.downloadStartTime) / 1000 // seconds
        if (timeElapsed > 0) {
            download.downloadSpeed = downloadedBytes / timeElapsed
            val remainingBytes = download.totalBytes - downloadedBytes
            download.estimatedTimeRemaining = if (download.downloadSpeed > 0) {
                remainingBytes / download.downloadSpeed
            } else {
                0
            }
        }
        
        saveDownload(context, download)
        listeners.forEach { it.onDownloadProgress(download) }
    }

    fun completeDownload(context: Context, downloadId: String) {
        val download = getDownload(context, downloadId) ?: return
        download.status = DownloadStatus.COMPLETED
        download.progress = 100
        download.downloadCompleteTime = System.currentTimeMillis()
        download.localFilePath = getDownloadFilePath(context, download)
        
        saveDownload(context, download)
        listeners.forEach { it.onDownloadCompleted(download) }
    }

    fun cancelDownload(context: Context, downloadId: String) {
        val download = getDownload(context, downloadId) ?: return
        download.status = DownloadStatus.CANCELLED
        
        // Delete partial file if exists
        download.localFilePath?.let { path ->
            File(path).delete()
        }
        
        saveDownload(context, download)
        listeners.forEach { it.onDownloadCancelled(download) }
    }

    fun deleteDownload(context: Context, downloadId: String) {
        val download = getDownload(context, downloadId) ?: return
        
        // Delete file from storage
        download.localFilePath?.let { path ->
            File(path).delete()
        }
        
        // Remove from list
        val downloads = getAllDownloads(context).toMutableList()
        downloads.removeAll { it.id == downloadId }
        saveAllDownloads(context, downloads)
    }

    fun updateDownloadError(context: Context, downloadId: String, error: String) {
        val download = getDownload(context, downloadId) ?: return
        download.status = DownloadStatus.FAILED
        download.error = error
        saveDownload(context, download)
        listeners.forEach { it.onDownloadFailed(download) }
    }

    fun getTotalDownloadSize(context: Context): Long {
        return getCompletedDownloads(context).sumOf { it.totalBytes }
    }

    fun getAvailableSpace(context: Context): Long {
        val downloadDir = getDownloadDirectory(context)
        return downloadDir.usableSpace
    }

    fun hasEnoughSpace(context: Context, requiredBytes: Long): Boolean {
        return getAvailableSpace(context) > requiredBytes * 1.1 // 10% buffer
    }

    fun cleanupExpiredDownloads(context: Context) {
        val downloads = getAllDownloads(context)
        val expiredDownloads = downloads.filter { it.isExpired() }
        
        expiredDownloads.forEach { download ->
            deleteDownload(context, download.id)
        }
    }

    private fun saveAllDownloads(context: Context, downloads: List<Download>) {
        val prefs = Prefs.getSharedPreferences(context)
        val downloadsJson = Gson().toJson(downloads)
        prefs.edit().putString(PREFS_DOWNLOADS, downloadsJson).apply()
    }

    private fun getDownloadDirectory(context: Context): File {
        val dir = File(context.filesDir, DOWNLOAD_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun getDownloadFilePath(context: Context, download: Download): String {
        val dir = getDownloadDirectory(context)
        val fileName = "${download.movieId}_${download.quality.name.lowercase()}.mp4"
        return File(dir, fileName).absolutePath
    }
}