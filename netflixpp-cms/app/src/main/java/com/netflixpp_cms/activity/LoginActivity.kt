package com.netflixpp_cms.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.netflixpp_cms.api.ApiClient
import com.netflixpp_cms.databinding.ActivityLoginBinding
import com.netflixpp_cms.model.LoginRequest
import com.netflixpp_cms.model.LoginResponse
import com.netflixpp_cms.util.Prefs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if user is already logged in
        if (Prefs.getToken(this).isNotEmpty()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setupClickListeners()
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
                        // Salvar token e informações do usuário
                        Prefs.saveToken(this@LoginActivity, it.token)
                        Prefs.saveUser(this@LoginActivity, it.user)

                        Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()

                        // Ir para a tela principal
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } ?: run {
                        Toast.makeText(this@LoginActivity, "Invalid response from server", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Tratar diferentes códigos de erro
                    when (response.code()) {
                        401 -> Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                        404 -> Toast.makeText(this@LoginActivity, "Server not found", Toast.LENGTH_SHORT).show()
                        500 -> Toast.makeText(this@LoginActivity, "Server error", Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(this@LoginActivity, "Login failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                binding.btnLogin.isEnabled = true
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this@LoginActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Método para testes - preenche credenciais automaticamente
    private fun setupTestCredentials() {
        // Remover isso em produção!
        binding.etUsername.setText("admin")
        binding.etPassword.setText("admin123")
    }
}