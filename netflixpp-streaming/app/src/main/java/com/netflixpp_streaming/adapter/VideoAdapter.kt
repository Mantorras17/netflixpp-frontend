package com.netflixpp_streaming.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.netflixpp_streaming.R
import com.netflixpp_streaming.model.Video
import com.netflixpp_streaming.util.VideoUtils

class VideoAdapter(
    private val videos: List<Video>,
    private val onVideoClick: (Video) -> Unit
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivThumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)

        fun bind(video: Video) {
            tvTitle.text = video.title
            tvDuration.text = VideoUtils.formatDuration(video.duration)

            // Load thumbnail using Coil
            VideoUtils.loadThumbnail(ivThumbnail, video.thumbnailUrl)

            itemView.setOnClickListener {
                onVideoClick(video)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videos[position])
    }

    override fun getItemCount(): Int = videos.size
}