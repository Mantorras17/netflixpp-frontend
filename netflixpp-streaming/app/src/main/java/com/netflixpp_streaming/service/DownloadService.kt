package com.netflixpp_streaming.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.netflixpp_streaming.R
import com.netflixpp_streaming.activity.DownloadsActivity
import com.netflixpp_streaming.model.Download
import com.netflixpp_streaming.model.DownloadStatus
import com.netflixpp_streaming.util.DownloadManager
import kotlinx.coroutines.*

class DownloadService : Service() {

    private val binder = DownloadBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeDownloads = mutableMapOf<String, Job>()
    
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "downloads"
        private const val NOTIFICATION_ID = 1001
        
        const val ACTION_PAUSE = "com.netflixpp_streaming.PAUSE_DOWNLOAD"
        const val ACTION_RESUME = "com.netflixpp_streaming.RESUME_DOWNLOAD"
        const val ACTION_CANCEL = "com.netflixpp_streaming.CANCEL_DOWNLOAD"
        const val EXTRA_DOWNLOAD_ID = "download_id"
    }

    inner class DownloadBinder : Binder() {
        fun getService(): DownloadService = this@DownloadService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> {
                val downloadId = intent.getStringExtra(EXTRA_DOWNLOAD_ID)
                downloadId?.let { pauseDownload(it) }
            }
            ACTION_RESUME -> {
                val downloadId = intent.getStringExtra(EXTRA_DOWNLOAD_ID)
                downloadId?.let { resumeDownload(it) }
            }
            ACTION_CANCEL -> {
                val downloadId = intent.getStringExtra(EXTRA_DOWNLOAD_ID)
                downloadId?.let { cancelDownload(it) }
            }
        }
        return START_STICKY
    }

    fun startDownload(download: Download) {
        if (activeDownloads.containsKey(download.id)) return
        
        val job = serviceScope.launch {
            try {
                DownloadManager.updateDownloadStatus(this@DownloadService, download.id, DownloadStatus.DOWNLOADING)
                downloadFile(download)
            } catch (e: CancellationException) {
                DownloadManager.updateDownloadStatus(this@DownloadService, download.id, DownloadStatus.CANCELLED)
            } catch (e: Exception) {
                DownloadManager.updateDownloadError(this@DownloadService, download.id, e.message ?: "Unknown error")
            }
        }
        
        activeDownloads[download.id] = job
        showDownloadNotification(download)
    }

    private suspend fun downloadFile(download: Download) {
        // Simulate download with progress updates
        // In real implementation, use OkHttp or similar for actual file download
        val totalChunks = 100
        var downloadedChunks = 0
        
        while (downloadedChunks < totalChunks) {
            if (!activeDownloads.containsKey(download.id)) {
                // Download was cancelled or paused
                break
            }
            
            delay(100) // Simulate download time
            downloadedChunks++
            
            val progress = (downloadedChunks * 100) / totalChunks
            val downloadedBytes = (download.totalBytes * downloadedChunks) / totalChunks
            
            DownloadManager.updateDownloadProgress(
                this,
                download.id,
                progress,
                downloadedBytes
            )
            
            updateNotification(download.id, progress)
        }
        
        if (downloadedChunks == totalChunks) {
            completeDownload(download)
        }
    }

    private fun completeDownload(download: Download) {
        DownloadManager.completeDownload(this, download.id)
        activeDownloads.remove(download.id)
        showCompletedNotification(download)
    }

    fun pauseDownload(downloadId: String) {
        activeDownloads[downloadId]?.cancel()
        activeDownloads.remove(downloadId)
        DownloadManager.updateDownloadStatus(this, downloadId, DownloadStatus.PAUSED)
    }

    fun resumeDownload(downloadId: String) {
        val download = DownloadManager.getDownload(this, downloadId)
        download?.let { startDownload(it) }
    }

    fun cancelDownload(downloadId: String) {
        activeDownloads[downloadId]?.cancel()
        activeDownloads.remove(downloadId)
        DownloadManager.cancelDownload(this, downloadId)
        cancelNotification(downloadId)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Movie download notifications"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showDownloadNotification(download: Download) {
        val intent = Intent(this, DownloadsActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Downloading ${download.movieTitle}")
            .setContentText("0% complete")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, 0, false)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun updateNotification(downloadId: String, progress: Int) {
        val download = DownloadManager.getDownload(this, downloadId) ?: return
        
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Downloading ${download.movieTitle}")
            .setContentText("$progress% complete")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showCompletedNotification(download: Download) {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Download complete")
            .setContentText(download.movieTitle)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
        
        stopForeground(true)
    }

    private fun cancelNotification(downloadId: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancel(NOTIFICATION_ID)
        stopForeground(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}