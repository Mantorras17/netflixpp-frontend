package com.netflixpp_cms.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netflixpp_cms.databinding.ActivityMainBinding
import com.netflixpp_cms.util.ChangePasswordDialog
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
        binding.tvRole.text = "Role: ${user?.role ?: "Unknown"}"
    }

    private fun setupClickListeners() {
        binding.cardDashboard.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }

        binding.cardUploadMovie.setOnClickListener {
            startActivity(Intent(this, UploadActivity::class.java))
        }

        binding.cardManageMovies.setOnClickListener {
            startActivity(Intent(this, MovieListActivity::class.java))
        }

        binding.cardManageUsers.setOnClickListener {
            startActivity(Intent(this, UserManagementActivity::class.java))
        }

        binding.cardStorage.setOnClickListener {
            startActivity(Intent(this, StorageActivity::class.java))
        }

        binding.cardLogs.setOnClickListener {
            startActivity(Intent(this, LogsActivity::class.java))
        }

        binding.btnSettings.setOnClickListener {
            showSettingsDialog()
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun showSettingsDialog() {
        val options = arrayOf("Change Password", "View Profile", "About")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Settings")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val dialog = ChangePasswordDialog(this)
                        dialog.show()
                    }
                    1 -> {
                        showProfileDialog()
                    }
                    2 -> {
                        showAboutDialog()
                    }
                }
            }
            .show()
    }

    private fun showProfileDialog() {
        val user = Prefs.getUser(this)
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Profile")
            .setMessage("""
                Username: ${user?.username}
                Email: ${user?.email}
                Role: ${user?.role}
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showAboutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("NetflixPP CMS")
            .setMessage("""
                Version: 1.0.0
                Content Management System
                for NetflixPP Platform
                
                © 2025 NetflixPP
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun logout() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                Prefs.clearUserData(this)
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}