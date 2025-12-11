package com.netflixpp_streaming.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.netflixpp_streaming.R

/**
 * Reusable loading state view
 */
class LoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val progressBar: ProgressBar
    private val tvLoadingMessage: TextView

    init {
        orientation = VERTICAL
        gravity = android.view.Gravity.CENTER
        setPadding(48, 48, 48, 48)

        LayoutInflater.from(context).inflate(R.layout.view_loading, this, true)

        progressBar = findViewById(R.id.progressBar)
        tvLoadingMessage = findViewById(R.id.tvLoadingMessage)
    }

    fun show(message: String = context.getString(R.string.loading)) {
        tvLoadingMessage.text = message
        visibility = android.view.View.VISIBLE
    }

    fun hide() {
        visibility = android.view.View.GONE
    }
}