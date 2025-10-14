package com.netflixpp_streaming.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.netflixpp_streaming.R
import com.netflixpp_streaming.adapter.CategoryAdapter
import com.netflixpp_streaming.api.ApiClient
import com.netflixpp_streaming.databinding.ActivityMainBinding
import com.netflixpp_streaming.model.Category
import com.netflixpp_streaming.model.Video
import com.netflixpp_streaming.util.Prefs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private val categories = mutableListOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupRecyclerView()
        setupClickListeners()
        loadVideos()
    }

    private fun setupUI() {
        val user = Prefs.getUser(this)
        binding.tvWelcome.text = "Welcome, ${user?.username ?: "User"}"
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(categories) { video ->
            openVideoPlayer(video)
        }

        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = categoryAdapter
        }
    }

    private fun setupClickListeners() {
        binding.navigationBar.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_browse -> {
                    // Already on browse
                    true
                }
                R.id.nav_my_list -> {
                    Toast.makeText(this, "My List - Coming Soon", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_mesh -> {
                    startActivity(Intent(this, MeshActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    showProfileMenu()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadVideos() {
        binding.progressBar.visibility = android.view.View.VISIBLE

        ApiClient.getApiService(this).getVideos().enqueue(object : Callback<List<Video>> {
            override fun onResponse(call: Call<List<Video>>, response: Response<List<Video>>) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.swipeRefresh.isRefreshing = false

                if (response.isSuccessful) {
                    val videos = response.body() ?: emptyList()
                    organizeVideosByCategory(videos)
                } else {
                    Toast.makeText(this@MainActivity, "Error loading videos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Video>>, t: Throwable) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(this@MainActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun organizeVideosByCategory(videos: List<Video>) {
        categories.clear()

        // Group by genre
        val genreGroups = videos.groupBy { it.genre }

        genreGroups.forEach { (genre, genreVideos) ->
            categories.add(Category(genre, genreVideos))
        }

        // Add trending category
        if (videos.isNotEmpty()) {
            val trendingVideos = videos.take(10)
            categories.add(0, Category("Trending Now", trendingVideos))
        }

        categoryAdapter.notifyDataSetChanged()
    }

    private fun openVideoPlayer(video: Video) {
        val intent = Intent(this, VideoPlayerActivity::class.java).apply {
            putExtra("video", video)
        }
        startActivity(intent)
    }

    private fun showProfileMenu() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Profile")
            .setItems(arrayOf("Settings", "Logout")) { dialog, which ->
                when (which) {
                    0 -> Toast.makeText(this, "Settings - Coming Soon", Toast.LENGTH_SHORT).show()
                    1 -> {
                        Prefs.clearUserData(this)
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                }
            }
            .show()
    }
}