package com.netflixpp_streaming.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.netflixpp_streaming.R
import com.netflixpp_streaming.adapter.MeshPeerAdapter
import com.netflixpp_streaming.adapter.MeshTransferAdapter
import com.netflixpp_streaming.databinding.ActivityMeshBinding
import com.netflixpp_streaming.model.*
import com.netflixpp_streaming.service.MeshProtocol
import com.netflixpp_streaming.service.P2PTransferService
import com.netflixpp_streaming.util.Prefs
import java.util.*

class MeshActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMeshBinding
    private lateinit var peerAdapter: MeshPeerAdapter
    private lateinit var transferAdapter: MeshTransferAdapter
    
    private val peers = mutableListOf<MeshNode>()
    private val activeTransfers = mutableListOf<MeshTransfer>()
    
    private val updateHandler = Handler(Looper.getMainLooper())
    private val updateInterval = 1000L // Update every second
    
    private var isMeshEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeshBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerViews()
        setupClickListeners()
        loadMeshPreferences()
        startPeriodicUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPeriodicUpdates()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_settings -> {
                    showMeshSettings()
                    true
                }
                R.id.action_info -> {
                    showMeshInfo()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerViews() {
        // Peers list
        peerAdapter = MeshPeerAdapter(peers) { peer ->
            showPeerDetails(peer)
        }
        
        binding.rvPeers.apply {
            layoutManager = LinearLayoutManager(this@MeshActivity)
            adapter = peerAdapter
        }
        
        // Active transfers
        transferAdapter = MeshTransferAdapter(activeTransfers)
        
        binding.rvActiveTransfers.apply {
            layoutManager = LinearLayoutManager(this@MeshActivity)
            adapter = transferAdapter
        }
    }

    private fun setupClickListeners() {
        binding.switchMeshEnabled.setOnCheckedChangeListener { _, isChecked ->
            isMeshEnabled = isChecked
            saveMeshPreferences()
            
            if (isChecked) {
                startMeshNetwork()
            } else {
                stopMeshNetwork()
            }
        }
        
        binding.btnRefreshPeers.setOnClickListener {
            refreshPeers()
        }
        
        binding.swipeRefresh.setOnRefreshListener {
            refreshPeers()
        }
        
        binding.btnStartSharing.setOnClickListener {
            if (isMeshEnabled) {
                startSharing()
            } else {
                Toast.makeText(this, "Enable mesh network first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadMeshPreferences() {
        val user = Prefs.getUser(this)
        isMeshEnabled = user?.preferences?.meshEnabled ?: false
        binding.switchMeshEnabled.isChecked = isMeshEnabled
        
        if (isMeshEnabled) {
            startMeshNetwork()
        }
    }

    private fun saveMeshPreferences() {
        val user = Prefs.getUser(this) ?: return
        user.preferences?.meshEnabled = isMeshEnabled
        Prefs.saveUser(this, user)
    }

    private fun startMeshNetwork() {
        if (!MeshProtocol.isRunning()) {
            MeshProtocol.startMeshNode(this)
            updateMeshStatus(true)
            
            // Start discovery
            refreshPeers()
        }
    }

    private fun stopMeshNetwork() {
        if (MeshProtocol.isRunning()) {
            MeshProtocol.stopMeshNode()
            updateMeshStatus(false)
            
            peers.clear()
            activeTransfers.clear()
            peerAdapter.notifyDataSetChanged()
            transferAdapter.notifyDataSetChanged()
            
            updateUI()
        }
    }

    private fun refreshPeers() {
        if (!isMeshEnabled) {
            binding.swipeRefresh.isRefreshing = false
            return
        }
        
        binding.tvScanningStatus.visibility = View.VISIBLE
        binding.tvScanningStatus.text = "Scanning for peers..."
        
        // Simulate peer discovery (replace with actual mesh discovery)
        Handler(Looper.getMainLooper()).postDelayed({
            simulatePeerDiscovery()
            simulateActiveTransfers()
            
            binding.tvScanningStatus.visibility = View.GONE
            binding.swipeRefresh.isRefreshing = false
            
            updateUI()
        }, 2000)
    }

    private fun startSharing() {
        P2PTransferService.startSharing(this)
        Toast.makeText(this, "Started sharing movies with peers", Toast.LENGTH_SHORT).show()
        
        // Update button state
        binding.btnStartSharing.isEnabled = false
        binding.btnStartSharing.text = "Sharing Active"
    }

    private fun updateMeshStatus(isRunning: Boolean) {
        if (isRunning) {
            binding.tvMeshStatus.text = "Connected"
            binding.ivMeshStatusIndicator.setColorFilter(
                getColor(R.color.quality_excellent)
            )
        } else {
            binding.tvMeshStatus.text = "Disconnected"
            binding.ivMeshStatusIndicator.setColorFilter(
                getColor(R.color.text_secondary)
            )
        }
    }

    private fun updateUI() {
        // Peer count
        binding.tvPeerCount.text = "${peers.size} ${if (peers.size == 1) "peer" else "peers"} online"
        
        // Show/hide empty states
        if (peers.isEmpty()) {
            binding.rvPeers.visibility = View.GONE
            binding.emptyStatePeers.visibility = View.VISIBLE
            binding.emptyStatePeers.showEmptyState(
                title = "No Peers Found",
                message = "Make sure nearby devices have mesh enabled and are on the same network.",
                iconRes = R.drawable.ic_mesh_offline
            )
        } else {
            binding.rvPeers.visibility = View.VISIBLE
            binding.emptyStatePeers.visibility = View.GONE
            peerAdapter.notifyDataSetChanged()
        }
        
        // Active transfers
        if (activeTransfers.isEmpty()) {
            binding.transfersSection.visibility = View.GONE
        } else {
            binding.transfersSection.visibility = View.VISIBLE
            binding.tvTransferCount.text = "${activeTransfers.size} active ${if (activeTransfers.size == 1) "transfer" else "transfers"}"
            transferAdapter.notifyDataSetChanged()
        }
        
        // Network stats
        updateNetworkStats()
    }

    private fun updateNetworkStats() {
        val totalDataShared = peers.sumOf { it.dataShared }
        val totalDataReceived = peers.sumOf { it.dataReceived }
        val totalBandwidth = peers.sumOf { it.bandwidth }
        val availableMovies = peers.flatMap { it.availableMovies }.distinct().size
        
        val sharedMB = totalDataShared / (1024.0 * 1024.0)
        val receivedMB = totalDataReceived / (1024.0 * 1024.0)
        val bandwidthMBps = totalBandwidth / (1024.0 * 1024.0)
        
        binding.tvDataShared.text = String.format("%.2f MB", sharedMB)
        binding.tvDataReceived.text = String.format("%.2f MB", receivedMB)
        binding.tvTotalBandwidth.text = String.format("%.2f MB/s", bandwidthMBps)
        binding.tvAvailableMovies.text = "$availableMovies"
    }

    private fun showPeerDetails(peer: MeshNode) {
        val message = buildString {
            append("Device: ${peer.deviceName}\n")
            append("IP: ${peer.ipAddress}:${peer.port}\n")
            append("Type: ${peer.deviceType}\n\n")
            append("Connection Quality: ${peer.connectionQuality}\n")
            append("Latency: ${peer.getFormattedLatency()}\n")
            append("Bandwidth: ${peer.getFormattedBandwidth()}\n\n")
            append("Available Movies: ${peer.availableMovies.size}\n")
            append("Data Shared: ${peer.getFormattedDataShared()}\n")
            append("Data Received: ${peer.getFormattedDataReceived()}\n\n")
            append("Connection Time: ${peer.getConnectionTimeMinutes()}m\n")
            
            peer.batteryLevel?.let {
                append("Battery: $it%")
                if (peer.isCharging) append(" (Charging)")
                append("\n")
            }
            
            append("Signal Strength: ${peer.signalStrength}%")
        }
        
        AlertDialog.Builder(this)
            .setTitle("Peer Details")
            .setMessage(message)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun showMeshSettings() {
        // Show mesh configuration dialog
        val options = arrayOf(
            "Max Connections: ${Prefs.getUser(this)?.preferences?.maxMeshConnections ?: 5}",
            "Auto-connect to peers",
            "Share downloaded movies",
            "Background mesh discovery"
        )
        
        AlertDialog.Builder(this)
            .setTitle("Mesh Settings")
            .setItems(options) { _, which ->
                // Handle settings selection
                when (which) {
                    0 -> showMaxConnectionsDialog()
                    // ... other settings
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showMaxConnectionsDialog() {
        val options = arrayOf("3", "5", "10", "15", "Unlimited")
        val currentMax = Prefs.getUser(this)?.preferences?.maxMeshConnections ?: 5
        val selectedIndex = when (currentMax) {
            3 -> 0
            5 -> 1
            10 -> 2
            15 -> 3
            else -> 4
        }
        
        AlertDialog.Builder(this)
            .setTitle("Max Mesh Connections")
            .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
                val newMax = when (which) {
                    0 -> 3
                    1 -> 5
                    2 -> 10
                    3 -> 15
                    else -> 999
                }
                
                val user = Prefs.getUser(this)
                user?.preferences?.maxMeshConnections = newMax
                user?.let { Prefs.saveUser(this, it) }
                
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMeshInfo() {
        val message = """
            Mesh networking allows you to stream movies directly from nearby devices without using server bandwidth.
            
            Benefits:
            • Faster streaming from local peers
            • Reduced server load
            • Works with limited internet
            • Share and discover content
            
            Requirements:
            • All devices on same WiFi network
            • Mesh feature enabled
            • Sufficient battery level
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle("About Mesh Network")
            .setMessage(message)
            .setPositiveButton("Got it", null)
            .show()
    }

    private fun startPeriodicUpdates() {
        updateHandler.post(object : Runnable {
            override fun run() {
                if (isMeshEnabled) {
                    updateActiveTransfers()
                    updateNetworkStats()
                }
                updateHandler.postDelayed(this, updateInterval)
            }
        })
    }

    private fun stopPeriodicUpdates() {
        updateHandler.removeCallbacksAndMessages(null)
    }

    private fun updateActiveTransfers() {
        // Update progress for active transfers
        activeTransfers.forEach { transfer ->
            if (transfer.progress < 100) {
                transfer.progress = minOf(100, transfer.progress + (1..5).random())
                transfer.bytesTransferred = (transfer.totalBytes * transfer.progress) / 100
                
                val elapsed = (System.currentTimeMillis() - transfer.startTime) / 1000
                if (elapsed > 0) {
                    transfer.transferRate = transfer.bytesTransferred / elapsed
                    val remaining = transfer.totalBytes - transfer.bytesTransferred
                    transfer.estimatedTimeRemaining = if (transfer.transferRate > 0) {
                        remaining / transfer.transferRate
                    } else {
                        0
                    }
                }
            }
        }
        
        // Remove completed transfers
        activeTransfers.removeAll { it.progress >= 100 }
        
        transferAdapter.notifyDataSetChanged()
        updateUI()
    }

    // Simulation methods (replace with actual mesh protocol calls)
    private fun simulatePeerDiscovery() {
        peers.clear()
        
        val deviceNames = listOf("John's Phone", "Sarah's Tablet", "Living Room TV", "Bob's Laptop", "Gaming PC")
        val deviceTypes = listOf(MeshNode.DeviceType.MOBILE, MeshNode.DeviceType.TABLET, MeshNode.DeviceType.TV, MeshNode.DeviceType.MOBILE, MeshNode.DeviceType.DESKTOP)
        
        repeat((1..5).random()) { i ->
            val latency = (10L..200L).random()
            val bandwidth = (1_000_000L..50_000_000L).random() // 1-50 MB/s
            
            val quality = when {
                latency < 50 && bandwidth > 10_000_000 -> MeshNode.ConnectionQuality.EXCELLENT
                latency < 100 && bandwidth > 5_000_000 -> MeshNode.ConnectionQuality.GOOD
                latency < 200 && bandwidth > 2_000_000 -> MeshNode.ConnectionQuality.FAIR
                else -> MeshNode.ConnectionQuality.POOR
            }
            
            val peer = MeshNode(
                id = UUID.randomUUID().toString(),
                ipAddress = "192.168.1.${100 + i}",
                port = 8088,
                deviceName = deviceNames.getOrElse(i) { "Device $i" },
                deviceType = deviceTypes.getOrElse(i) { MeshNode.DeviceType.UNKNOWN },
                availableMovies = List((5..20).random()) { UUID.randomUUID().toString() },
                bandwidth = bandwidth,
                latency = latency,
                connectionQuality = quality,
                isSharing = (0..1).random() == 1,
                uploadSpeed = (500_000L..5_000_000L).random(),
                downloadSpeed = (1_000_000L..10_000_000L).random(),
                dataShared = (0L..1_000_000_000L).random(),
                dataReceived = (0L..500_000_000L).random(),
                connectedAt = System.currentTimeMillis() - (60_000L..3_600_000L).random(),
                batteryLevel = (20..100).random(),
                isCharging = (0..1).random() == 1,
                signalStrength = (50..100).random(),
                isActive = true
            )
            
            peers.add(peer)
        }
    }

    private fun simulateActiveTransfers() {
        activeTransfers.clear()
        
        if (peers.isNotEmpty()) {
            repeat((0..2).random()) {
                val peer = peers.random()
                val isUpload = (0..1).random() == 1
                
                val transfer = MeshTransfer(
                    id = UUID.randomUUID().toString(),
                    movieId = UUID.randomUUID().toString(),
                    movieTitle = listOf("The Matrix", "Inception", "Interstellar", "Avatar").random(),
                    peerId = peer.id,
                    peerName = peer.deviceName,
                    chunkId = (1..20).random(),
                    totalChunks = 20,
                    progress = (10..70).random(),
                    bytesTransferred = 0,
                    totalBytes = (50_000_000L..200_000_000L).random(),
                    transferRate = peer.bandwidth / 2,
                    isUpload = isUpload
                )
                
                activeTransfers.add(transfer)
            }
        }
    }
}