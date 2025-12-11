package com.netflixpp_streaming.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netflixpp_streaming.databinding.ActivityMyListBinding

class MyListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}