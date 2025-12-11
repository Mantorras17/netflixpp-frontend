package com.netflixpp_cms.activity

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.netflixpp_cms.adapter.LogAdapter
import com.netflixpp_cms.api.ApiClient
import com.netflixpp_cms.databinding.ActivityLogsBinding
import com.netflixpp_cms.model.LogEntry
import com.netflixpp_cms.model.LogsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LogsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogsBinding
    private lateinit var logAdapter: LogAdapter
    private val logList = mutableListOf<LogEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLogTypeSpinner()
        setupRecyclerView()
        setupClickListeners()
        loadLogs()
    }

    private fun setupLogTypeSpinner() {
        val logTypes = arrayOf("system", "error", "access", "database")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, logTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spLogType.adapter = adapter
    }

    private fun setupRecyclerView() {
        logAdapter = LogAdapter(logList)
        binding.rvLogs.apply {
            layoutManager = LinearLayoutManager(this@LogsActivity)
            adapter = logAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnRefresh.setOnClickListener {
            loadLogs()
        }

        binding.btnFilter.setOnClickListener {
            loadLogs()
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadLogs()
        }
    }

    private fun loadLogs() {
        binding.progressBar.visibility = android.view.View.VISIBLE

        val logType = binding.spLogType.selectedItem.toString()
        val limit = 100

        ApiClient.getApiService(this).getLogs(logType, limit).enqueue(object : Callback<LogsResponse> {
            override fun onResponse(call: Call<LogsResponse>, response: Response<LogsResponse>) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.swipeRefresh.isRefreshing = false

                if (response.isSuccessful) {
                    response.body()?.let { logsResponse ->
                        logList.clear()
                        logList.addAll(logsResponse.logs)
                        logAdapter.notifyDataSetChanged()

                        binding.tvLogCount.text = "Total logs: ${logsResponse.count}"
                        
                        if (logsResponse.logs.isEmpty()) {
                            Toast.makeText(this@LogsActivity, "No logs found", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this@LogsActivity, "Failed to load logs", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LogsResponse>, t: Throwable) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(this@LogsActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}