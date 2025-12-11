package com.netflixpp_cms.util

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.netflixpp_cms.api.ApiClient
import com.netflixpp_cms.databinding.DialogChangePasswordBinding
import com.netflixpp_cms.model.ApiResponse
import com.netflixpp_cms.model.ChangePasswordRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordDialog(context: Context) : Dialog(context) {

    private lateinit var binding: DialogChangePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnChange.setOnClickListener {
            val oldPassword = binding.etOldPassword.text.toString()
            val newPassword = binding.etNewPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (validateInput(oldPassword, newPassword, confirmPassword)) {
                changePassword(oldPassword, newPassword)
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun validateInput(oldPassword: String, newPassword: String, confirmPassword: String): Boolean {
        if (oldPassword.isEmpty()) {
            Toast.makeText(context, "Please enter old password", Toast.LENGTH_SHORT).show()
            return false
        }
        if (newPassword.isEmpty()) {
            Toast.makeText(context, "Please enter new password", Toast.LENGTH_SHORT).show()
            return false
        }
        if (newPassword.length < 6) {
            Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        if (newPassword != confirmPassword) {
            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun changePassword(oldPassword: String, newPassword: String) {
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.btnChange.isEnabled = false

        val request = ChangePasswordRequest(oldPassword, newPassword)

        ApiClient.getApiService(context).changePassword(request).enqueue(object : Callback<ApiResponse<String>> {
            override fun onResponse(call: Call<ApiResponse<String>>, response: Response<ApiResponse<String>>) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnChange.isEnabled = true

                if (response.isSuccessful) {
                    Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
                    dismiss()
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> "Old password is incorrect"
                        401 -> "Unauthorized"
                        else -> "Failed to change password"
                    }
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<String>>, t: Throwable) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnChange.isEnabled = true
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}