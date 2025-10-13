package com.netflixpp_cms.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netflixpp_cms.databinding.ActivityMainBinding
import com.netflixpp_cms.util.Prefs

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        val user = Prefs.getUser(this)
        binding.tvWelcome.text = "Welcome, ${user?.username ?: "Admin"}"
    }

    private fun setupClickListeners() {
        binding.cardUploadVideo.setOnClickListener {
            startActivity(Intent(this, UploadActivity::class.java))
        }

        binding.cardManageVideos.setOnClickListener {
            startActivity(Intent(this, VideoListActivity::class.java))
        }

        binding.cardManageUsers.setOnClickListener {
            startActivity(Intent(this, UserManagementActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            Prefs.clearUserData(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}