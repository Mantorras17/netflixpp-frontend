package com.netflixpp_cms.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.netflixpp_cms.api.ApiClient
import com.netflixpp_cms.databinding.ActivityUploadBinding
import com.netflixpp_cms.model.ChunkGenerationResponse
import com.netflixpp_cms.model.MovieUploadResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream


class UploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding
    private var movieUri: Uri? = null
    private var uploadedMovieId: Int? = null

    private val moviePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            movieUri = result.data?.data
            binding.tvSelectedFile.text = "Selected: ${getFileName(movieUri)}"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGenreSpinner()
        setupCategorySpinner()
        setupClickListeners()
    }

    private fun setupGenreSpinner() {
        val genres = arrayOf("Comedy", "Animation", "Horror", "Sci-Fi", "Action", "Drama", "Documentary", "Romance")
        binding.spGenre.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genres)
    }

    private fun setupCategorySpinner() {
        val categories = arrayOf("Movies", "Series", "Documentaries", "Kids", "Sports")
        binding.spCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
    }

    private fun setupClickListeners() {
        binding.btnSelectMovie.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "movie/*"
            }
            moviePickerLauncher.launch(intent)
        }

        binding.btnUpload.setOnClickListener {
            if (validateForm()) {
                uploadMovie()
            }
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun validateForm(): Boolean {
        if (binding.etTitle.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.etDescription.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.etDuration.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter duration", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.etYear.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter year", Toast.LENGTH_SHORT).show()
            return false
        }
        if (movieUri == null) {
            Toast.makeText(this, "Please select a movie file", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun uploadMovie() {
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.tvStatus.text = "Uploading movie..."
        binding.btnUpload.isEnabled = false

        try {
            val tempFile = createTempFileFromUri(movieUri!!)
            if (tempFile == null) {
                showError("Failed to read movie file")
                return
            }
            val requestFile = tempFile.asRequestBody("movie/*".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

            val title = binding.etTitle.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val description = binding.etDescription.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val category = binding.spCategory.selectedItem.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val genre = binding.spGenre.selectedItem.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val year = binding.etYear.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val duration = binding.etDuration.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            ApiClient.getApiService(this).uploadMovie(
                title, description, category, genre, year, duration, filePart
            ).enqueue(object : Callback<MovieUploadResponse> {
                override fun onResponse(call: Call<MovieUploadResponse>, response: Response<MovieUploadResponse>) {
                    tempFile.delete()

                    if (response.isSuccessful && response.body()?.movieId != null) {
                        uploadedMovieId = response.body()!!.movieId
                        binding.tvStatus.text = "Upload complete! Generating chunks..."
                        generateChunks(uploadedMovieId!!)
                    } else {
                        showError(response.body()?.message ?: "Upload failed: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<MovieUploadResponse>, t: Throwable) {
                    tempFile.delete()
                    showError("Network error: ${t.message}")
                }
            })
        } catch (e: Exception) {
            showError("Error: ${e.message}")
        }
    }

    private fun generateChunks(movieId: Int) {
        ApiClient.getApiService(this).generateChunks(movieId)
            .enqueue(object : Callback<ChunkGenerationResponse> {
                override fun onResponse(call: Call<ChunkGenerationResponse>, response: Response<ChunkGenerationResponse>) {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.btnUpload.isEnabled = true

                    if (response.isSuccessful) {
                        val result = response.body()
                        binding.tvStatus.text = "Movie uploaded successfully with chunks generated!"
                        Toast.makeText(
                            this@UploadActivity,
                            "Chunks generated: ${result?.chunksGenerated ?: 0}",
                            Toast.LENGTH_LONG
                        ).show()
                        clearForm()
                        finish()
                    } else {
                        binding.tvStatus.text = "Movie uploaded but chunk generation failed"
                        Toast.makeText(this@UploadActivity, "Chunk generation failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ChunkGenerationResponse>, t: Throwable) {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.btnUpload.isEnabled = true
                    binding.tvStatus.text = "Movie uploaded but chunk generation failed"
                }
            })
    }

    private fun createTempFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload_", ".mp4", cacheDir)
            FileOutputStream(tempFile).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    private fun getFileName(uri: Uri?): String {
        uri ?: return "No file selected"
        var name = "Unknown"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = android.view.View.GONE
        binding.btnUpload.isEnabled = true
        binding.tvStatus.text = "Error"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun clearForm() {
        binding.etTitle.text?.clear()
        binding.etDescription.text?.clear()
        binding.etDuration.text?.clear()
        binding.etYear.text?.clear()
        binding.tvSelectedFile.text = "No file selected"
        binding.tvStatus.text = ""
        movieUri = null
        uploadedMovieId = null
    }
}