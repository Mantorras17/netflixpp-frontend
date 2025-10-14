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
import com.netflixpp_streaming.databinding.ActivityVideoPlayerBinding
import com.netflixpp_streaming.model.Video
import com.netflixpp_streaming.service.MeshProtocol
import com.netflixpp_streaming.util.VideoUtils

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoPlayerBinding
    private var exoPlayer: ExoPlayer? = null
    private lateinit var currentVideo: Video
    private var isMeshSource = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentVideo = intent.getSerializableExtra("video") as Video
        setupUI()
        initializePlayer()
        setupClickListeners()
    }

    private fun setupUI() {
        binding.tvVideoTitle.text = currentVideo.title
        binding.tvVideoDescription.text = currentVideo.description

        // Hide system UI for immersive experience
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }

    private fun initializePlayer() {
        // Choose between direct URL or mesh source
        val videoUrl = if (VideoUtils.shouldUseMesh(this, currentVideo)) {
            isMeshSource = true
            MeshProtocol.getMeshVideoUrl(currentVideo.id!!)
        } else {
            // Use direct URL (prefer 1080p, fallback to 360p)
            currentVideo.videoUrl1080p ?: currentVideo.videoUrl360p
        }

        if (videoUrl.isNullOrEmpty()) {
            Toast.makeText(this, "Video not available", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        exoPlayer = ExoPlayer.Builder(this).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
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
                        }
                        Player.STATE_ENDED -> {
                            // Video ended
                            finish()
                        }
                    }
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@VideoPlayerActivity, "Playback error", Toast.LENGTH_SHORT).show()

                    // Try fallback source if mesh failed
                    if (isMeshSource) {
                        isMeshSource = false
                        initializePlayer()
                    }
                }
            })
        }

        binding.playerView.player = exoPlayer
    }

    @OptIn(UnstableApi::class)
    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnQuality.setOnClickListener {
            showQualitySelector()
        }

        binding.btnMeshToggle.setOnClickListener {
            toggleMeshSource()
        }

        // Click on player to toggle controls
        binding.playerView.setOnClickListener {
            if (binding.playerView.isControllerFullyVisible) {
                binding.playerView.hideController()
            } else {
                binding.playerView.showController()
            }
        }
    }

    private fun showQualitySelector() {
        val qualities = mutableListOf<String>()

        if (!currentVideo.videoUrl1080p.isNullOrEmpty()) {
            qualities.add("1080p")
        }
        if (!currentVideo.videoUrl360p.isNullOrEmpty()) {
            qualities.add("360p")
        }
        if (MeshProtocol.isVideoAvailableInMesh(currentVideo.id!!)) {
            qualities.add("Mesh (P2P)")
        }

        android.app.AlertDialog.Builder(this)
            .setTitle("Select Quality")
            .setItems(qualities.toTypedArray()) { dialog, which ->
                when (qualities[which]) {
                    "1080p" -> switchToQuality(currentVideo.videoUrl1080p!!, false)
                    "360p" -> switchToQuality(currentVideo.videoUrl360p!!, false)
                    "Mesh (P2P)" -> switchToMeshSource()
                }
            }
            .show()
    }

    private fun switchToQuality(url: String, isMesh: Boolean) {
        exoPlayer?.stop()
        isMeshSource = isMesh

        exoPlayer?.setMediaItem(MediaItem.fromUri(Uri.parse(url)))
        exoPlayer?.prepare()
        exoPlayer?.playWhenReady = true
    }

    private fun switchToMeshSource() {
        val meshUrl = MeshProtocol.getMeshVideoUrl(currentVideo.id!!)
        if (!meshUrl.isNullOrEmpty()) {
            switchToQuality(meshUrl, true)
            binding.btnMeshToggle.text = "Switch to Direct"
        } else {
            Toast.makeText(this, "Mesh source not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleMeshSource() {
        if (isMeshSource) {
            // Switch to direct URL
            val directUrl = currentVideo.videoUrl1080p ?: currentVideo.videoUrl360p
            if (!directUrl.isNullOrEmpty()) {
                switchToQuality(directUrl, false)
                binding.btnMeshToggle.text = "Switch to Mesh"
            }
        } else {
            // Switch to mesh
            switchToMeshSource()
        }
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }

    @OptIn(UnstableApi::class)
    override fun onBackPressed() {
        // Simple back press - always finish the activity
        super.onBackPressed()
    }
}