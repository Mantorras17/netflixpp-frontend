package com.netflixppstreaming.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.netflixpp_streaming.activity.LoginActivity
import com.netflixpp_streaming.activity.MainActivity
import com.netflixpp_streaming.databinding.ActivitySplashBinding
import com.netflixpp_streaming.util.Prefs

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Show splash for 2 seconds then redirect
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginStatus()
        }, 2000)
    }

    private fun checkLoginStatus() {
        val token = Prefs.getToken(this)

        val intent = if (token.isNotEmpty()) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}