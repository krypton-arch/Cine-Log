package com.exmple.cinelog.data.remote

import com.exmple.cinelog.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApiService {
    @GET("trending/movie/week")
    suspend fun getTrendingMovies(
        @Query("api_key") apiKey: String = BuildConfig.TMDB_API_KEY
    ): MovieListResponse

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String = BuildConfig.TMDB_API_KEY,
        @Query("page") page: Int = 1
    ): MovieListResponse

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("api_key") apiKey: String = BuildConfig.TMDB_API_KEY,
        @Query("page") page: Int = 1
    ): MovieListResponse

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("api_key") apiKey: String = BuildConfig.TMDB_API_KEY,
        @Query("page") page: Int = 1
    ): MovieListResponse

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String = BuildConfig.TMDB_API_KEY,
        @Query("page") page: Int = 1
    ): MovieListResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("api_key") apiKey: String = BuildConfig.TMDB_API_KEY,
        @Query("page") page: Int = 1
    ): MovieListResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String = BuildConfig.TMDB_API_KEY
    ): MovieDetailResponse
}

data class MovieListResponse(
    val results: List<RemoteMovie>,
    val total_results: Int = 0,
    val total_pages: Int = 0
)

// Keep old name as typealias for backwards compat
typealias TrendingResponse = MovieListResponse

data class RemoteMovie(
    val id: Int,
    val title: String,
    val poster_path: String?,
    val release_date: String?,
    val overview: String?,
    val vote_average: Double = 0.0,
    val vote_count: Int = 0,
    val genre_ids: List<Int> = emptyList()
)

data class MovieDetailResponse(
    val id: Int,
    val title: String,
    val poster_path: String?,
    val release_date: String?,
    val overview: String?,
    val vote_average: Double = 0.0,
    val runtime: Int?,
    val genres: List<Genre> = emptyList()
)

data class Genre(
    val id: Int,
    val name: String
)
