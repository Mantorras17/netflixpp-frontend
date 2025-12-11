package com.netflixpp_cms.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.netflixpp_cms.R
import com.netflixpp_cms.model.LogEntry

class LogAdapter(
    private val logs: List<LogEntry>
) : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    inner class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val tvLevel: TextView = itemView.findViewById(R.id.tvLevel)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)

        fun bind(log: LogEntry) {
            tvTimestamp.text = log.timestamp
            tvLevel.text = log.level
            tvMessage.text = log.message

            // Color code by log level
            val levelColor = when (log.level.uppercase()) {
                "ERROR" -> Color.RED
                "WARN", "WARNING" -> Color.rgb(255, 165, 0)
                "INFO" -> Color.BLUE
                "DEBUG" -> Color.GRAY
                else -> Color.BLACK
            }
            tvLevel.setTextColor(levelColor)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_log, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(logs[position])
    }

    override fun getItemCount(): Int = logs.size
}