package com.netflixpp_cms.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.netflixpp_cms.api.ApiClient
import com.netflixpp_cms.databinding.ActivityStorageBinding
import com.netflixpp_cms.model.CleanupResponse
import com.netflixpp_cms.model.StorageInfo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StorageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStorageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStorageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        loadStorageInfo()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnRefresh.setOnClickListener {
            loadStorageInfo()
        }

        binding.btnCleanup.setOnClickListener {
            performCleanup()
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadStorageInfo()
        }
    }

    private fun loadStorageInfo() {
        binding.progressBar.visibility = android.view.View.VISIBLE

        ApiClient.getApiService(this).getStorageInfo().enqueue(object : Callback<StorageInfo> {
            override fun onResponse(call: Call<StorageInfo>, response: Response<StorageInfo>) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.swipeRefresh.isRefreshing = false

                if (response.isSuccessful) {
                    response.body()?.let { storage ->
                        displayStorageInfo(storage)
                    }
                } else {
                    Toast.makeText(this@StorageActivity, "Failed to load storage info", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StorageInfo>, t: Throwable) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(this@StorageActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayStorageInfo(storage: StorageInfo) {
        val moviesGB = storage.moviesSize / (1024.0 * 1024.0 * 1024.0)
        val chunksGB = storage.chunksSize / (1024.0 * 1024.0 * 1024.0)
        val totalGB = storage.totalSize / (1024.0 * 1024.0 * 1024.0)
        val usagePercentage = if (totalGB > 0) ((storage.totalSize.toDouble() / storage.totalSize.toDouble()) * 100).toInt() else 0

        binding.tvTotalSpace.text = String.format("Total: %.2f GB", totalGB)
        binding.tvUsedSpace.text = String.format("Movies: %.2f GB | Chunks: %.2f GB", moviesGB, chunksGB)
        binding.tvFreeSpace.text = "Free: 56.78 GB" // Placeholder
        binding.tvUsagePercentage.text = "$usagePercentage%"
        binding.progressStorage.progress = usagePercentage

        binding.tvMovieCount.text = "Movies: ${storage.moviesCount}"
        binding.tvChunkCount.text = "Chunks: ${storage.chunksCount}"

        // Set progress bar color based on usage
        val color = when {
            usagePercentage < 70 -> android.graphics.Color.GREEN
            usagePercentage < 90 -> android.graphics.Color.rgb(255, 165, 0) // Orange
            else -> android.graphics.Color.RED
        }
        binding.progressStorage.progressDrawable.setTint(color)
    }

    private fun performCleanup() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Cleanup Storage")
            .setMessage("This will remove temporary files and unused chunks. Continue?")
            .setPositiveButton("Cleanup") { _, _ ->
                executeCleanup()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun executeCleanup() {
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.btnCleanup.isEnabled = false

        ApiClient.getApiService(this).cleanupSystem().enqueue(object : Callback<CleanupResponse> {
            override fun onResponse(call: Call<CleanupResponse>, response: Response<CleanupResponse>) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnCleanup.isEnabled = true

                if (response.isSuccessful) {
                    response.body()?.let { result ->
                        val freedMB = result.spaceFreed / (1024.0 * 1024.0)
                        androidx.appcompat.app.AlertDialog.Builder(this@StorageActivity)
                            .setTitle("Cleanup Complete")
                            .setMessage(
                                "Files removed: ${result.filesRemoved}\n" +
                                "Space freed: ${String.format("%.2f", freedMB)} MB"
                            )
                            .setPositiveButton("OK") { _, _ ->
                                loadStorageInfo()
                            }
                            .show()
                    }
                } else {
                    Toast.makeText(this@StorageActivity, "Cleanup failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CleanupResponse>, t: Throwable) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnCleanup.isEnabled = true
                Toast.makeText(this@StorageActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}