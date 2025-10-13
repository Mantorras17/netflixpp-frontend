package com.netflixpp_cms.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.netflixpp_cms.adapter.VideoAdapter
import com.netflixpp_cms.api.ApiClient
import com.netflixpp_cms.databinding.ActivityVideoListBinding
import com.netflixpp_cms.model.ApiResponse
import com.netflixpp_cms.model.Video
import com.netflixpp_cms.util.Prefs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VideoListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoListBinding
    private lateinit var videoAdapter: VideoAdapter
    private val videoList = mutableListOf<Video>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupClickListeners()
        loadVideos()
    }

    private fun setupRecyclerView() {
        videoAdapter = VideoAdapter(videoList) { video ->
            deleteVideo(video)
        }

        binding.rvVideos.apply {
            layoutManager = LinearLayoutManager(this@VideoListActivity)
            adapter = videoAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadVideos()
        }
    }

    private fun loadVideos() {
        binding.progressBar.visibility = android.view.View.VISIBLE

        ApiClient.getApiService(this).getVideos().enqueue(object : Callback<ApiResponse<List<Video>>> {
            override fun onResponse(call: Call<ApiResponse<List<Video>>>, response: Response<ApiResponse<List<Video>>>) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.swipeRefresh.isRefreshing = false

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        videoList.clear()
                        apiResponse.data?.let { videos ->
                            videoList.addAll(videos)
                            videoAdapter.notifyDataSetChanged()
                        }
                    } else {
                        Toast.makeText(
                            this@VideoListActivity,
                            apiResponse?.error ?: "Failed to load videos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this@VideoListActivity, "Server error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<Video>>>, t: Throwable) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(this@VideoListActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteVideo(video: Video) {
        video.id?.let { videoId ->
            android.app.AlertDialog.Builder(this)
                .setTitle("Delete Video")
                .setMessage("Are you sure you want to delete '${video.title}'?")
                .setPositiveButton("Delete") { dialog, which ->
                    performDeleteVideo(videoId)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun performDeleteVideo(videoId: String) {
        ApiClient.getApiService(this).deleteVideo(videoId).enqueue(object : Callback<ApiResponse<Unit>> {
            override fun onResponse(call: Call<ApiResponse<Unit>>, response: Response<ApiResponse<Unit>>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        Toast.makeText(this@VideoListActivity, "Video deleted successfully", Toast.LENGTH_SHORT).show()
                        loadVideos()
                    } else {
                        Toast.makeText(
                            this@VideoListActivity,
                            apiResponse?.error ?: "Failed to delete video",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this@VideoListActivity, "Server error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<Unit>>, t: Throwable) {
                Toast.makeText(this@VideoListActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}