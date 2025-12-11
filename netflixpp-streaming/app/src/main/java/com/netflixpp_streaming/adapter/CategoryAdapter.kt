package com.netflixpp_streaming.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.netflixpp_streaming.R
import com.netflixpp_streaming.model.Category
import com.netflixpp_streaming.model.Movie

class CategoryAdapter(
    private val categories: List<Category>,
    private val onMovieClick: (Movie, View) -> Unit  // Pass the view for transition
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategoryTitle: TextView = itemView.findViewById(R.id.tvCategoryTitle)
        private val rvMovies: RecyclerView = itemView.findViewById(R.id.rvMovies)

        fun bind(category: Category) {
            tvCategoryTitle.text = category.title

            // Pass the thumbnail view to parent for transition
            val movieAdapter = MovieAdapter(category.movies) { movie, thumbnailView ->
                onMovieClick(movie, thumbnailView)
            }
            
            rvMovies.apply {
                layoutManager = LinearLayoutManager(
                    itemView.context, 
                    LinearLayoutManager.HORIZONTAL, 
                    false
                )
                adapter = movieAdapter
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size
}