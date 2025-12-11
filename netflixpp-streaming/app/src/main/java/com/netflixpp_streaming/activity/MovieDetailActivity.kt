package com.netflixpp_streaming.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.netflixpp_streaming.R
import com.netflixpp_streaming.api.ApiClient
import com.netflixpp_streaming.databinding.ActivityMovieDetailBinding
import com.netflixpp_streaming.model.ApiResponse
import com.netflixpp_streaming.model.Movie
import com.netflixpp_streaming.service.MeshProtocol
import com.netflixpp_streaming.util.Prefs
import com.netflixpp_streaming.util.MovieUtils
import com.netflixpp_streaming.model.DownloadQuality
import com.netflixpp_streaming.service.DownloadService
import com.netflixpp_streaming.util.DownloadManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class MovieDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieDetailBinding
    private lateinit var currentMovie: Movie
    private var isInMyList = false
    private var selectedQuality = "auto" // auto, 1080p, 360p, mesh

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set transition name to match the one from previous activity
        binding.ivHeroImage.transitionName = "movie_thumbnail"

        // Get movie from intent
        currentMovie = intent.getSerializableExtra("movie") as? Movie ?: run {
            Toast.makeText(this, "Error loading movie", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupUI()
        setupClickListeners()
        checkMyListStatus()
        checkMeshAvailability()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        
        binding.toolbar.setNavigationOnClickListener {
            // Support reverse transition
            supportFinishAfterTransition()
        }

        binding.collapsingToolbar.title = ""
    }

    private fun setupUI() {
        with(binding) {
            // Load hero image (this is the shared element)
            MovieUtils.loadThumbnail(ivHeroImage, currentMovie.thumbnailUrl)

            // Basic info
            tvTitle.text = currentMovie.title
            tvYear.text = currentMovie.year.toString()
            tvDuration.text = MovieUtils.formatDurationReadable(currentMovie.duration)
            tvGenre.text = currentMovie.genre
            tvRating.text = String.format("%.1f", currentMovie.rating)
            tvDescription.text = currentMovie.description

            // Upload date
            tvUploadDate.text = formatUploadDate(currentMovie.uploadDate)

            // Available resolutions
            val resolutions = mutableListOf<String>()
            if (currentMovie.movieUrl1080p != null) resolutions.add("1080p")
            if (currentMovie.movieUrl360p != null) resolutions.add("360p")
            tvResolution.text = resolutions.joinToString(", ")

            // Disable quality options if not available
            rb1080p.isEnabled = currentMovie.movieUrl1080p != null
            rb360p.isEnabled = currentMovie.movieUrl360p != null
        }
    }

    private fun setupClickListeners() {
        binding.btnPlay.setOnClickListener {
            playMovie()
        }

        binding.btnAddToList.setOnClickListener {
            toggleMyList()
        }

        binding.rgQuality.setOnCheckedChangeListener { _, checkedId ->
            selectedQuality = when (checkedId) {
                R.id.rb1080p -> "1080p"
                R.id.rb360p -> "360p"
                R.id.rbMesh -> "mesh"
                else -> "auto"
            }
        }
    }

    private fun checkMyListStatus() {
        val user = Prefs.getUser(this)
        if (user == null) {
            binding.btnAddToList.visibility = View.GONE
            return
        }

        // Check local cache first
        val myList = Prefs.getMyList(this)
        isInMyList = myList.contains(currentMovie.id)
        updateMyListButton()

        // Fetch from server in background
        ApiClient.getApiService(this).getMyList(user.id!!).enqueue(object : Callback<List<Movie>> {
            override fun onResponse(call: Call<List<Movie>>, response: Response<List<Movie>>) {
                if (response.isSuccessful) {
                    val serverMyList = response.body() ?: emptyList()
                    isInMyList = serverMyList.any { it.id == currentMovie.id }
                    updateMyListButton()
                    
                    // Update local cache
                    Prefs.saveMyList(this@MovieDetailActivity, serverMyList.mapNotNull { it.id })
                }
            }

            override fun onFailure(call: Call<List<Movie>>, t: Throwable) {
                // Use cached value
            }
        })
    }

    private fun checkMeshAvailability() {
        if (MeshProtocol.isRunning() && MeshProtocol.isMovieAvailableInMesh(currentMovie.id!!)) {
            val peerCount = MeshProtocol.getAvailablePeersForMovie(currentMovie.id!!)
            binding.llMeshStatus.visibility = View.VISIBLE
            binding.tvMeshAvailability.text = "Available from $peerCount peer${if (peerCount != 1) "s" else ""}"
            binding.rbMesh.isEnabled = true
        } else {
            binding.llMeshStatus.visibility = View.GONE
            binding.rbMesh.isEnabled = false
            binding.rbMesh.text = "Mesh Network (Not Available)"
        }
    }

    private fun updateMyListButton() {
        if (isInMyList) {
            binding.btnAddToList.setIconResource(R.drawable.ic_check)
            binding.btnAddToList.contentDescription = getString(R.string.my_list_remove)
            binding.cvMyListIndicator.visibility = View.VISIBLE
        } else {
            binding.btnAddToList.setIconResource(R.drawable.ic_add)
            binding.btnAddToList.contentDescription = getString(R.string.my_list_add)
            binding.cvMyListIndicator.visibility = View.GONE
        }
    }

    private fun toggleMyList() {
        val user = Prefs.getUser(this) ?: return

        if (isInMyList) {
            // Remove from list
            ApiClient.getApiService(this)
                .removeFromMyList(user.id!!, currentMovie.id!!)
                .enqueue(object : Callback<ApiResponse<Unit>> {
                    override fun onResponse(
                        call: Call<ApiResponse<Unit>>,
                        response: Response<ApiResponse<Unit>>
                    ) {
                        if (response.isSuccessful) {
                            isInMyList = false
                            updateMyListButton()
                            Toast.makeText(
                                this@MovieDetailActivity,
                                "Removed from My List",
                                Toast.LENGTH_SHORT
                            ).show()
                            
                            // Update local cache
                            val myList = Prefs.getMyList(this@MovieDetailActivity).toMutableList()
                            myList.remove(currentMovie.id)
                            Prefs.saveMyList(this@MovieDetailActivity, myList)
                        } else {
                            Toast.makeText(
                                this@MovieDetailActivity,
                                "Failed to remove from list",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<Unit>>, t: Throwable) {
                        Toast.makeText(
                            this@MovieDetailActivity,
                            "Network error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        } else {
            // Add to list
            ApiClient.getApiService(this)
                .addToMyList(user.id!!, mapOf("movieId" to currentMovie.id!!))
                .enqueue(object : Callback<ApiResponse<Unit>> {
                    override fun onResponse(
                        call: Call<ApiResponse<Unit>>,
                        response: Response<ApiResponse<Unit>>
                    ) {
                        if (response.isSuccessful) {
                            isInMyList = true
                            updateMyListButton()
                            Toast.makeText(
                                this@MovieDetailActivity,
                                "Added to My List",
                                Toast.LENGTH_SHORT
                            ).show()
                            
                            // Update local cache
                            val myList = Prefs.getMyList(this@MovieDetailActivity).toMutableList()
                            myList.add(currentMovie.id!!)
                            Prefs.saveMyList(this@MovieDetailActivity, myList)
                        } else {
                            Toast.makeText(
                                this@MovieDetailActivity,
                                "Failed to add to list",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<Unit>>, t: Throwable) {
                        Toast.makeText(
                            this@MovieDetailActivity,
                            "Network error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

    private fun playMovie() {
        // Validate quality selection
        val finalQuality = when (selectedQuality) {
            "1080p" -> {
                if (currentMovie.movieUrl1080p == null) {
                    Toast.makeText(this, "1080p not available, using 360p", Toast.LENGTH_SHORT).show()
                    "360p"
                } else "1080p"
            }
            "360p" -> {
                if (currentMovie.movieUrl360p == null) {
                    Toast.makeText(this, "360p not available, using 1080p", Toast.LENGTH_SHORT).show()
                    "1080p"
                } else "360p"
            }
            "mesh" -> {
                if (!MeshProtocol.isRunning() || !MeshProtocol.isMovieAvailableInMesh(currentMovie.id!!)) {
                    showMeshUnavailableDialog()
                    return
                }
                "mesh"
            }
            else -> "auto" // Auto-select best quality
        }

        // Store quality preference
        Prefs.getSharedPreferences(this).edit()
            .putString("last_selected_quality", finalQuality)
            .apply()

        // Launch movie player
        val intent = Intent(this, MoviePlayerActivity::class.java).apply {
            putExtra("movie", currentMovie)
            putExtra("quality", finalQuality)
        }
        
        // Scale animation for player
        val options = ActivityOptionsCompat.makeScaleUpAnimation(
            binding.btnPlay,
            0,
            0,
            binding.btnPlay.width,
            binding.btnPlay.height
        )
        
        startActivity(intent, options.toBundle())
    }

    private fun showMeshUnavailableDialog() {
        AlertDialog.Builder(this)
            .setTitle("Mesh Network Unavailable")
            .setMessage("The mesh network is not currently available for this movie. Would you like to play using direct streaming instead?")
            .setPositiveButton("Play Direct") { _, _ ->
                selectedQuality = "auto"
                binding.rgQuality.check(R.id.rbAuto)
                playMovie()
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Open Mesh Settings") { _, _ ->
                startActivity(Intent(this, MeshActivity::class.java))
            }
            .show()
    }

    private fun formatUploadDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "Unknown"

        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check mesh availability when returning to this screen
        checkMeshAvailability()
    }

    private fun showDownloadDialog() {
        val qualities = arrayOf("High (1080p)", "Medium (720p)", "Low (360p)")
        
        AlertDialog.Builder(this)
            .setTitle("Download Quality")
            .setItems(qualities) { _, which ->
                val quality = when (which) {
                    0 -> DownloadQuality.HIGH
                    1 -> DownloadQuality.MEDIUM
                    2 -> DownloadQuality.LOW
                    else -> DownloadQuality.MEDIUM
                }
                
                startDownload(quality)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startDownload(quality: DownloadQuality) {
        // Check if already downloaded
        if (DownloadManager.isMovieDownloaded(this, currentMovie.id ?: "")) {
            Toast.makeText(this, "Already downloaded", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Check available space
        val estimatedSize = when (quality) {
            DownloadQuality.HIGH -> currentMovie.fileSize1080p ?: 2L * 1024 * 1024 * 1024
            DownloadQuality.MEDIUM -> ((currentMovie.fileSize1080p ?: 2L * 1024 * 1024 * 1024) * 0.6).toLong()
            DownloadQuality.LOW -> currentMovie.fileSize360p ?: 512L * 1024 * 1024
        }
        
        if (!DownloadManager.hasEnoughSpace(this, estimatedSize)) {
            AlertDialog.Builder(this)
                .setTitle("Insufficient Storage")
                .setMessage("Not enough space to download this movie. Please free up some space and try again.")
                .setPositiveButton("OK", null)
                .show()
            return
        }
        
        // Create download
        val download = DownloadManager.createDownload(this, currentMovie, quality)
        
        // Start download service
        val intent = Intent(this, DownloadService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        
        // Bind to service and start download
        bindService(intent, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as DownloadService.DownloadBinder
                binder.getService().startDownload(download)
                unbindService(this)
            }
            
            override fun onServiceDisconnected(name: ComponentName?) {}
        }, Context.BIND_AUTO_CREATE)
        
        Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        // Support reverse transition
        supportFinishAfterTransition()
    }
}