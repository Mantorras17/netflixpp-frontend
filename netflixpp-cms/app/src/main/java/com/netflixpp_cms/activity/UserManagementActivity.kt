package com.netflixpp_cms.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.netflixpp_cms.adapter.UserAdapter
import com.netflixpp_cms.api.ApiClient
import com.netflixpp_cms.databinding.ActivityUserManagementBinding
import com.netflixpp_cms.model.ApiResponse
import com.netflixpp_cms.model.User
import com.netflixpp_cms.util.Prefs
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
        userAdapter = UserAdapter(userList) { user ->
            deleteUser(user)
        }

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

        ApiClient.getApiService(this).getUsers().enqueue(object : Callback<ApiResponse<List<User>>> {
            override fun onResponse(call: Call<ApiResponse<List<User>>>, response: Response<ApiResponse<List<User>>>) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.swipeRefresh.isRefreshing = false

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        userList.clear()
                        apiResponse.data?.let { users ->
                            userList.addAll(users)
                            userAdapter.notifyDataSetChanged()

                            if (users.isEmpty()) {
                                Toast.makeText(this@UserManagementActivity, "No users found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(
                            this@UserManagementActivity,
                            apiResponse?.error ?: "Failed to load users",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this@UserManagementActivity, "Server error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<User>>>, t: Throwable) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(this@UserManagementActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showCreateUserDialog() {
        val username = "user${System.currentTimeMillis()}"
        val email = "$username@netflixpp.com"
        val password = "password123"

        val newUser = User(
            username = username,
            email = email,
            password = password,
            role = "USER"
        )

        createUser(newUser)
    }

    private fun createUser(user: User) {
        binding.progressBar.visibility = android.view.View.VISIBLE

        ApiClient.getApiService(this).createUser(user).enqueue(object : Callback<ApiResponse<User>> {
            override fun onResponse(call: Call<ApiResponse<User>>, response: Response<ApiResponse<User>>) {
                binding.progressBar.visibility = android.view.View.GONE

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        Toast.makeText(this@UserManagementActivity, "User created successfully", Toast.LENGTH_SHORT).show()
                        loadUsers()
                    } else {
                        Toast.makeText(
                            this@UserManagementActivity,
                            apiResponse?.error ?: "Failed to create user",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this@UserManagementActivity, "Server error: ${response.code()}", Toast.LENGTH_SHORT).show()
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
            android.app.AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete user '${user.username}'?")
                .setPositiveButton("Delete") { dialog, which ->
                    performDeleteUser(userId)
                }
                .setNegativeButton("Cancel", null)
                .show()
        } ?: run {
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performDeleteUser(userId: String) {
        ApiClient.getApiService(this).deleteUser(userId).enqueue(object : Callback<ApiResponse<Unit>> {
            override fun onResponse(call: Call<ApiResponse<Unit>>, response: Response<ApiResponse<Unit>>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        Toast.makeText(this@UserManagementActivity, "User deleted successfully", Toast.LENGTH_SHORT).show()
                        loadUsers()
                    } else {
                        Toast.makeText(
                            this@UserManagementActivity,
                            apiResponse?.error ?: "Failed to delete user",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this@UserManagementActivity, "Server error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<Unit>>, t: Throwable) {
                Toast.makeText(this@UserManagementActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}