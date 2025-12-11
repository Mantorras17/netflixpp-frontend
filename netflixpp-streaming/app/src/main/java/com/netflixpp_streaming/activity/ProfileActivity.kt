package com.netflixpp_streaming.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.netflixpp_streaming.R
import com.netflixpp_streaming.databinding.ActivityProfileBinding
import com.netflixpp_streaming.util.Prefs
import kotlin.math.roundToInt

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadUserInfo()
        loadPreferences()
        loadDataUsage()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadUserInfo() {
        val user = Prefs.getUser(this)
        user?.let {
            binding.tvUsername.text = it.username
            binding.tvEmail.text = it.email
        }
    }

    private fun loadPreferences() {
        val prefs = Prefs.getSharedPreferences(this)

        // Load quality preference
        val quality = prefs.getString("pref_movie_quality", "auto") ?: "auto"
        when (quality) {
            "auto" -> binding.rgQuality.check(R.id.rbAutoQuality)
            "high" -> binding.rgQuality.check(R.id.rbHighQuality)
            "medium" -> binding.rgQuality.check(R.id.rbMediumQuality)
        }

        // Load mesh preferences
        val autoMesh = prefs.getBoolean("pref_auto_mesh", true)
        binding.switchAutoMesh.isChecked = autoMesh

        val maxConnections = prefs.getInt("pref_max_connections", 5)
        binding.sliderMaxConnections.value = maxConnections.toFloat()
        binding.tvMaxConnections.text = maxConnections.toString()
    }

    private fun loadDataUsage() {
        val prefs = Prefs.getSharedPreferences(this)
        
        val dataStreamed = prefs.getLong("data_streamed", 0)
        val dataShared = prefs.getLong("data_shared", 0)

        binding.tvDataStreamed.text = formatBytes(dataStreamed)
        binding.tvDataShared.text = formatBytes(dataShared)
    }

    private fun setupClickListeners() {
        // Account Section
        binding.btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        // Quality Settings
        binding.rgQuality.setOnCheckedChangeListener { _, checkedId ->
            val quality = when (checkedId) {
                R.id.rbHighQuality -> "high"
                R.id.rbMediumQuality -> "medium"
                else -> "auto"
            }
            Prefs.getSharedPreferences(this).edit()
                .putString("pref_movie_quality", quality)
                .apply()
            
            Toast.makeText(this, "Quality preference updated", Toast.LENGTH_SHORT).show()
        }

        // Mesh Settings
        binding.switchAutoMesh.setOnCheckedChangeListener { _, isChecked ->
            Prefs.getSharedPreferences(this).edit()
                .putBoolean("pref_auto_mesh", isChecked)
                .apply()
            
            val message = if (isChecked) "Mesh network will auto-enable" else "Mesh auto-enable disabled"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        binding.sliderMaxConnections.addOnChangeListener { _, value, _ ->
            val connections = value.roundToInt()
            binding.tvMaxConnections.text = connections.toString()
            Prefs.getSharedPreferences(this).edit()
                .putInt("pref_max_connections", connections)
                .apply()
        }

        binding.btnMeshSettings.setOnClickListener {
            startActivity(Intent(this, MeshActivity::class.java))
        }

        // Data Usage
        binding.btnResetStats.setOnClickListener {
            showResetStatsDialog()
        }

        // App Settings
        binding.btnAbout.setOnClickListener {
            showAboutDialog()
        }

        // Logout
        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val etUsername = dialogView.findViewById<TextInputEditText>(R.id.etUsername)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.etEmail)

        val user = Prefs.getUser(this)
        etUsername.setText(user?.username)
        etEmail.setText(user?.email)

        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newUsername = etUsername.text.toString().trim()
                val newEmail = etEmail.text.toString().trim()

                if (newUsername.isEmpty() || newEmail.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Update user info (in real app, call API)
                user?.let {
                    val updatedUser = it.copy(username = newUsername, email = newEmail)
                    Prefs.saveUser(this, updatedUser)
                    loadUserInfo()
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etCurrentPassword = dialogView.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNewPassword = dialogView.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<TextInputEditText>(R.id.etConfirmPassword)

        MaterialAlertDialogBuilder(this)
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change") { _, _ ->
                val currentPassword = etCurrentPassword.text.toString()
                val newPassword = etNewPassword.text.toString()
                val confirmPassword = etConfirmPassword.text.toString()

                when {
                    currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty() -> {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    }
                    newPassword != confirmPassword -> {
                        Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show()
                    }
                    newPassword.length < 6 -> {
                        Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // In real app, call API to change password
                        Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showResetStatsDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Reset Statistics")
            .setMessage("Are you sure you want to reset all data usage statistics?")
            .setPositiveButton("Reset") { _, _ ->
                Prefs.getSharedPreferences(this).edit()
                    .putLong("data_streamed", 0)
                    .putLong("data_shared", 0)
                    .apply()
                
                loadDataUsage()
                Toast.makeText(this, "Statistics reset", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("About Netflix++ Streaming")
            .setMessage("""
                Version: 1.0.0
                
                Netflix++ is a next-generation streaming platform with peer-to-peer mesh networking capabilities.
                
                Features:
                • HD Movie Streaming (1080p/360p)
                • Mesh Network P2P Sharing
                • Offline Downloads
                • My List Management
                
                © 2025 Netflix++. All rights reserved.
            """.trimIndent())
            .setPositiveButton("OK", null)
            .setNeutralButton("Privacy Policy") { _, _ ->
                Toast.makeText(this, "Opening privacy policy...", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                Prefs.clearUserData(this)
                
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1_000_000_000 -> String.format("%.2f GB", bytes / 1_000_000_000.0)
            bytes >= 1_000_000 -> String.format("%.1f MB", bytes / 1_000_000.0)
            bytes >= 1_000 -> String.format("%.0f KB", bytes / 1_000.0)
            else -> "$bytes B"
        }
    }
}