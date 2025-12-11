package com.netflixpp_streaming.activity

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.netflixpp_streaming.R
import com.netflixpp_streaming.databinding.ActivityMoviePlayerBinding
import com.netflixpp_streaming.model.Movie
import com.netflixpp_streaming.service.MeshProtocol
import com.netflixpp_streaming.util.Prefs
import com.netflixpp_streaming.util.MovieUtils

class MoviePlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoviePlayerBinding
    private var exoPlayer: ExoPlayer? = null
    private lateinit var currentMovie: Movie
    private var isMeshSource = false
    private var currentQuality = "auto"

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoviePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentMovie = intent.getSerializableExtra("movie") as? Movie ?: run {
            Toast.makeText(this, "Error loading movie", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        currentQuality = intent.getStringExtra("quality") ?: "auto"

        setupUI()
        initializePlayer()
        setupClickListeners()
    }

    private fun setupUI() {
        binding.tvMovieTitle.text = currentMovie.title
        binding.tvMovieDescription.text = currentMovie.description

        // Hide system UI for immersive experience
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }

    private fun initializePlayer() {
        // Choose movie URL based on quality
        val movieUrl = when (currentQuality) {
            "1080p" -> currentMovie.movieUrl1080p
            "360p" -> currentMovie.movieUrl360p
            "mesh" -> {
                isMeshSource = true
                MeshProtocol.getMeshMovieUrl(currentMovie.id!!)
            }
            else -> {
                // Auto: prefer 1080p, fallback to 360p
                currentMovie.movieUrl1080p ?: currentMovie.movieUrl360p
            }
        }

        if (movieUrl.isNullOrEmpty()) {
            Toast.makeText(this, "Movie not available", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        exoPlayer = ExoPlayer.Builder(this).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.parse(movieUrl))
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            binding.progressBar.visibility = View.VISIBLE
                            if (isMeshSource) {
                                binding.tvMeshStatus.visibility = View.VISIBLE
                                binding.tvMeshStatus.text = "Streaming from Mesh Network..."
                            }
                        }
                        Player.STATE_READY -> {
                            binding.progressBar.visibility = View.GONE
                            binding.tvMeshStatus.visibility = View.GONE
                            
                            // Track data usage
                            trackPlaybackStart()
                        }
                        Player.STATE_ENDED -> {
                            // Movie ended
                            finish()
                        }
                    }
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@MoviePlayerActivity, "Playback error: ${error.message}", Toast.LENGTH_SHORT).show()

                    // Try fallback source if mesh failed
                    if (isMeshSource) {
                        Toast.makeText(this@MoviePlayerActivity, "Mesh failed, switching to direct stream", Toast.LENGTH_SHORT).show()
                        isMeshSource = false
                        currentQuality = "auto"
                        initializePlayer()
                    }
                }
            })
        }

        binding.playerView.player = exoPlayer
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnQuality.setOnClickListener {
            showQualityDialog()
        }

        binding.btnMeshToggle.setOnClickListener {
            toggleMeshSource()
        }
    }

    private fun showQualityDialog() {
        val qualities = mutableListOf("Auto")
        if (currentMovie.movieUrl1080p != null) qualities.add("1080p")
        if (currentMovie.movieUrl360p != null) qualities.add("360p")

        val currentIndex = when (currentQuality) {
            "1080p" -> qualities.indexOf("1080p")
            "360p" -> qualities.indexOf("360p")
            else -> 0
        }

        android.app.AlertDialog.Builder(this)
            .setTitle("Select Quality")
            .setSingleChoiceItems(qualities.toTypedArray(), currentIndex) { dialog, which ->
                val newQuality = when (qualities[which]) {
                    "1080p" -> "1080p"
                    "360p" -> "360p"
                    else -> "auto"
                }
                
                if (newQuality != currentQuality) {
                    currentQuality = newQuality
                    isMeshSource = false
                    
                    // Save current position
                    val position = exoPlayer?.currentPosition ?: 0
                    
                    // Reinitialize with new quality
                    exoPlayer?.release()
                    initializePlayer()
                    
                    // Seek to saved position
                    exoPlayer?.seekTo(position)
                    
                    Toast.makeText(this, "Switched to $newQuality", Toast.LENGTH_SHORT).show()
                }
                
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun toggleMeshSource() {
        if (!MeshProtocol.isRunning()) {
            Toast.makeText(this, "Mesh network is not running", Toast.LENGTH_SHORT).show()
            return
        }

        if (!MeshProtocol.isMovieAvailableInMesh(currentMovie.id!!)) {
            Toast.makeText(this, "Movie not available in mesh network", Toast.LENGTH_SHORT).show()
            return
        }

        val position = exoPlayer?.currentPosition ?: 0
        
        isMeshSource = !isMeshSource
        exoPlayer?.release()
        
        if (isMeshSource) {
            currentQuality = "mesh"
        } else {
            currentQuality = "auto"
        }
        
        initializePlayer()
        exoPlayer?.seekTo(position)
        
        val message = if (isMeshSource) "Switched to Mesh" else "Switched to Direct"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun trackPlaybackStart() {
        // Track data usage
        val estimatedSize = when (currentQuality) {
            "1080p" -> currentMovie.duration * 2000L // ~2MB per second for 1080p
            "360p" -> currentMovie.duration * 500L   // ~500KB per second for 360p
            else -> currentMovie.duration * 1000L    // ~1MB per second for auto
        }
        
        if (isMeshSource) {
            Prefs.incrementDataShared(this, estimatedSize)
        } else {
            Prefs.incrementDataStreamed(this, estimatedSize)
        }
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        exoPlayer?.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}