package com.netflixpp_streaming.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.netflixpp_streaming.api.ApiClient
import com.netflixpp_streaming.databinding.ActivityLoginBinding
import com.netflixpp_streaming.model.LoginRequest
import com.netflixpp_streaming.model.LoginResponse
import com.netflixpp_streaming.util.Prefs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setupTestCredentials()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(username, password)
        }
    }

    private fun loginUser(username: String, password: String) {
        binding.btnLogin.isEnabled = false
        binding.progressBar.visibility = android.view.View.VISIBLE

        val loginRequest = LoginRequest(username, password)

        ApiClient.getApiService(this).login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                binding.btnLogin.isEnabled = true
                binding.progressBar.visibility = android.view.View.GONE

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    loginResponse?.let {
                        Prefs.saveToken(this@LoginActivity, it.token)
                        Prefs.saveUser(this@LoginActivity, it.user)

                        Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                binding.btnLogin.isEnabled = true
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this@LoginActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupTestCredentials() {
        // For testing
        binding.etUsername.setText("user")
        binding.etPassword.setText("password")
    }
}