package com.netflixpp_streaming.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.netflixpp_streaming.R
import com.netflixpp_streaming.databinding.ItemDownloadBinding
import com.netflixpp_streaming.model.Download
import com.netflixpp_streaming.model.DownloadStatus
import com.netflixpp_streaming.util.MovieUtils

class DownloadAdapter(
    private val downloads: List<Download>,
    private val onPlayClick: (Download) -> Unit,
    private val onPauseClick: (Download) -> Unit,
    private val onResumeClick: (Download) -> Unit,
    private val onCancelClick: (Download) -> Unit,
    private val onDeleteClick: (Download) -> Unit,
    private val onRetryClick: (Download) -> Unit
) : RecyclerView.Adapter<DownloadAdapter.DownloadViewHolder>() {

    inner class DownloadViewHolder(private val binding: ItemDownloadBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(download: Download) {
            binding.tvTitle.text = download.movieTitle
            binding.tvQuality.text = download.quality.displayName
            binding.tvSize.text = download.getFormattedSize()
            
            // Load thumbnail
            MovieUtils.loadThumbnail(binding.ivThumbnail, download.thumbnailUrl)
            
            // Update UI based on status
            when (download.status) {
                DownloadStatus.PENDING -> {
                    showPendingState(download)
                }
                DownloadStatus.DOWNLOADING -> {
                    showDownloadingState(download)
                }
                DownloadStatus.PAUSED -> {
                    showPausedState(download)
                }
                DownloadStatus.COMPLETED -> {
                    showCompletedState(download)
                }
                DownloadStatus.FAILED -> {
                    showFailedState(download)
                }
                DownloadStatus.CANCELLED -> {
                    showCancelledState(download)
                }
            }
            
            // Setup click listeners
            binding.btnPlay.setOnClickListener { onPlayClick(download) }
            binding.btnPause.setOnClickListener { onPauseClick(download) }
            binding.btnResume.setOnClickListener { onResumeClick(download) }
            binding.btnCancel.setOnClickListener { onCancelClick(download) }
            binding.btnDelete.setOnClickListener { onDeleteClick(download) }
            binding.btnRetry.setOnClickListener { onRetryClick(download) }
        }

        private fun showPendingState(download: Download) {
            binding.progressDownload.visibility = View.VISIBLE
            binding.progressDownload.isIndeterminate = true
            binding.tvStatus.text = "Pending..."
            binding.tvProgress.visibility = View.GONE
            binding.tvSpeed.visibility = View.GONE
            
            binding.btnPlay.visibility = View.GONE
            binding.btnPause.visibility = View.GONE
            binding.btnResume.visibility = View.GONE
            binding.btnCancel.visibility = View.VISIBLE
            binding.btnDelete.visibility = View.GONE
            binding.btnRetry.visibility = View.GONE
            binding.tvExpiry.visibility = View.GONE
        }

        private fun showDownloadingState(download: Download) {
            binding.progressDownload.visibility = View.VISIBLE
            binding.progressDownload.isIndeterminate = false
            binding.progressDownload.progress = download.progress
            
            binding.tvStatus.text = "Downloading"
            binding.tvProgress.visibility = View.VISIBLE
            binding.tvProgress.text = "${download.progress}% • ${download.getFormattedDownloadedSize()} / ${download.getFormattedSize()}"
            
            binding.tvSpeed.visibility = View.VISIBLE
            binding.tvSpeed.text = "${download.getFormattedSpeed()} • ${download.getFormattedTimeRemaining()} remaining"
            
            binding.btnPlay.visibility = View.GONE
            binding.btnPause.visibility = View.VISIBLE
            binding.btnResume.visibility = View.GONE
            binding.btnCancel.visibility = View.VISIBLE
            binding.btnDelete.visibility = View.GONE
            binding.btnRetry.visibility = View.GONE
            binding.tvExpiry.visibility = View.GONE
        }

        private fun showPausedState(download: Download) {
            binding.progressDownload.visibility = View.VISIBLE
            binding.progressDownload.isIndeterminate = false
            binding.progressDownload.progress = download.progress
            
            binding.tvStatus.text = "Paused"
            binding.tvProgress.visibility = View.VISIBLE
            binding.tvProgress.text = "${download.progress}% • ${download.getFormattedDownloadedSize()} / ${download.getFormattedSize()}"
            binding.tvSpeed.visibility = View.GONE
            
            binding.btnPlay.visibility = View.GONE
            binding.btnPause.visibility = View.GONE
            binding.btnResume.visibility = View.VISIBLE
            binding.btnCancel.visibility = View.VISIBLE
            binding.btnDelete.visibility = View.GONE
            binding.btnRetry.visibility = View.GONE
            binding.tvExpiry.visibility = View.GONE
        }

        private fun showCompletedState(download: Download) {
            binding.progressDownload.visibility = View.GONE
            binding.tvStatus.text = "Downloaded"
            binding.tvProgress.visibility = View.GONE
            binding.tvSpeed.visibility = View.GONE
            
            binding.btnPlay.visibility = View.VISIBLE
            binding.btnPause.visibility = View.GONE
            binding.btnResume.visibility = View.GONE
            binding.btnCancel.visibility = View.GONE
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnRetry.visibility = View.GONE
            
            // Show expiry time
            if (download.isExpired()) {
                binding.tvExpiry.visibility = View.VISIBLE
                binding.tvExpiry.text = "Expired"
                binding.tvExpiry.setTextColor(itemView.context.getColor(R.color.error))
            } else {
                val remainingHours = (download.getRemainingExpiryTime() ?: 0) / 3600
                binding.tvExpiry.visibility = View.VISIBLE
                binding.tvExpiry.text = "Expires in ${remainingHours}h"
                binding.tvExpiry.setTextColor(itemView.context.getColor(R.color.text_secondary))
            }
        }

        private fun showFailedState(download: Download) {
            binding.progressDownload.visibility = View.GONE
            binding.tvStatus.text = "Failed"
            binding.tvProgress.visibility = View.VISIBLE
            binding.tvProgress.text = download.error ?: "Download failed"
            binding.tvSpeed.visibility = View.GONE
            
            binding.btnPlay.visibility = View.GONE
            binding.btnPause.visibility = View.GONE
            binding.btnResume.visibility = View.GONE
            binding.btnCancel.visibility = View.GONE
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnRetry.visibility = View.VISIBLE
            binding.tvExpiry.visibility = View.GONE
        }

        private fun showCancelledState(download: Download) {
            binding.progressDownload.visibility = View.GONE
            binding.tvStatus.text = "Cancelled"
            binding.tvProgress.visibility = View.GONE
            binding.tvSpeed.visibility = View.GONE
            
            binding.btnPlay.visibility = View.GONE
            binding.btnPause.visibility = View.GONE
            binding.btnResume.visibility = View.GONE
            binding.btnCancel.visibility = View.GONE
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnRetry.visibility = View.VISIBLE
            binding.tvExpiry.visibility = View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewHolder {
        val binding = ItemDownloadBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DownloadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DownloadViewHolder, position: Int) {
        holder.bind(downloads[position])
    }

    override fun getItemCount() = downloads.size
}