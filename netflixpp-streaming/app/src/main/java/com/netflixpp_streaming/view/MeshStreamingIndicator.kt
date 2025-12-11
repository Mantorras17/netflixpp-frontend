package com.netflixpp_streaming.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.netflixpp_streaming.R
import com.netflixpp_streaming.databinding.ViewMeshIndicatorBinding
import com.netflixpp_streaming.model.MeshNode

/**
 * Visual indicator showing mesh streaming status
 */
class MeshStreamingIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewMeshIndicatorBinding
    private var activePeers = 0
    private var totalBandwidth = 0L

    init {
        binding = ViewMeshIndicatorBinding.inflate(LayoutInflater.from(context), this, true)
        orientation = HORIZONTAL
        hide()
    }

    fun showMeshStreaming(peers: List<MeshNode>) {
        activePeers = peers.size
        totalBandwidth = peers.sumOf { it.bandwidth }
        
        binding.tvSource.text = "Mesh Network"
        binding.tvPeerCount.text = "$activePeers ${if (activePeers == 1) "peer" else "peers"}"
        
        val bandwidthMBps = totalBandwidth.toDouble() / (1024.0 * 1024.0)
        binding.tvSpeed.text = String.format("%.1f MB/s", bandwidthMBps)
        
        binding.iconSource.setColorFilter(context.getColor(R.color.mesh_active))
        binding.root.setBackgroundResource(R.drawable.bg_rounded_mesh)
        
        show()
    }

    fun showServerStreaming() {
        binding.tvSource.text = "Server"
        binding.tvPeerCount.text = "Direct"
        binding.tvSpeed.text = ""
        
        binding.iconSource.setColorFilter(context.getColor(R.color.text_secondary))
        binding.root.setBackgroundResource(R.drawable.bg_rounded_mesh)
        
        show()
    }

    fun updateProgress(bytesReceived: Long, totalBytes: Long, currentSpeed: Long) {
        val speedMBps = currentSpeed.toDouble() / (1024.0 * 1024.0)
        binding.tvSpeed.text = String.format("%.1f MB/s", speedMBps)
    }

    fun show() {
        visibility = VISIBLE
        animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(300)
            .start()
    }

    fun hide() {
        animate()
            .alpha(0f)
            .translationY(-50f)
            .setDuration(300)
            .withEndAction { visibility = GONE }
            .start()
    }
}