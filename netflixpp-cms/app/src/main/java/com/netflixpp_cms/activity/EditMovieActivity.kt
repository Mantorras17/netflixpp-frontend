package com.netflixpp_cms.activity

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.netflixpp_cms.api.ApiClient
import com.netflixpp_cms.databinding.ActivityEditMovieBinding
import com.netflixpp_cms.model.ApiResponse
import com.netflixpp_cms.model.Movie
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditMovieActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditMovieBinding
    private var movieId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        movieId = intent.getIntExtra("MOVIE_ID", -1)
        if (movieId == -1) {
            Toast.makeText(this, "Invalid movie ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupGenreSpinner()
        setupCategorySpinner()
        loadMovieDetails()
        setupClickListeners()
    }

    private fun setupGenreSpinner() {
        val genres = arrayOf("Action", "Comedy", "Drama", "Horror", "Sci-Fi", "Animation", "Documentary", "Romance")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spGenre.adapter = adapter
    }

    private fun setupCategorySpinner() {
        val categories = arrayOf("Movies", "Series", "Documentaries", "Kids", "Sports")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spCategory.adapter = adapter
    }

    private fun loadMovieDetails() {
        binding.progressBar.visibility = android.view.View.VISIBLE

        ApiClient.getApiService(this).getMovie(movieId).enqueue(object : Callback<Movie> {
            override fun onResponse(call: Call<Movie>, response: Response<Movie>) {
                binding.progressBar.visibility = android.view.View.GONE

                if (response.isSuccessful) {
                    response.body()?.let { movie ->
                        populateFields(movie)
                    }
                } else {
                    Toast.makeText(this@EditMovieActivity, "Failed to load movie", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<Movie>, t: Throwable) {
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this@EditMovieActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun populateFields(movie: Movie) {
        binding.etTitle.setText(movie.title)
        binding.etDescription.setText(movie.description)
        binding.etYear.setText(movie.year.toString())
        binding.etDuration.setText(movie.duration.toString())
        
        // Set spinner selections
        val genreAdapter = binding.spGenre.adapter as ArrayAdapter<String>
        val genrePosition = genreAdapter.getPosition(movie.genre)
        if (genrePosition >= 0) binding.spGenre.setSelection(genrePosition)
        
        val categoryAdapter = binding.spCategory.adapter as ArrayAdapter<String>
        val categoryPosition = categoryAdapter.getPosition(movie.category)
        if (categoryPosition >= 0) binding.spCategory.setSelection(categoryPosition)
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            if (validateForm()) {
                updateMovie()
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun validateForm(): Boolean {
        if (binding.etTitle.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.etYear.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter year", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun updateMovie() {
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.btnSave.isEnabled = false

        val updates = mutableMapOf<String, Any>()
        updates["title"] = binding.etTitle.text.toString()
        updates["description"] = binding.etDescription.text.toString()
        updates["category"] = binding.spCategory.selectedItem.toString()
        updates["genre"] = binding.spGenre.selectedItem.toString()
        updates["year"] = binding.etYear.text.toString().toIntOrNull() ?: 0
        updates["duration"] = binding.etDuration.text.toString().toIntOrNull() ?: 0

        ApiClient.getApiService(this).updateMovie(movieId, updates).enqueue(object : Callback<ApiResponse<String>> {
            override fun onResponse(call: Call<ApiResponse<String>>, response: Response<ApiResponse<String>>) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnSave.isEnabled = true

                if (response.isSuccessful) {
                    Toast.makeText(this@EditMovieActivity, "Movie updated successfully", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@EditMovieActivity, "Update failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<String>>, t: Throwable) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnSave.isEnabled = true
                Toast.makeText(this@EditMovieActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}