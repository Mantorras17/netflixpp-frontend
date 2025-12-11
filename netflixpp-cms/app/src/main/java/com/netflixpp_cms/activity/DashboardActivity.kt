package com.netflixpp_cms.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.netflixpp_cms.api.ApiClient
import com.netflixpp_cms.databinding.ActivityDashboardBinding
import com.netflixpp_cms.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        loadSystemStats()
        loadStorageInfo()
        checkSystemHealth()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnRefresh.setOnClickListener {
            loadSystemStats()
            loadStorageInfo()
            checkSystemHealth()
        }

        binding.cardStorage.setOnClickListener {
            startActivity(Intent(this, StorageActivity::class.java))
        }

        binding.cardLogs.setOnClickListener {
            startActivity(Intent(this, LogsActivity::class.java))
        }

        binding.btnCleanup.setOnClickListener {
            performCleanup()
        }

        binding.cardPeers.setOnClickListener {
            loadPeerInfo()
        }
    }

    private fun loadSystemStats() {
        binding.progressBar.visibility = android.view.View.VISIBLE

        ApiClient.getApiService(this).getSystemStats().enqueue(object : Callback<SystemStats> {
            override fun onResponse(call: Call<SystemStats>, response: Response<SystemStats>) {
                binding.progressBar.visibility = android.view.View.GONE

                if (response.isSuccessful) {
                    response.body()?.let { stats ->
                        binding.tvTotalUsers.text = "${stats.totalUsers ?: 0}"
                        binding.tvTotalMovies.text = "${stats.totalMovies ?: 0}"
                        binding.tvTotalViews.text = "${stats.totalViews ?: 0}"
                        binding.tvActiveStreams.text = "${stats.activeStreams ?: 0}"
                        
                        val uptimeHours = (stats.serverUptime ?: 0) / 3600000
                        binding.tvServerUptime.text = "${uptimeHours}h"
                    }
                } else {
                    Toast.makeText(this@DashboardActivity, "Failed to load stats", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SystemStats>, t: Throwable) {
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this@DashboardActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadStorageInfo() {
        ApiClient.getApiService(this).getStorageInfo().enqueue(object : Callback<StorageInfo> {
            override fun onResponse(call: Call<StorageInfo>, response: Response<StorageInfo>) {
                if (response.isSuccessful) {
                    response.body()?.let { storage ->
                        val usedGB = storage.usedSpace / (1024 * 1024 * 1024)
                        val totalGB = storage.totalSpace / (1024 * 1024 * 1024)
                        val percentage = if (totalGB > 0) (usedGB * 100 / totalGB) else 0
                        
                        binding.tvStorageUsed.text = "${usedGB} GB / ${totalGB} GB"
                        binding.tvStoragePercentage.text = "${percentage}%"
                        binding.progressStorage.progress = percentage.toInt()
                    }
                }
            }

            override fun onFailure(call: Call<StorageInfo>, t: Throwable) {
                // Silent fail for storage
            }
        })
    }

    private fun checkSystemHealth() {
        ApiClient.getApiService(this).healthCheck().enqueue(object : Callback<HealthResponse> {
            override fun onResponse(call: Call<HealthResponse>, response: Response<HealthResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { health ->
                        val isHealthy = health.status.equals("healthy", ignoreCase = true)
                        binding.tvHealthStatus.text = if (isHealthy) "✅ Healthy" else "❌ Unhealthy"
                        binding.tvDatabaseStatus.text = "DB: ${health.database ?: "Unknown"}"
                        binding.tvStorageStatus.text = "Storage: ${health.storage ?: "Unknown"}"
                    }
                } else {
                    binding.tvHealthStatus.text = "❌ Unhealthy"
                }
            }

            override fun onFailure(call: Call<HealthResponse>, t: Throwable) {
                binding.tvHealthStatus.text = "❌ Error"
            }
        })
    }

    private fun performCleanup() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("System Cleanup")
            .setMessage("This will remove temporary files and unused chunks. Continue?")
            .setPositiveButton("Cleanup") { _, _ ->
                executeCleanup()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun executeCleanup() {
        binding.progressBar.visibility = android.view.View.VISIBLE

        ApiClient.getApiService(this).cleanupSystem().enqueue(object : Callback<CleanupResponse> {
            override fun onResponse(call: Call<CleanupResponse>, response: Response<CleanupResponse>) {
                binding.progressBar.visibility = android.view.View.GONE

                if (response.isSuccessful) {
                    response.body()?.let { result ->
                        val spaceMB = result.spaceFreed / (1024 * 1024)
                        Toast.makeText(
                            this@DashboardActivity,
                            "Cleanup complete!\n${result.filesRemoved} files removed\n${spaceMB} MB freed",
                            Toast.LENGTH_LONG
                        ).show()
                        loadStorageInfo()
                    }
                } else {
                    Toast.makeText(this@DashboardActivity, "Cleanup failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CleanupResponse>, t: Throwable) {
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this@DashboardActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadPeerInfo() {
        ApiClient.getApiService(this).getActivePeers().enqueue(object : Callback<List<PeerInfo>> {
            override fun onResponse(call: Call<List<PeerInfo>>, response: Response<List<PeerInfo>>) {
                if (response.isSuccessful) {
                    response.body()?.let { peers ->
                        val peerInfo = peers.joinToString("\n") { "• ${it.peerId} (${it.address})" }
                        androidx.appcompat.app.AlertDialog.Builder(this@DashboardActivity)
                            .setTitle("Active Peers (${peers.size})")
                            .setMessage(if (peers.isEmpty()) "No active peers" else peerInfo)
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            }

            override fun onFailure(call: Call<List<PeerInfo>>, t: Throwable) {
                Toast.makeText(this@DashboardActivity, "Failed to load peers", Toast.LENGTH_SHORT).show()
            }
        })
    }
}