package com.netflixpp_cms.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.netflixpp_cms.adapter.UserAdapter
import com.netflixpp_cms.api.ApiClient
import com.netflixpp_cms.databinding.ActivityUserManagementBinding
import com.netflixpp_cms.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserManagementBinding
    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupClickListeners()
        loadUsers()
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(
            users = userList,
            onDeleteClick = { user -> deleteUser(user) },
            onEditClick = { user -> editUser(user) },
            onResetPasswordClick = { user -> resetPassword(user) }
        )

        binding.rvUsers.apply {
            layoutManager = LinearLayoutManager(this@UserManagementActivity)
            adapter = userAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnCreateUser.setOnClickListener {
            showCreateUserDialog()
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadUsers()
        }
    }

    private fun loadUsers() {
        binding.progressBar.visibility = android.view.View.VISIBLE

        ApiClient.getApiService(this).getUsers().enqueue(object : Callback<UsersResponse> {
            override fun onResponse(call: Call<UsersResponse>, response: Response<UsersResponse>) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.swipeRefresh.isRefreshing = false

                if (response.isSuccessful) {
                    response.body()?.let { usersResponse ->
                        userList.clear()
                        userList.addAll(usersResponse.users)
                        userAdapter.notifyDataSetChanged()

                        binding.tvUserCount.text = "Total: ${usersResponse.total} users"

                        if (usersResponse.users.isEmpty()) {
                            Toast.makeText(this@UserManagementActivity, "No users found", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this@UserManagementActivity, "Server error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UsersResponse>, t: Throwable) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(this@UserManagementActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun editUser(user: User) {
        user.id?.let { userId ->
            val intent = Intent(this, EditUserActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivityForResult(intent, REQUEST_EDIT)
        }
    }

    private fun resetPassword(user: User) {
        user.id?.let { userId ->
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("Reset password for '${user.username}'?")
                .setPositiveButton("Reset") { _, _ ->
                    executePasswordReset(userId, user.username)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun executePasswordReset(userId: Int, username: String) {
        binding.progressBar.visibility = android.view.View.VISIBLE

        ApiClient.getApiService(this).resetUserPassword(userId).enqueue(object : Callback<PasswordResetResponse> {
            override fun onResponse(call: Call<PasswordResetResponse>, response: Response<PasswordResetResponse>) {
                binding.progressBar.visibility = android.view.View.GONE

                if (response.isSuccessful) {
                    response.body()?.let { result ->
                        showPasswordDialog(username, result.newPassword)
                    }
                } else {
                    Toast.makeText(this@UserManagementActivity, "Password reset failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PasswordResetResponse>, t: Throwable) {
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this@UserManagementActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showPasswordDialog(username: String, newPassword: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Password Reset Successful")
            .setMessage("New password for $username:\n\n$newPassword\n\nPlease share this with the user.")
            .setPositiveButton("Copy") { _, _ ->
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("password", newPassword)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Password copied", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("OK", null)
            .show()
    }

    private fun showCreateUserDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        val input = android.widget.EditText(this)
        input.hint = "Enter username"
        builder.setView(input)

        builder.setTitle("Create User")
        builder.setPositiveButton("Create") { _, _ ->
            val username = input.text.toString()
            if (username.isNotEmpty()) {
                val email = "$username@netflixpp.com"
                val password = "password123"
                val newUser = CreateUserRequest(username, password, email, "user")
                createUser(newUser)
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun createUser(user: CreateUserRequest) {
        binding.progressBar.visibility = android.view.View.VISIBLE

        ApiClient.getApiService(this).createUser(user).enqueue(object : Callback<ApiResponse<User>> {
            override fun onResponse(call: Call<ApiResponse<User>>, response: Response<ApiResponse<User>>) {
                binding.progressBar.visibility = android.view.View.GONE

                if (response.isSuccessful) {
                    Toast.makeText(this@UserManagementActivity, "User created successfully", Toast.LENGTH_SHORT).show()
                    loadUsers()
                } else {
                    Toast.makeText(this@UserManagementActivity, "Failed to create user", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<User>>, t: Throwable) {
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this@UserManagementActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteUser(user: User) {
        user.id?.let { userId ->
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete user '${user.username}'?")
                .setPositiveButton("Delete") { _, _ ->
                    performDeleteUser(userId)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun performDeleteUser(userId: Int) {
        ApiClient.getApiService(this).deleteUser(userId).enqueue(object : Callback<ApiResponse<String>> {
            override fun onResponse(call: Call<ApiResponse<String>>, response: Response<ApiResponse<String>>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@UserManagementActivity, "User deleted successfully", Toast.LENGTH_SHORT).show()
                    loadUsers()
                } else {
                    Toast.makeText(this@UserManagementActivity, "Failed to delete user", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<String>>, t: Throwable) {
                Toast.makeText(this@UserManagementActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_EDIT && resultCode == RESULT_OK) {
            loadUsers()
        }
    }

    companion object {
        private const val REQUEST_EDIT = 100
    }
}