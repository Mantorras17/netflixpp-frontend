package com.netflixpp_streaming.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.netflixpp_streaming.R
import com.netflixpp_streaming.databinding.ItemMeshTransferBinding
import com.netflixpp_streaming.model.MeshTransfer

class MeshTransferAdapter(
    private val transfers: List<MeshTransfer>
) : RecyclerView.Adapter<MeshTransferAdapter.TransferViewHolder>() {

    inner class TransferViewHolder(private val binding: ItemMeshTransferBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transfer: MeshTransfer) {
            binding.tvMovieTitle.text = transfer.movieTitle
            binding.tvPeerId.text = if (transfer.isUpload) {
                "Uploading to ${transfer.peerName}"
            } else {
                "Downloading from ${transfer.peerName}"
            }
            
            // Direction icon
            binding.iconStatus.setImageResource(
                if (transfer.isUpload) R.drawable.ic_upload else R.drawable.ic_download
            )
            
            // Progress
            binding.progressTransfer.progress = transfer.progress
            binding.tvProgress.text = "${transfer.progress}%"
            
            // Transfer details
            binding.tvTransferred.text = "${transfer.getFormattedTransferred()} / ${transfer.getFormattedSize()}"
            binding.tvSpeed.text = transfer.getFormattedTransferRate()
            binding.tvElapsed.text = transfer.getFormattedTimeRemaining()
            
            // Status
            binding.tvStatus.text = if (transfer.isUpload) "Uploading" else "Downloading"
            binding.tvStatus.setTextColor(
                itemView.context.getColor(R.color.mesh_primary)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransferViewHolder {
        val binding = ItemMeshTransferBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransferViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransferViewHolder, position: Int) {
        holder.bind(transfers[position])
    }

    override fun getItemCount() = transfers.size
}