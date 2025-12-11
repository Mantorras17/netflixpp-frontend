package com.netflixpp_cms.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.netflixpp_cms.R
import com.netflixpp_cms.model.Movie

class MovieAdapter(
    private val movies: List<Movie>,
    private val onDeleteClick: (Movie) -> Unit,
    private val onEditClick: (Movie) -> Unit,
    private val onDetailsClick: (Movie) -> Unit,
    private val onGenerateChunksClick: (Movie) -> Unit
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvGenre: TextView = itemView.findViewById(R.id.tvGenre)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        private val tvYear: TextView = itemView.findViewById(R.id.tvYear)
        private val btnDetails: Button = itemView.findViewById(R.id.btnDetails)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        private val btnChunks: Button = itemView.findViewById(R.id.btnChunks)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(movie: Movie) {
            tvTitle.text = movie.title
            tvGenre.text = "Genre: ${movie.genre}"
            tvDuration.text = "${movie.duration} min"
            tvYear.text = "Year: ${movie.year}"

            btnDetails.setOnClickListener { onDetailsClick(movie) }
            btnEdit.setOnClickListener { onEditClick(movie) }
            btnChunks.setOnClickListener { onGenerateChunksClick(movie) }
            btnDelete.setOnClickListener { onDeleteClick(movie) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    override fun getItemCount(): Int = movies.size
}