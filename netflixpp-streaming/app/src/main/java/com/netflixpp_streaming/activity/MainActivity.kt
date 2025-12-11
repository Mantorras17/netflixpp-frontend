package com.netflixpp_streaming.activity

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.netflixpp_streaming.R
import com.netflixpp_streaming.adapter.CategoryAdapter
import com.netflixpp_streaming.api.ApiClient
import com.netflixpp_streaming.databinding.ActivityMainBinding
import com.netflixpp_streaming.model.Category
import com.netflixpp_streaming.model.Movie
import com.netflixpp_streaming.model.MovieResponse
import com.netflixpp_streaming.util.Prefs
import com.netflixpp_streaming.util.MovieUtils
import com.netflixpp_streaming.util.NetworkUtils
import com.netflixpp_streaming.view.ErrorStateView
import com.netflixpp_streaming.view.EmptyStateView
import com.netflixpp_streaming.view.LoadingView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var loadingView: LoadingView
    private lateinit var errorStateView: ErrorStateView
    private lateinit var emptyStateView: EmptyStateView
    
    private val categories = mutableListOf<Category>()
    private val allMovies = mutableListOf<Movie>()
    private var isInitialLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupErrorHandlingViews()
        setupUI()
        setupRecyclerView()
        setupClickListeners()
        loadMovies()
    }

    private fun setupErrorHandlingViews() {
        // Initialize error handling views
        loadingView = binding.loadingView
        errorStateView = binding.errorStateView
        emptyStateView = binding.emptyStateView
        
        // Setup retry callbacks
        errorStateView.setOnRetryClickListener {
            loadMovies()
        }
        
        emptyStateView.setOnActionClickListener {
            // Refresh when empty state action is clicked
            loadMovies()
        }
    }

    private fun setupUI() {
        val user = Prefs.getUser(this)
        
        // Search button click
        binding.btnSearch.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            // Simple fade transition for search
            val options = ActivityOptionsCompat.makeCustomAnimation(
                this,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            startActivity(intent, options.toBundle())
        }

        // Profile click
        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
        }
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(categories) { movie, sharedView ->
            // Pass the shared element view for transition
            openMovieDetail(movie, sharedView)
        }

        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = categoryAdapter
        }
    }

    private fun setupClickListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            loadMovies()
        }

        binding.navigationBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_browse -> {
                    // Already on browse
                    true
                }
                R.id.nav_my_list -> {
                    val intent = Intent(this, MyListActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    true
                }
                R.id.nav_mesh -> {
                    val intent = Intent(this, MeshActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    true
                }
                R.id.nav_downloads -> {
                    val intent = Intent(this, DownloadsActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    true
                }
                else -> false
            }
        }
    }

    private fun loadMovies() {
        // Check network connectivity first
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showNetworkError()
            return
        }

        showLoading()

        ApiClient.getApiService(this).getMovies().enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                hideLoading()

                if (response.isSuccessful) {
                    val movieResponse = response.body()
                    val movies = movieResponse?.movies
                    
                    if (movies.isNullOrEmpty()) {
                        showEmptyState()
                    } else {
                        allMovies.clear()
                        allMovies.addAll(movies)
                        organizeMoviesByCategory(movies)
                        setupHeroSection()
                        showContent()
                        isInitialLoad = false
                    }
                } else {
                    when (response.code()) {
                        401 -> showAuthError()
                        403 -> showPermissionError()
                        404 -> showNotFoundError()
                        500, 502, 503 -> showServerError()
                        else -> showGenericError("Error ${'$'}{response.code()}: ${'$'}{response.message()}")
                    }
                }
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                hideLoading()
                
                when (t) {
                    is UnknownHostException -> showNetworkError()
                    is SocketTimeoutException -> showTimeoutError()
                    else -> showGenericError(t.message ?: "Unknown error occurred")
                }
            }
        })
    }

    private fun organizeMoviesByCategory(movies: List<Movie>) {
        categories.clear()

        // Group by genre
        val genreGroups = movies.groupBy { it.genre }

        genreGroups.forEach { (genre, genreMovies) ->
            categories.add(Category(title = genre, movies = genreMovies))
        }

        // Add trending category
        if (movies.isNotEmpty()) {
            val trendingMovies = movies.take(10)
            categories.add(0, Category(title = "Trending Now", movies = trendingMovies))
        }

        categoryAdapter.notifyDataSetChanged()
    }

    private fun setupHeroSection() {
        if (allMovies.isEmpty()) {
            binding.heroSection.visibility = View.GONE
            return
        }
        
        binding.heroSection.visibility = View.VISIBLE
        
        // Get featured movie (highest rated or first)
        val featuredMovie = allMovies.maxByOrNull { it.rating } ?: allMovies.first()
        
        binding.tvHeroTitle.text = featuredMovie.title
        binding.tvHeroRating.text = String.format("%.1f", featuredMovie.rating)
        binding.tvHeroYear.text = featuredMovie.year.toString()
        binding.tvHeroGenre.text = featuredMovie.genre
        binding.tvHeroDescription.text = featuredMovie.description
        
        // Load hero background
        MovieUtils.loadThumbnail(binding.ivHeroBackground, featuredMovie.thumbnailUrl)
        
        binding.btnHeroPlay.setOnClickListener {
            openMoviePlayer(featuredMovie, binding.ivHeroBackground)
        }
        
        binding.btnHeroInfo.setOnClickListener {
            openMovieDetail(featuredMovie, binding.ivHeroBackground)
        }
        
        // Animate hero entrance only on initial load
        if (isInitialLoad) {
            animateHeroEntrance()
        }
    }

    private fun animateHeroEntrance() {
        binding.ivHeroBackground.alpha = 0f
        binding.tvHeroTitle.alpha = 0f
        binding.tvHeroDescription.alpha = 0f
        
        binding.ivHeroBackground.animate()
            .alpha(1f)
            .setDuration(500)
            .start()
        
        binding.tvHeroTitle.animate()
            .alpha(1f)
            .setStartDelay(200)
            .setDuration(400)
            .start()
        
        binding.tvHeroDescription.animate()
            .alpha(1f)
            .setStartDelay(400)
            .setDuration(400)
            .start()
    }

    /**
     * Open movie detail with shared element transition
     * @param movie The movie to display
     * @param sharedView The ImageView that will be shared between activities
     */
    private fun openMovieDetail(movie: Movie, sharedView: android.view.View) {
        val intent = Intent(this, MovieDetailActivity::class.java).apply {
            putExtra("movie", movie)
        }
        
        // Create shared element transition
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this,
            sharedView,
            "movie_thumbnail" // This must match the transitionName in MovieDetailActivity
        )
        
        startActivity(intent, options.toBundle())
    }

    /**
     * Open movie player with scale animation
     * @param movie The movie to play
     * @param sharedView The view to animate from
     */
    private fun openMoviePlayer(movie: Movie, sharedView: android.view.View) {
        val intent = Intent(this, MoviePlayerActivity::class.java).apply {
            putExtra("movie", movie)
        }
        
        // Scale up animation for player
        val options = ActivityOptions.makeScaleUpAnimation(
            sharedView,
            0,
            0,
            sharedView.width,
            sharedView.height
        )
        
        startActivity(intent, options.toBundle())
    }

    // ========== UI State Management ==========
    
    private fun showLoading() {
        binding.swipeRefresh.isRefreshing = false
        errorStateView.hide()
        emptyStateView.hide()
        
        if (categories.isEmpty()) {
            // Show full loading view if no content
            binding.heroSection.visibility = View.GONE
            binding.rvCategories.visibility = View.GONE
            loadingView.show(getString(R.string.loading_movies))
        } else {
            // Show shimmer in existing content
            loadingView.hide()
        }
    }
    
    private fun hideLoading() {
        binding.swipeRefresh.isRefreshing = false
        loadingView.hide()
    }
    
    private fun showContent() {
        loadingView.hide()
        errorStateView.hide()
        emptyStateView.hide()
        binding.heroSection.visibility = View.VISIBLE
        binding.rvCategories.visibility = View.VISIBLE
    }
    
    private fun showNetworkError() {
        hideLoading()
        binding.heroSection.visibility = View.GONE
        binding.rvCategories.visibility = View.GONE
        emptyStateView.hide()
        
        errorStateView.showNetworkError()
    }
    
    private fun showServerError() {
        hideLoading()
        binding.heroSection.visibility = View.GONE
        binding.rvCategories.visibility = View.GONE
        emptyStateView.hide()
        
        errorStateView.showServerError()
    }
    
    private fun showTimeoutError() {
        hideLoading()
        binding.heroSection.visibility = View.GONE
        binding.rvCategories.visibility = View.GONE
        emptyStateView.hide()
        
        errorStateView.showError(
            title = getString(R.string.error_timeout_title),
            message = getString(R.string.error_timeout_message),
            iconRes = R.drawable.ic_no_wifi
        )
    }
    
    private fun showAuthError() {
        hideLoading()
        Toast.makeText(this, getString(R.string.error_auth), Toast.LENGTH_LONG).show()
        
        // Redirect to login
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
    
    private fun showPermissionError() {
        hideLoading()
        binding.heroSection.visibility = View.GONE
        binding.rvCategories.visibility = View.GONE
        emptyStateView.hide()
        
        errorStateView.showError(
            title = getString(R.string.error_permission_title),
            message = getString(R.string.error_permission_message),
            iconRes = R.drawable.ic_server_error,
            showRetryButton = false
        )
    }
    
    private fun showNotFoundError() {
        hideLoading()
        binding.heroSection.visibility = View.GONE
        binding.rvCategories.visibility = View.GONE
        emptyStateView.hide()
        
        errorStateView.showError(
            title = getString(R.string.error_not_found_title),
            message = getString(R.string.error_not_found_message),
            iconRes = R.drawable.ic_empty_box
        )
    }
    
    private fun showGenericError(message: String) {
        hideLoading()
        binding.heroSection.visibility = View.GONE
        binding.rvCategories.visibility = View.GONE
        emptyStateView.hide()
        
        errorStateView.showError(
            title = getString(R.string.error_generic_title),
            message = message,
            iconRes = R.drawable.ic_server_error
        )
    }
    
    private fun showEmptyState() {
        hideLoading()
        binding.heroSection.visibility = View.GONE
        binding.rvCategories.visibility = View.GONE
        errorStateView.hide()
        
        emptyStateView.showNoMovies()
    }
}
