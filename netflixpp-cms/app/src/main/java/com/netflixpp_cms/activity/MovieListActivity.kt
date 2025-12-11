package com.netflixpp_cms.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.netflixpp_cms.adapter.MovieAdapter
import com.netflixpp_cms.api.ApiClient
import com.netflixpp_cms.databinding.ActivityMovieListBinding
import com.netflixpp_cms.model.ApiResponse
import com.netflixpp_cms.model.Movie
import com.netflixpp_cms.model.MoviesResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MovieListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieListBinding
    private lateinit var movieAdapter: MovieAdapter
    private val movieList = mutableListOf<Movie>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupClickListeners()
        loadMovies()
    }

    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter(
            movies = movieList,
            onDeleteClick = { movie -> deleteMovie(movie) },
            onEditClick = { movie -> editMovie(movie) },
            onDetailsClick = { movie -> viewDetails(movie) },
            onGenerateChunksClick = { movie -> generateChunks(movie) }
        )

        binding.rvMovies.apply {
            layoutManager = LinearLayoutManager(this@MovieListActivity)
            adapter = movieAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadMovies()
        }
    }

    private fun loadMovies() {
        binding.progressBar.visibility = android.view.View.VISIBLE

        ApiClient.getApiService(this).getMovies().enqueue(object : Callback<MoviesResponse> {
            override fun onResponse(call: Call<MoviesResponse>, response: Response<MoviesResponse>) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.swipeRefresh.isRefreshing = false

                if (response.isSuccessful) {
                    response.body()?.let { moviesResponse ->
                        movieList.clear()
                        movieList.addAll(moviesResponse.movies)
                        movieAdapter.notifyDataSetChanged()
                        
                        binding.tvMovieCount.text = "Total: ${moviesResponse.total} movies"
                    }
                } else {
                    Toast.makeText(this@MovieListActivity, "Failed to load movies", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MoviesResponse>, t: Throwable) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(this@MovieListActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun editMovie(movie: Movie) {
        val intent = Intent(this, EditMovieActivity::class.java)
        intent.putExtra("MOVIE_ID", movie.id)
        startActivityForResult(intent, REQUEST_EDIT)
    }

    private fun viewDetails(movie: Movie) {
        val intent = Intent(this, MovieDetailActivity::class.java)
        intent.putExtra("MOVIE_ID", movie.id)
        startActivity(intent)
    }

    private fun generateChunks(movie: Movie) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Generate Chunks")
            .setMessage("Generate streaming chunks for '${movie.title}'?")
            .setPositiveButton("Generate") { _, _ ->
                executeChunkGeneration(movie.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun executeChunkGeneration(movieId: Int) {
        binding.progressBar.visibility = android.view.View.VISIBLE

        ApiClient.getApiService(this).generateChunks(movieId).enqueue(object : Callback<com.netflixpp_cms.model.ChunkGenerationResponse> {
            override fun onResponse(
                call: Call<com.netflixpp_cms.model.ChunkGenerationResponse>,
                response: Response<com.netflixpp_cms.model.ChunkGenerationResponse>
            ) {
                binding.progressBar.visibility = android.view.View.GONE

                if (response.isSuccessful) {
                    response.body()?.let { result ->
                        Toast.makeText(
                            this@MovieListActivity,
                            "Chunks generated: ${result.chunksGenerated}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(this@MovieListActivity, "Generation failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<com.netflixpp_cms.model.ChunkGenerationResponse>, t: Throwable) {
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this@MovieListActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteMovie(movie: Movie) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Movie")
            .setMessage("Are you sure you want to delete '${movie.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                performDeleteMovie(movie.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performDeleteMovie(movieId: Int) {
        ApiClient.getApiService(this).deleteMovie(movieId).enqueue(object : Callback<ApiResponse<String>> {
            override fun onResponse(call: Call<ApiResponse<String>>, response: Response<ApiResponse<String>>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MovieListActivity, "Movie deleted successfully", Toast.LENGTH_SHORT).show()
                    loadMovies()
                } else {
                    Toast.makeText(this@MovieListActivity, "Failed to delete movie", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<String>>, t: Throwable) {
                Toast.makeText(this@MovieListActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_EDIT && resultCode == RESULT_OK) {
            loadMovies()
        }
    }

    companion object {
        private const val REQUEST_EDIT = 100
    }
}