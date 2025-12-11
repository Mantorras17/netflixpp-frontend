package com.netflixpp_streaming.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.netflixpp_streaming.R

class EmptyStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val ivEmpty: ImageView
    private val tvEmptyTitle: TextView
    private val tvEmptyMessage: TextView
    private val btnAction: Button

    private var onActionClick: (() -> Unit)? = null

    init {
        orientation = VERTICAL
        gravity = android.view.Gravity.CENTER
        setPadding(48, 48, 48, 48)

        LayoutInflater.from(context).inflate(R.layout.view_empty_state, this, true)

        ivEmpty = findViewById(R.id.ivEmpty)
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle)
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage)
        btnAction = findViewById(R.id.btnAction)

        btnAction.setOnClickListener {
            onActionClick?.invoke()
        }
    }

    fun setOnActionClickListener(listener: () -> Unit) {
        onActionClick = listener
    }

    fun showNoMovies() {
        ivEmpty.setImageResource(R.drawable.ic_movie_library)
        tvEmptyTitle.text = context.getString(R.string.empty_no_movies_title)
        tvEmptyMessage.text = context.getString(R.string.empty_no_movies_message)
        btnAction.visibility = View.VISIBLE
        btnAction.text = context.getString(R.string.browse_movies)
        visibility = View.VISIBLE
    }

    fun showEmptyDownloads() {
        ivEmpty.setImageResource(R.drawable.ic_download)
        tvEmptyTitle.text = context.getString(R.string.empty_downloads_title)
        tvEmptyMessage.text = context.getString(R.string.empty_downloads_message)
        btnAction.visibility = View.VISIBLE
        btnAction.text = context.getString(R.string.browse_movies)
        visibility = View.VISIBLE
    }

    fun showEmptyState(
        title: String,
        message: String,
        iconRes: Int,
        actionText: String? = null
    ) {
        ivEmpty.setImageResource(iconRes)
        tvEmptyTitle.text = title
        tvEmptyMessage.text = message
        btnAction.visibility = if (actionText != null) View.VISIBLE else View.GONE
        btnAction.text = actionText ?: ""
        visibility = View.VISIBLE
    }

    fun hide() {
        visibility = View.GONE
    }
}