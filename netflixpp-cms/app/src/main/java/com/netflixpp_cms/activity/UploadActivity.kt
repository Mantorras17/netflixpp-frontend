package com.netflixpp_cms.activity

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import com.netflixpp_cms.api.ApiClient
import com.netflixpp_cms.databinding.ActivityUploadBinding
import com.netflixpp_cms.model.ApiResponse
import com.netflixpp_cms.model.Video
import com.netflixpp_cms.util.Prefs
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class UploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding
    private var videoUri: Uri? = null

    private val videoPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val resultCode = result.resultCode
        val data = result.data

        when (resultCode) {
            Activity.RESULT_OK -> {
                videoUri = data?.data
                binding.tvSelectedFile.text = "Video selected: ${getFileName(videoUri)}"
            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Task cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGenreSpinner()
        setupClickListeners()
    }

    private fun setupGenreSpinner() {
        val genres = arrayOf("Comedy", "Animation", "Horror", "Sci-Fi", "Action", "Drama", "Documentary")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spGenre.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnSelectVideo.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .galleryMimeTypes(arrayOf("video/*"))
                .maxResultSize(1080, 1080)
                .createIntent { intent ->
                    videoPickerLauncher.launch(intent)
                }
        }

        binding.btnUpload.setOnClickListener {
            if (validateForm()) {
                uploadVideo()
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
        if (videoUri == null) {
            Toast.makeText(this, "Please select a video file", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun uploadVideo() {
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.btnUpload.isEnabled = false

        try {
            val videoFile = File(videoUri!!.path!!)
            val requestFile = RequestBody.create("video/*".toMediaTypeOrNull(), videoFile)
            val videoPart = MultipartBody.Part.createFormData("videoFile", videoFile.name, requestFile)

            val title = RequestBody.create("text/plain".toMediaTypeOrNull(), binding.etTitle.text.toString())
            val description = RequestBody.create("text/plain".toMediaTypeOrNull(), binding.etDescription.text.toString())
            val genre = RequestBody.create("text/plain".toMediaTypeOrNull(), binding.spGenre.selectedItem.toString())
            val duration = RequestBody.create("text/plain".toMediaTypeOrNull(), binding.etDuration.text.toString())
            val year = RequestBody.create("text/plain".toMediaTypeOrNull(), binding.etYear.text.toString())

            ApiClient.getApiService(this).uploadVideo(
                title,
                description,
                genre,
                duration,
                year,
                videoPart
            ).enqueue(object : Callback<ApiResponse<Video>> {
                override fun onResponse(call: Call<ApiResponse<Video>>, response: Response<ApiResponse<Video>>) {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.btnUpload.isEnabled = true

                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse?.success == true) {
                            Toast.makeText(this@UploadActivity, "Video uploaded successfully", Toast.LENGTH_SHORT).show()
                            clearForm()
                        } else {
                            Toast.makeText(
                                this@UploadActivity,
                                apiResponse?.error ?: "Upload failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(this@UploadActivity, "Server error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<Video>>, t: Throwable) {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.btnUpload.isEnabled = true
                    Toast.makeText(this@UploadActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            binding.progressBar.visibility = android.view.View.GONE
            binding.btnUpload.isEnabled = true
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileName(uri: Uri?): String {
        return uri?.lastPathSegment ?: "Unknown file"
    }

    private fun clearForm() {
        binding.etTitle.text?.clear()
        binding.etDescription.text?.clear()
        binding.etDuration.text?.clear()
        binding.etYear.text?.clear()
        binding.tvSelectedFile.text = "No file selected"
        videoUri = null
    }
}
