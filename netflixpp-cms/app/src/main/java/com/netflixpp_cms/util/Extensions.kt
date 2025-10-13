package com.netflixpp_cms.util

import android.widget.Toast
import androidx.fragment.app.Fragment

// Extension functions for easier usage
fun Fragment.toast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun androidx.appcompat.app.AppCompatActivity.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}