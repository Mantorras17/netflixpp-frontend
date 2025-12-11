package com.netflixpp_streaming.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.netflixpp_streaming.R
import com.netflixpp_streaming.model.Movie
import com.netflixpp_streaming.util.MovieUtils

class MyListAdapter(
    private val movies: List<Movie>,
    private val onMovieClick: (Movie) -> Unit,
    private val onPlayClick: (Movie) -> Unit,
    private val onRemoveClick: (Movie) -> Unit
) : RecyclerView.Adapter<MyListAdapter.MyListViewHolder>() {

    inner class MyListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivThumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvYear: TextView = itemView.findViewById(R.id.tvYear)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        private val tvGenre: TextView = itemView.findViewById(R.id.tvGenre)
        private val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val btnPlay: MaterialButton = itemView.findViewById(R.id.btnPlay)
        private val btnInfo: MaterialButton = itemView.findViewById(R.id.btnInfo)
        private val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemove)

        fun bind(movie: Movie) {
            // Load thumbnail
            MovieUtils.loadThumbnail(ivThumbnail, movie.thumbnailUrl)

            // Set text fields
            tvTitle.text = movie.title
            tvYear.text = movie.year.toString()
            tvDuration.text = MovieUtils.formatDurationReadable(movie.duration)
            tvGenre.text = movie.genre
            tvRating.text = String.format("%.1f", movie.rating)
            tvDescription.text = movie.description

            // Click listeners
            itemView.setOnClickListener {
                onMovieClick(movie)
            }

            btnPlay.setOnClickListener {
                onPlayClick(movie)
            }

            btnInfo.setOnClickListener {
                onMovieClick(movie)
            }

            btnRemove.setOnClickListener {
                onRemoveClick(movie)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_list_movie, parent, false)
        return MyListViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyListViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    override fun getItemCount(): Int = movies.size
}