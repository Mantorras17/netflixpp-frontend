package com.netflixpp_streaming.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.netflixpp_streaming.R
import com.netflixpp_streaming.adapter.RecentSearchAdapter
import com.netflixpp_streaming.adapter.SearchResultAdapter
import com.netflixpp_streaming.api.ApiClient
import com.netflixpp_streaming.databinding.ActivitySearchBinding
import com.netflixpp_streaming.model.Movie
import com.netflixpp_streaming.util.Prefs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var searchResultAdapter: SearchResultAdapter
    private lateinit var recentSearchAdapter: RecentSearchAdapter
    private lateinit var popularSearchAdapter: RecentSearchAdapter
    
    private val searchResults = mutableListOf<Movie>()
    private val recentSearches = mutableListOf<String>()
    private val popularSearches = mutableListOf<String>()
    
    private var currentQuery = ""
    private var selectedGenre: String? = null
    private var selectedYear: Int? = null
    private var selectedMinRating: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupRecyclerViews()
        setupClickListeners()
        loadRecentSearches()
        loadPopularSearches()
    }

    private fun setupUI() {
        // Focus search box and show keyboard
        binding.etSearch.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun setupRecyclerViews() {
        // Search results
        searchResultAdapter = SearchResultAdapter(searchResults) { movie ->
            openMovieDetail(movie)
        }
        binding.rvSearchResults.apply {
            layoutManager = GridLayoutManager(this@SearchActivity, 1)
            adapter = searchResultAdapter
        }

        // Recent searches
        recentSearchAdapter = RecentSearchAdapter(
            searches = recentSearches,
            onSearchClick = { query ->
                binding.etSearch.setText(query)
                performSearch(query)
            },
            onDeleteClick = { query ->
                removeRecentSearch(query)
            }
        )
        binding.rvRecentSearches.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = recentSearchAdapter
        }

        // Popular searches
        popularSearchAdapter = RecentSearchAdapter(
            searches = popularSearches,
            onSearchClick = { query ->
                binding.etSearch.setText(query)
                performSearch(query)
            },
            onDeleteClick = null // No delete for popular searches
        )
        binding.rvPopularSearches.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = popularSearchAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.text?.clear()
        }

        binding.btnFilter.setOnClickListener {
            toggleFilters()
        }

        binding.chipGenre.setOnClickListener {
            showGenreDialog()
        }

        binding.chipYear.setOnClickListener {
            showYearDialog()
        }

        binding.chipRating.setOnClickListener {
            showRatingDialog()
        }

        binding.chipClearFilters.setOnClickListener {
            clearFilters()
        }

        // Search text watcher
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnClearSearch.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Search action
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    performSearch(query)
                }
                true
            } else false
        }
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            showRecentSearches()
            return
        }

        currentQuery = query
        hideKeyboard()
        
        binding.progressBar.visibility = View.VISIBLE
        binding.llRecentSearches.visibility = View.GONE
        binding.rvSearchResults.visibility = View.GONE
        binding.emptyState.visibility = View.GONE

        // Save to recent searches
        saveRecentSearch(query)

        // Perform API search
        ApiClient.getApiService(this).searchMovies(
            query = query,
            genre = selectedGenre,
            year = selectedYear,
            minRating = selectedMinRating
        ).enqueue(object : Callback<List<Movie>> {
            override fun onResponse(call: Call<List<Movie>>, response: Response<List<Movie>>) {
                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val results = response.body() ?: emptyList()
                    displaySearchResults(results)
                } else {
                    Toast.makeText(this@SearchActivity, "Search failed", Toast.LENGTH_SHORT).show()
                    showEmptyState("Search failed. Please try again.")
                }
            }

            override fun onFailure(call: Call<List<Movie>>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@SearchActivity, "Network error", Toast.LENGTH_SHORT).show()
                
                // Fallback to local search
                performLocalSearch(query)
            }
        })
    }

    private fun performLocalSearch(query: String) {
        // If API fails, search through cached movies
        // This is a simplified implementation
        showEmptyState("Unable to search. Please check your connection.")
    }

    private fun displaySearchResults(results: List<Movie>) {
        searchResults.clear()
        searchResults.addAll(results)
        searchResultAdapter.notifyDataSetChanged()

        if (results.isEmpty()) {
            showEmptyState("No results for \"$currentQuery\"")
        } else {
            binding.rvSearchResults.visibility = View.VISIBLE
            binding.emptyState.visibility = View.GONE
        }
    }

    private fun showRecentSearches() {
        binding.llRecentSearches.visibility = View.VISIBLE
        binding.rvSearchResults.visibility = View.GONE
        binding.emptyState.visibility = View.GONE
    }

    private fun showEmptyState(message: String) {
        binding.emptyState.visibility = View.VISIBLE
        binding.tvEmptyMessage.text = message
        binding.llRecentSearches.visibility = View.GONE
        binding.rvSearchResults.visibility = View.GONE
    }

    private fun toggleFilters() {
        val isVisible = binding.chipScrollView.visibility == View.VISIBLE
        binding.chipScrollView.visibility = if (isVisible) View.GONE else View.VISIBLE
    }

    private fun showGenreDialog() {
        val genres = arrayOf(
            "All Genres", "Action", "Animation", "Comedy", "Drama", 
            "Horror", "Sci-Fi", "Thriller", "Documentary"
        )

        val currentIndex = genres.indexOf(selectedGenre ?: "All Genres")

        AlertDialog.Builder(this)
            .setTitle("Select Genre")
            .setSingleChoiceItems(genres, currentIndex) { dialog, which ->
                selectedGenre = if (which == 0) null else genres[which]
                updateFilterChip(binding.chipGenre, selectedGenre)
                dialog.dismiss()
                if (currentQuery.isNotEmpty()) {
                    performSearch(currentQuery)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showYearDialog() {
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val years = (currentYear downTo 1900).map { it.toString() }.toTypedArray()
        val yearsWithAll = arrayOf("All Years") + years

        val currentIndex = if (selectedYear == null) 0 else years.indexOf(selectedYear.toString()) + 1

        AlertDialog.Builder(this)
            .setTitle("Select Year")
            .setSingleChoiceItems(yearsWithAll, currentIndex) { dialog, which ->
                selectedYear = if (which == 0) null else yearsWithAll[which].toInt()
                updateFilterChip(binding.chipYear, selectedYear?.toString())
                dialog.dismiss()
                if (currentQuery.isNotEmpty()) {
                    performSearch(currentQuery)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRatingDialog() {
        val ratings = arrayOf("All Ratings", "4.5+", "4.0+", "3.5+", "3.0+", "2.5+")
        val ratingValues = arrayOf(null, 4.5, 4.0, 3.5, 3.0, 2.5)

        val currentIndex = ratingValues.indexOf(selectedMinRating)

        AlertDialog.Builder(this)
            .setTitle("Minimum Rating")
            .setSingleChoiceItems(ratings, currentIndex) { dialog, which ->
                selectedMinRating = ratingValues[which]
                updateFilterChip(binding.chipRating, if (which == 0) null else ratings[which])
                dialog.dismiss()
                if (currentQuery.isNotEmpty()) {
                    performSearch(currentQuery)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateFilterChip(chip: com.google.android.material.chip.Chip, value: String?) {
        chip.isChecked = value != null
        if (value != null) {
            chip.text = value
        }
    }

    private fun clearFilters() {
        selectedGenre = null
        selectedYear = null
        selectedMinRating = null
        
        binding.chipGenre.isChecked = false
        binding.chipGenre.text = "Genre"
        binding.chipYear.isChecked = false
        binding.chipYear.text = "Year"
        binding.chipRating.isChecked = false
        binding.chipRating.text = "Rating"

        if (currentQuery.isNotEmpty()) {
            performSearch(currentQuery)
        }
    }

    private fun loadRecentSearches() {
        val prefs = Prefs.getSharedPreferences(this)
        val savedSearches = prefs.getStringSet("recent_searches", emptySet()) ?: emptySet()
        recentSearches.clear()
        recentSearches.addAll(savedSearches.take(10))
        recentSearchAdapter.notifyDataSetChanged()
    }

    private fun saveRecentSearch(query: String) {
        val prefs = Prefs.getSharedPreferences(this)
        val savedSearches = prefs.getStringSet("recent_searches", emptySet())?.toMutableSet() ?: mutableSetOf()
        
        savedSearches.remove(query) // Remove if exists
        savedSearches.add(query) // Add to front
        
        // Keep only last 20 searches
        val recentList = savedSearches.toList().takeLast(20).toSet()
        
        prefs.edit().putStringSet("recent_searches", recentList).apply()
        
        loadRecentSearches()
    }

    private fun removeRecentSearch(query: String) {
        val prefs = Prefs.getSharedPreferences(this)
        val savedSearches = prefs.getStringSet("recent_searches", emptySet())?.toMutableSet() ?: mutableSetOf()
        savedSearches.remove(query)
        prefs.edit().putStringSet("recent_searches", savedSearches).apply()
        
        loadRecentSearches()
    }

    private fun loadPopularSearches() {
        // In a real app, fetch from API
        popularSearches.clear()
        popularSearches.addAll(listOf(
            "Horror",
            "Comedy",
            "Action",
            "Animation",
            "Classic Movies"
        ))
        popularSearchAdapter.notifyDataSetChanged()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
    }

    private fun openMovieDetail(movie: Movie) {
        val intent = Intent(this, MovieDetailActivity::class.java).apply {
            putExtra("movie", movie)
        }
        startActivity(intent)
    }
}