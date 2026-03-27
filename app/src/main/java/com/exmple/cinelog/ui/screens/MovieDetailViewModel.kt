package com.exmple.cinelog.ui.screens

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.exmple.cinelog.data.local.AppDatabase
import com.exmple.cinelog.data.local.entity.MovieEntity
import com.exmple.cinelog.data.remote.MovieDetailResponse
import com.exmple.cinelog.data.remote.RetrofitClient
import com.exmple.cinelog.data.repository.WatchlistRepository
import com.exmple.cinelog.data.local.entity.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MovieDetailUiState(
    val isLoading: Boolean = true,
    val detail: MovieDetailResponse? = null,
    val isInWatchlist: Boolean = false,
    val error: String? = null
)

class MovieDetailViewModel(
    private val movieId: Int,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(MovieDetailUiState())
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    init {
        fetchDetails()
    }

    private fun fetchDetails() {
        viewModelScope.launch {
            try {
                val detail = RetrofitClient.apiService.getMovieDetails(movieId)
                _uiState.value = MovieDetailUiState(isLoading = false, detail = detail)
            } catch (e: Exception) {
                Log.e("MovieDetail", "Failed to fetch details", e)
                _uiState.value = MovieDetailUiState(isLoading = false, error = e.message)
            }
        }
    }

    fun addToWatchlist() {
        val detail = _uiState.value.detail ?: return
        viewModelScope.launch {
            val db = AppDatabase.getDatabase(application)
            val repo = WatchlistRepository(db.watchlistDao(), db.movieDao())
            val entity = MovieEntity(
                movieId = detail.id,
                title = detail.title,
                posterPath = detail.poster_path,
                releaseYear = detail.release_date?.take(4),
                genres = detail.genres.joinToString(", ") { it.name },
                runtime = detail.runtime,
                overview = detail.overview
            )
            repo.addToWatchlist(entity, Priority.CASUAL)
            _uiState.value = _uiState.value.copy(isInWatchlist = true)
        }
    }

    fun toMovieEntity(): MovieEntity? {
        val detail = _uiState.value.detail ?: return null
        return MovieEntity(
            movieId = detail.id,
            title = detail.title,
            posterPath = detail.poster_path,
            releaseYear = detail.release_date?.take(4),
            genres = detail.genres.joinToString(", ") { it.name },
            runtime = detail.runtime,
            overview = detail.overview
        )
    }

    class Factory(private val movieId: Int, private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MovieDetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MovieDetailViewModel(movieId, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
