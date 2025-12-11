package com.netflixpp_streaming.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.netflixpp_streaming.R
import com.netflixpp_streaming.databinding.ItemMeshPeerBinding
import com.netflixpp_streaming.model.MeshNode

class MeshPeerAdapter(
    private val peers: List<MeshNode>,
    private val onPeerClick: (MeshNode) -> Unit
) : RecyclerView.Adapter<MeshPeerAdapter.PeerViewHolder>() {

    inner class PeerViewHolder(private val binding: ItemMeshPeerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(peer: MeshNode) {
            binding.tvDeviceName.text = peer.deviceName
            binding.tvIpAddress.text = peer.ipAddress
            
            // Device type icon
            val deviceIcon = when (peer.deviceType) {
                MeshNode.DeviceType.MOBILE -> R.drawable.ic_phone
                MeshNode.DeviceType.TABLET -> R.drawable.ic_tablet
                MeshNode.DeviceType.TV -> R.drawable.ic_tv
                MeshNode.DeviceType.DESKTOP -> R.drawable.ic_computer
                MeshNode.DeviceType.UNKNOWN -> R.drawable.ic_device
            }
            binding.ivDeviceType.setImageResource(deviceIcon)
            
            // Connection quality indicator
            val (qualityColor, qualityText) = when (peer.connectionQuality) {
                MeshNode.ConnectionQuality.EXCELLENT -> Pair(R.color.quality_excellent, "Excellent")
                MeshNode.ConnectionQuality.GOOD -> Pair(R.color.quality_good, "Good")
                MeshNode.ConnectionQuality.FAIR -> Pair(R.color.quality_fair, "Fair")
                MeshNode.ConnectionQuality.POOR -> Pair(R.color.quality_poor, "Poor")
                MeshNode.ConnectionQuality.UNKNOWN -> Pair(R.color.text_secondary, "Unknown")
            }
            
            binding.ivQualityIndicator.setColorFilter(
                ContextCompat.getColor(itemView.context, qualityColor)
            )
            binding.tvQuality.text = qualityText
            binding.tvQuality.setTextColor(
                ContextCompat.getColor(itemView.context, qualityColor)
            )
            
            // Stats
            binding.tvMovieCount.text = "${peer.availableMovies.size} movies"
            binding.tvBandwidth.text = peer.getFormattedBandwidth()
            binding.tvLatency.text = peer.getFormattedLatency()
            
            // Sharing status
            if (peer.isSharing) {
                binding.chipSharing.visibility = View.VISIBLE
                binding.tvDataShared.visibility = View.VISIBLE
                binding.tvDataShared.text = "Shared: ${peer.getFormattedDataShared()}"
            } else {
                binding.chipSharing.visibility = View.GONE
                binding.tvDataShared.visibility = View.GONE
            }
            
            // Connection time
            val minutes = peer.getConnectionTimeMinutes()
            binding.tvConnectionTime.text = if (minutes < 60) {
                "${minutes}m connected"
            } else {
                val hours = minutes / 60
                val mins = minutes % 60
                "${hours}h ${mins}m connected"
            }
            
            // Active indicator
            if (peer.isRecentlyActive()) {
                binding.ivActiveIndicator.visibility = View.VISIBLE
                binding.ivActiveIndicator.setColorFilter(
                    ContextCompat.getColor(itemView.context, R.color.quality_excellent)
                )
            } else {
                binding.ivActiveIndicator.visibility = View.GONE
            }
            
            // Battery level (if available)
            peer.batteryLevel?.let { level ->
                binding.batteryInfo.visibility = View.VISIBLE
                binding.tvBatteryLevel.text = "$level%"
                
                val batteryIcon = when {
                    peer.isCharging -> R.drawable.ic_battery_charging
                    level > 80 -> R.drawable.ic_battery_full
                    level > 50 -> R.drawable.ic_battery_high
                    level > 20 -> R.drawable.ic_battery_medium
                    else -> R.drawable.ic_battery_low
                }
                binding.ivBatteryIcon.setImageResource(batteryIcon)
            } ?: run {
                binding.batteryInfo.visibility = View.GONE
            }
            
            // Signal strength
            binding.progressSignal.progress = peer.signalStrength
            
            // Click listener
            binding.root.setOnClickListener {
                onPeerClick(peer)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeerViewHolder {
        val binding = ItemMeshPeerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PeerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PeerViewHolder, position: Int) {
        holder.bind(peers[position])
    }

    override fun getItemCount() = peers.size
}