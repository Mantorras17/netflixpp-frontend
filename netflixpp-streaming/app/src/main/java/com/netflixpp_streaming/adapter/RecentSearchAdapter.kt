package com.netflixpp_streaming.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.netflixpp_streaming.R

class RecentSearchAdapter(
    private val searches: List<String>,
    private val onSearchClick: (String) -> Unit,
    private val onDeleteClick: ((String) -> Unit)?
) : RecyclerView.Adapter<RecentSearchAdapter.RecentSearchViewHolder>() {

    inner class RecentSearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
        private val tvSearchQuery: TextView = itemView.findViewById(R.id.tvSearchQuery)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(query: String) {
            tvSearchQuery.text = query

            // Show/hide delete button
            btnDelete.visibility = if (onDeleteClick != null) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                onSearchClick(query)
            }

            btnDelete.setOnClickListener {
                onDeleteClick?.invoke(query)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_search, parent, false)
        return RecentSearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentSearchViewHolder, position: Int) {
        holder.bind(searches[position])
    }

    override fun getItemCount(): Int = searches.size
}