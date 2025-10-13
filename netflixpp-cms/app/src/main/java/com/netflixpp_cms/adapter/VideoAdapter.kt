package com.netflixpp_cms.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.netflixpp_cms.R
import com.netflixpp_cms.model.Video

class VideoAdapter(
    private val videos: List<Video>,
    private val onDeleteClick: (Video) -> Unit
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvGenre: TextView = itemView.findViewById(R.id.tvGenre)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        private val btnDelete: TextView = itemView.findViewById(R.id.btnDelete)

        fun bind(video: Video) {
            tvTitle.text = video.title
            tvGenre.text = video.genre
            tvDuration.text = "${video.duration} seconds"

            btnDelete.setOnClickListener {
                onDeleteClick(video)
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