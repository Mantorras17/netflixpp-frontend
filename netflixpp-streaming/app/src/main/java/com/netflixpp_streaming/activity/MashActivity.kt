package com.netflixpp_streaming.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.netflixpp_streaming.model.MeshNode
import com.netflixpp_streaming.service.MeshProtocol
import com.netflixpp_streaming.databinding.ActivityMeshBinding
import com.netflixpp_streaming.service.P2PTransferService

class MeshActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMeshBinding
    private val meshNodes = mutableListOf<MeshNode>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeshBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupClickListeners()
        startMeshService()
    }

    private fun setupUI() {
        binding.rvPeers.layoutManager = LinearLayoutManager(this)
        updateMeshStatus()
    }

    private fun setupClickListeners() {
        binding.btnToggleMesh.setOnClickListener {
            toggleMeshNetwork()
        }

        binding.btnStartSharing.setOnClickListener {
            startSharingVideos()
        }

        binding.btnRefresh.setOnClickListener {
            refreshMeshNetwork()
        }

        binding.swipeRefresh.setOnRefreshListener {
            refreshMeshNetwork()
        }
    }

    private fun startMeshService() {
        if (!MeshProtocol.isRunning()) {
            MeshProtocol.startMeshNode(this)
            updateMeshStatus()
        }
    }

    private fun toggleMeshNetwork() {
        if (MeshProtocol.isRunning()) {
            MeshProtocol.stopMeshNode()
            binding.tvMeshStatus.text = "Mesh Disconnected"
            binding.btnToggleMesh.text = "Start Mesh"
            meshNodes.clear()
            updatePeersList()
        } else {
            MeshProtocol.startMeshNode(this)
            binding.tvMeshStatus.text = "Mesh Starting..."
            binding.btnToggleMesh.text = "Stop Mesh"

            // Simulate peer discovery
            simulatePeerDiscovery()
        }
    }

    private fun startSharingVideos() {
        if (MeshProtocol.isRunning()) {
            P2PTransferService.startSharing(this)
            Toast.makeText(this, "Started sharing videos with peers", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Start mesh network first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshMeshNetwork() {
        if (MeshProtocol.isRunning()) {
            binding.tvMeshStatus.text = "Scanning for peers..."
            binding.progressBar.visibility = android.view.View.VISIBLE

            // Simulate network scan
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                simulatePeerDiscovery()
                binding.progressBar.visibility = android.view.View.GONE
                binding.swipeRefresh.isRefreshing = false
            }, 2000)
        } else {
            binding.swipeRefresh.isRefreshing = false
            Toast.makeText(this, "Mesh network not running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun simulatePeerDiscovery() {
        meshNodes.clear()

        // Simulate discovered peers
        meshNodes.addAll(listOf(
            MeshNode("192.168.1.101", "User_Phone_1", 3, 1024L),
            MeshNode("192.168.1.102", "User_Tablet_2", 5, 2048L),
            MeshNode("192.168.1.103", "User_Laptop_3", 2, 512L)
        ))

        updatePeersList()
        updateMeshStatus()
    }

    private fun updatePeersList() {
        val adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
                val view = android.view.LayoutInflater.from(parent.context)
                    .inflate(android.R.layout.simple_list_item_2, parent, false)
                return object : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {}
            }

            override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
                val node = meshNodes[position]
                (holder.itemView as android.widget.TextView).text = "${node.deviceName}\nIP: ${node.ipAddress} - Videos: ${node.availableVideos}"
            }

            override fun getItemCount() = meshNodes.size
        }

        binding.rvPeers.adapter = adapter
    }

    private fun updateMeshStatus() {
        val isRunning = MeshProtocol.isRunning()
        val peerCount = meshNodes.size

        binding.tvMeshStatus.text = if (isRunning) {
            "Mesh Connected - Peers: $peerCount"
        } else {
            "Mesh Disconnected"
        }

        binding.btnToggleMesh.text = if (isRunning) "Stop Mesh" else "Start Mesh"
        binding.tvPeerCount.text = "Connected Peers: $peerCount"

        // Update stats
        val totalVideos = meshNodes.sumOf { it.availableVideos }
        val totalBandwidth = meshNodes.sumOf { it.bandwidth }
        binding.tvStats.text = "Total Available Videos: $totalVideos\nTotal Bandwidth: ${totalBandwidth}MB/s"
    }

    override fun onDestroy() {
        super.onDestroy()
        // Don't stop mesh service here to keep it running in background
    }
}