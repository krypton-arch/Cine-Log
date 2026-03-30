package com.exmple.cinelog.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.exmple.cinelog.data.local.dao.WatchlistItemWithMovie
import com.exmple.cinelog.data.local.entity.MovieEntity
import com.exmple.cinelog.data.local.entity.Priority
import com.exmple.cinelog.data.remote.MovieApiService
import com.exmple.cinelog.data.remote.RemoteMovie
import com.exmple.cinelog.data.remote.RetrofitClient
import com.exmple.cinelog.data.repository.WatchlistRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val repository: WatchlistRepository,
    private val apiService: MovieApiService = RetrofitClient.apiService
) : ViewModel() {

    private val _watchlist = MutableStateFlow<List<WatchlistItemWithMovie>>(emptyList())
    val watchlist: StateFlow<List<WatchlistItemWithMovie>> = _watchlist.asStateFlow()

    private val _searchResults = MutableStateFlow<List<RemoteMovie>>(emptyList())
    val searchResults: StateFlow<List<RemoteMovie>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            repository.getAllWatchlistItems()
                .catch { error ->
                    Log.e("WatchlistViewModel", "Failed to load watchlist", error)
                    _watchlist.value = emptyList()
                }
                .collect { items ->
                    _watchlist.value = items
                }
        }
    }

    fun searchMovies(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            _isSearching.value = true
            delay(500) // Debounce
            try {
                val response = apiService.searchMovies(query)
                _searchResults.value = response.results
            } catch (e: Exception) {
                Log.e("WatchlistViewModel", "Movie search failed", e)
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun addToWatchlist(remoteMovie: RemoteMovie, priority: Priority = Priority.CASUAL) {
        viewModelScope.launch {
            val movieEntity = MovieEntity(
                movieId = remoteMovie.id,
                title = remoteMovie.title,
                posterPath = remoteMovie.poster_path,
                releaseYear = remoteMovie.release_date?.take(4),
                genres = "",
                runtime = 0,
                director = null,
                overview = remoteMovie.overview ?: ""
            )
            runCatching {
                repository.addToWatchlist(movieEntity, priority)
            }.onFailure { error ->
                Log.e("WatchlistViewModel", "Failed to add movie to watchlist", error)
            }
        }
    }
    
    fun removeFromWatchlist(item: WatchlistItemWithMovie) {
        viewModelScope.launch {
            repository.removeFromWatchlist(item.watchlistEntry)
        }
    }

}
