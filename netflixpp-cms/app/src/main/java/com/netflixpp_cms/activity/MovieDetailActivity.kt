package com.netflixpp_cms.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.netflixpp_cms.api.ApiClient
import com.netflixpp_cms.databinding.ActivityMovieDetailBinding
import com.netflixpp_cms.model.ChunkGenerationResponse
import com.netflixpp_cms.model.ChunksInfo
import com.netflixpp_cms.model.Movie
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MovieDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieDetailBinding
    private var movieId: Int = -1
    private var movie: Movie? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        movieId = intent.getIntExtra("MOVIE_ID", -1)
        if (movieId == -1) {
            Toast.makeText(this, "Invalid movie ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadMovieDetails()
        loadChunksInfo()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, EditMovieActivity::class.java)
            intent.putExtra("MOVIE_ID", movieId)
            startActivityForResult(intent, REQUEST_EDIT)
        }

        binding.btnGenerateChunks.setOnClickListener {
            generateChunks()
        }

        binding.btnViewChunks.setOnClickListener {
            loadChunksInfo()
        }
    }

    private fun loadMovieDetails() {
        binding.progressBar.visibility = android.view.View.VISIBLE

        ApiClient.getApiService(this).getMovie(movieId).enqueue(object : Callback<Movie> {
            override fun onResponse(call: Call<Movie>, response: Response<Movie>) {
                binding.progressBar.visibility = android.view.View.GONE

                if (response.isSuccessful) {
                    movie = response.body()
                    displayMovieDetails(movie)
                } else {
                    Toast.makeText(this@MovieDetailActivity, "Failed to load movie", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Movie>, t: Throwable) {
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this@MovieDetailActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayMovieDetails(movie: Movie?) {
        movie?.let {
            binding.tvTitle.text = it.title
            binding.tvDescription.text = it.description
            binding.tvCategory.text = "Category: ${it.category}"
            binding.tvGenre.text = "Genre: ${it.genre}"
            binding.tvYear.text = "Year: ${it.year}"
            binding.tvDuration.text = "Duration: ${it.duration} min"
            
            val has1080 = !it.filePath1080.isNullOrEmpty()
            val has360 = !it.filePath360.isNullOrEmpty()
            binding.tvQuality1080.text = if (has1080) "✅ 1080p Available" else "❌ 1080p Not Available"
            binding.tvQuality360.text = if (has360) "✅ 360p Available" else "❌ 360p Not Available"
        }
    }

    private fun loadChunksInfo() {
        ApiClient.getApiService(this).getChunksInfo("movie_$movieId").enqueue(object : Callback<ChunksInfo> {
            override fun onResponse(call: Call<ChunksInfo>, response: Response<ChunksInfo>) {
                if (response.isSuccessful) {
                    response.body()?.let { info ->
                        binding.tvChunkInfo.text = """
                            Chunks Generated: ${info.totalChunks}
                            Chunk Size: ${formatFileSize(info.chunkSize)}
                            Quality: ${info.quality}
                        """.trimIndent()
                    }
                } else {
                    binding.tvChunkInfo.text = "No chunks generated yet"
                }
            }

            override fun onFailure(call: Call<ChunksInfo>, t: Throwable) {
                binding.tvChunkInfo.text = "Failed to load chunk info"
            }
        })
    }

    private fun generateChunks() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Generate Chunks")
            .setMessage("This will process the movie file into streamable chunks. This may take several minutes. Continue?")
            .setPositiveButton("Generate") { _, _ ->
                executeChunkGeneration()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun executeChunkGeneration() {
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.btnGenerateChunks.isEnabled = false

        ApiClient.getApiService(this).generateChunks(movieId).enqueue(object : Callback<ChunkGenerationResponse> {
            override fun onResponse(call: Call<ChunkGenerationResponse>, response: Response<ChunkGenerationResponse>) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnGenerateChunks.isEnabled = true

                if (response.isSuccessful) {
                    response.body()?.let { result ->
                        Toast.makeText(
                            this@MovieDetailActivity,
                            "Chunks generated: ${result.chunksGenerated ?: 0}",
                            Toast.LENGTH_LONG
                        ).show()
                        loadChunksInfo()
                    }
                } else {
                    Toast.makeText(this@MovieDetailActivity, "Generation failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ChunkGenerationResponse>, t: Throwable) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnGenerateChunks.isEnabled = true
                Toast.makeText(this@MovieDetailActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_EDIT && resultCode == RESULT_OK) {
            loadMovieDetails()
        }
    }

    companion object {
        private const val REQUEST_EDIT = 100
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> {
                val mb = bytes / (1024.0 * 1024.0)
                if (mb >= 10) "10 MB" else "%.2f MB".format(mb)
            }
            bytes >= 1024 -> "%.2f KB".format(bytes / 1024.0)
            else -> "$bytes bytes"
        }
    }
}