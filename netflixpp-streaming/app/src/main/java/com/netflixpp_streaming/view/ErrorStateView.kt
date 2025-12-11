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

class ErrorStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val ivError: ImageView
    private val tvErrorTitle: TextView
    private val tvErrorMessage: TextView
    private val btnRetry: Button
    private val btnSecondary: Button

    private var onRetryClick: (() -> Unit)? = null
    private var onSecondaryClick: (() -> Unit)? = null

    init {
        orientation = VERTICAL
        gravity = android.view.Gravity.CENTER
        setPadding(48, 48, 48, 48)

        LayoutInflater.from(context).inflate(R.layout.view_error_state, this, true)

        ivError = findViewById(R.id.ivError)
        tvErrorTitle = findViewById(R.id.tvErrorTitle)
        tvErrorMessage = findViewById(R.id.tvErrorMessage)
        btnRetry = findViewById(R.id.btnRetry)
        btnSecondary = findViewById(R.id.btnSecondary)

        btnRetry.setOnClickListener {
            onRetryClick?.invoke()
        }

        btnSecondary.setOnClickListener {
            onSecondaryClick?.invoke()
        }
    }

    fun setOnRetryClickListener(listener: () -> Unit) {
        onRetryClick = listener
    }

    fun setOnSecondaryClickListener(listener: () -> Unit) {
        onSecondaryClick = listener
    }

    fun showNetworkError() {
        ivError.setImageResource(R.drawable.ic_no_wifi)
        tvErrorTitle.text = context.getString(R.string.error_no_internet_title)
        tvErrorMessage.text = context.getString(R.string.error_no_internet_message)
        btnRetry.visibility = View.VISIBLE
        btnRetry.text = context.getString(R.string.retry)
        btnSecondary.visibility = View.GONE
        visibility = View.VISIBLE
    }

    fun showServerError() {
        ivError.setImageResource(R.drawable.ic_server_error)
        tvErrorTitle.text = context.getString(R.string.error_server_title)
        tvErrorMessage.text = context.getString(R.string.error_server_message)
        btnRetry.visibility = View.VISIBLE
        btnRetry.text = context.getString(R.string.retry)
        btnSecondary.visibility = View.GONE
        visibility = View.VISIBLE
    }

    fun showError(
        title: String,
        message: String,
        iconRes: Int,
        showRetryButton: Boolean = true
    ) {
        ivError.setImageResource(iconRes)
        tvErrorTitle.text = title
        tvErrorMessage.text = message
        btnRetry.visibility = if (showRetryButton) View.VISIBLE else View.GONE
        btnRetry.text = context.getString(R.string.retry)
        btnSecondary.visibility = View.GONE
        visibility = View.VISIBLE
    }

    fun hide() {
        visibility = View.GONE
    }
}