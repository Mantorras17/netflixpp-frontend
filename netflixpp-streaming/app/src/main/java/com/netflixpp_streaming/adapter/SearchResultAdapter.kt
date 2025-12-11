package com.netflixpp_streaming.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.netflixpp_streaming.R
import com.netflixpp_streaming.model.Movie
import com.netflixpp_streaming.util.MovieUtils

class SearchResultAdapter(
    private val movies: List<Movie>,
    private val onMovieClick: (Movie) -> Unit
) : RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder>() {

    inner class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivThumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvYear: TextView = itemView.findViewById(R.id.tvYear)
        private val tvGenre: TextView = itemView.findViewById(R.id.tvGenre)
        private val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)

        fun bind(movie: Movie) {
            MovieUtils.loadThumbnail(ivThumbnail, movie.thumbnailUrl)
            tvTitle.text = movie.title
            tvYear.text = movie.year.toString()
            tvGenre.text = movie.genre
            tvRating.text = String.format("%.1f", movie.rating)
            tvDescription.text = movie.description

            itemView.setOnClickListener {
                onMovieClick(movie)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return SearchResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    override fun getItemCount(): Int = movies.size
}