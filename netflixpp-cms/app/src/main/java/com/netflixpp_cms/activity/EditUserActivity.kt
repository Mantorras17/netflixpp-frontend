package com.netflixpp_cms.activity

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.netflixpp_cms.api.ApiClient
import com.netflixpp_cms.databinding.ActivityEditUserBinding
import com.netflixpp_cms.model.ApiResponse
import com.netflixpp_cms.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditUserBinding
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRoleSpinner()
        loadUserDetails()
        setupClickListeners()
    }

    private fun setupRoleSpinner() {
        val roles = arrayOf("user", "admin")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spRole.adapter = adapter
    }

    private fun loadUserDetails() {
        binding.progressBar.visibility = android.view.View.VISIBLE

        ApiClient.getApiService(this).getUser(userId).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                binding.progressBar.visibility = android.view.View.GONE

                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        populateFields(user)
                    }
                } else {
                    Toast.makeText(this@EditUserActivity, "Failed to load user", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this@EditUserActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun populateFields(user: User) {
        binding.tvUsername.text = "Username: ${user.username}"
        binding.etEmail.setText(user.email)
        
        val roleAdapter = binding.spRole.adapter as ArrayAdapter<String>
        val rolePosition = roleAdapter.getPosition(user.role.lowercase())
        if (rolePosition >= 0) binding.spRole.setSelection(rolePosition)
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            if (validateForm()) {
                updateUser()
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnResetPassword.setOnClickListener {
            resetPassword()
        }
    }

    private fun validateForm(): Boolean {
        if (binding.etEmail.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun updateUser() {
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.btnSave.isEnabled = false

        val updates = mutableMapOf<String, Any>()
        updates["email"] = binding.etEmail.text.toString()
        updates["role"] = binding.spRole.selectedItem.toString()

        ApiClient.getApiService(this).updateUser(userId, updates).enqueue(object : Callback<ApiResponse<String>> {
            override fun onResponse(call: Call<ApiResponse<String>>, response: Response<ApiResponse<String>>) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnSave.isEnabled = true

                if (response.isSuccessful) {
                    Toast.makeText(this@EditUserActivity, "User updated successfully", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@EditUserActivity, "Update failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<String>>, t: Throwable) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnSave.isEnabled = true
                Toast.makeText(this@EditUserActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun resetPassword() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setMessage("This will generate a new random password for this user. Continue?")
            .setPositiveButton("Reset") { _, _ ->
                executePasswordReset()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun executePasswordReset() {
        binding.progressBar.visibility = android.view.View.VISIBLE

        ApiClient.getApiService(this).resetUserPassword(userId).enqueue(object : Callback<com.netflixpp_cms.model.PasswordResetResponse> {
            override fun onResponse(call: Call<com.netflixpp_cms.model.PasswordResetResponse>, response: Response<com.netflixpp_cms.model.PasswordResetResponse>) {
                binding.progressBar.visibility = android.view.View.GONE

                if (response.isSuccessful) {
                    response.body()?.let { result ->
                        showPasswordDialog(result.newPassword)
                    }
                } else {
                    Toast.makeText(this@EditUserActivity, "Password reset failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<com.netflixpp_cms.model.PasswordResetResponse>, t: Throwable) {
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this@EditUserActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showPasswordDialog(newPassword: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Password Reset Successful")
            .setMessage("New password: $newPassword\n\nPlease share this with the user.")
            .setPositiveButton("Copy") { _, _ ->
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("password", newPassword)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Password copied to clipboard", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("OK", null)
            .show()
    }
}