package com.netflixpp_streaming.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.netflixpp_streaming.R
import com.netflixpp_streaming.adapter.DownloadAdapter
import com.netflixpp_streaming.databinding.ActivityDownloadsBinding
import com.netflixpp_streaming.model.Download
import com.netflixpp_streaming.model.DownloadStatus
import com.netflixpp_streaming.service.DownloadService
import com.netflixpp_streaming.util.DownloadManager

class DownloadsActivity : AppCompatActivity(), DownloadManager.DownloadListener {

    private lateinit var binding: ActivityDownloadsBinding
    private lateinit var downloadAdapter: DownloadAdapter
    private val downloads = mutableListOf<Download>()
    
    private var downloadService: DownloadService? = null
    private var serviceBound = false
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as DownloadService.DownloadBinder
            downloadService = binder.getService()
            serviceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            downloadService = null
            serviceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        loadDownloads()
        bindDownloadService()
        
        // Register listener
        DownloadManager.addListener(this)
        
        // Cleanup expired downloads
        DownloadManager.cleanupExpiredDownloads(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        DownloadManager.removeListener(this)
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete_all -> {
                    showDeleteAllConfirmation()
                    true
                }
                R.id.action_sort -> {
                    showSortOptions()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        downloadAdapter = DownloadAdapter(
            downloads = downloads,
            onPlayClick = { download -> playDownload(download) },
            onPauseClick = { download -> pauseDownload(download) },
            onResumeClick = { download -> resumeDownload(download) },
            onCancelClick = { download -> cancelDownload(download) },
            onDeleteClick = { download -> deleteDownload(download) },
            onRetryClick = { download -> retryDownload(download) }
        )
        
        binding.rvDownloads.apply {
            layoutManager = LinearLayoutManager(this@DownloadsActivity)
            adapter = downloadAdapter
        }
    }

    private fun setupClickListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            loadDownloads()
        }
    }

    private fun bindDownloadService() {
        val intent = Intent(this, DownloadService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun loadDownloads() {
        downloads.clear()
        downloads.addAll(DownloadManager.getAllDownloads(this))
        updateUI()
        binding.swipeRefresh.isRefreshing = false
    }

    private fun updateUI() {
        if (downloads.isEmpty()) {
            showEmptyState()
        } else {
            showContent()
            updateStorageInfo()
        }
        
        downloadAdapter.notifyDataSetChanged()
    }

    private fun showEmptyState() {
        binding.rvDownloads.visibility = View.GONE
        binding.storageInfo.visibility = View.GONE
        binding.emptyStateView.visibility = View.VISIBLE
        binding.emptyStateView.showEmptyDownloads()
    }

    private fun showContent() {
        binding.rvDownloads.visibility = View.VISIBLE
        binding.storageInfo.visibility = View.VISIBLE
        binding.emptyStateView.visibility = View.GONE
    }

    private fun updateStorageInfo() {
        val totalSize = DownloadManager.getTotalDownloadSize(this)
        val availableSpace = DownloadManager.getAvailableSpace(this)
        
        val totalSizeGB = totalSize / (1024.0 * 1024.0 * 1024.0)
        val availableSpaceGB = availableSpace / (1024.0 * 1024.0 * 1024.0)
        
        binding.tvStorageUsed.text = String.format("%.2f GB used", totalSizeGB)
        binding.tvStorageAvailable.text = String.format("%.2f GB available", availableSpaceGB)
        
        // Update progress bar
        val usedPercentage = if (availableSpace > 0) {
            ((totalSize.toDouble() / (totalSize + availableSpace)) * 100).toInt()
        } else {
            0
        }
        binding.progressStorage.progress = usedPercentage
    }

    private fun playDownload(download: Download) {
        // Open movie player with local file
        val intent = Intent(this, MoviePlayerActivity::class.java).apply {
            putExtra("download_id", download.id)
            putExtra("local_file", download.localFilePath)
        }
        startActivity(intent)
    }

    private fun pauseDownload(download: Download) {
        downloadService?.pauseDownload(download.id)
    }

    private fun resumeDownload(download: Download) {
        downloadService?.resumeDownload(download.id)
    }

    private fun cancelDownload(download: Download) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.cancel_download))
            .setMessage(getString(R.string.cancel_download_message, download.movieTitle))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                downloadService?.cancelDownload(download.id)
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun deleteDownload(download: Download) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_download))
            .setMessage(getString(R.string.delete_download_message, download.movieTitle))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                DownloadManager.deleteDownload(this, download.id)
                loadDownloads()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun retryDownload(download: Download) {
        downloadService?.resumeDownload(download.id)
    }

    private fun showDeleteAllConfirmation() {
        val completedDownloads = downloads.filter { it.status == DownloadStatus.COMPLETED }
        
        if (completedDownloads.isEmpty()) {
            return
        }
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_all_downloads))
            .setMessage(getString(R.string.delete_all_downloads_message, completedDownloads.size))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                completedDownloads.forEach { download ->
                    DownloadManager.deleteDownload(this, download.id)
                }
                loadDownloads()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showSortOptions() {
        val options = arrayOf(
            "Date (Newest first)",
            "Date (Oldest first)",
            "Title (A-Z)",
            "Title (Z-A)",
            "Size (Largest first)",
            "Size (Smallest first)",
            "Status"
        )
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.sort_by))
            .setItems(options) { _, which ->
                sortDownloads(which)
            }
            .show()
    }

    private fun sortDownloads(sortType: Int) {
        when (sortType) {
            0 -> downloads.sortByDescending { it.downloadStartTime }
            1 -> downloads.sortBy { it.downloadStartTime }
            2 -> downloads.sortBy { it.movieTitle }
            3 -> downloads.sortByDescending { it.movieTitle }
            4 -> downloads.sortByDescending { it.totalBytes }
            5 -> downloads.sortBy { it.totalBytes }
            6 -> downloads.sortBy { it.status }
        }
        downloadAdapter.notifyDataSetChanged()
    }

    // DownloadManager.DownloadListener implementation
    override fun onDownloadStarted(download: Download) {
        runOnUiThread {
            updateDownloadInList(download)
        }
    }

    override fun onDownloadProgress(download: Download) {
        runOnUiThread {
            updateDownloadInList(download)
        }
    }

    override fun onDownloadCompleted(download: Download) {
        runOnUiThread {
            updateDownloadInList(download)
            updateStorageInfo()
        }
    }

    override fun onDownloadFailed(download: Download) {
        runOnUiThread {
            updateDownloadInList(download)
        }
    }

    override fun onDownloadCancelled(download: Download) {
        runOnUiThread {
            loadDownloads()
        }
    }

    private fun updateDownloadInList(updatedDownload: Download) {
        val index = downloads.indexOfFirst { it.id == updatedDownload.id }
        if (index != -1) {
            downloads[index] = updatedDownload
            downloadAdapter.notifyItemChanged(index)
        }
    }
}