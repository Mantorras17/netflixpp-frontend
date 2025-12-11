package com.netflixpp_streaming.model

data class MovieResponse(
    val movies: List<Movie>,
    val total: Int,
    val pages: Int,
    val limit: Int,
    val page: Int
)
