package com.exmple.cinelog.ui.screens

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.exmple.cinelog.data.local.AppDatabase
import com.exmple.cinelog.data.remote.RemoteMovie
import com.exmple.cinelog.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class HomeViewModel(private val application: Application) : ViewModel() {

    private val _trendingMovies = MutableStateFlow<List<RemoteMovie>>(emptyList())
    val trendingMovies: StateFlow<List<RemoteMovie>> = _trendingMovies.asStateFlow()

    private val _popularMovies = MutableStateFlow<List<RemoteMovie>>(emptyList())
    val popularMovies: StateFlow<List<RemoteMovie>> = _popularMovies.asStateFlow()

    private val _nowPlayingMovies = MutableStateFlow<List<RemoteMovie>>(emptyList())
    val nowPlayingMovies: StateFlow<List<RemoteMovie>> = _nowPlayingMovies.asStateFlow()

    private val _topRatedMovies = MutableStateFlow<List<RemoteMovie>>(emptyList())
    val topRatedMovies: StateFlow<List<RemoteMovie>> = _topRatedMovies.asStateFlow()

    private val _totalFilmsLogged = MutableStateFlow(0)
    val totalFilmsLogged: StateFlow<Int> = _totalFilmsLogged.asStateFlow()

    private val _totalMinutesLogged = MutableStateFlow(0)
    val totalMinutesLogged: StateFlow<Int> = _totalMinutesLogged.asStateFlow()

    private val _watchlistCount = MutableStateFlow(0)
    val watchlistCount: StateFlow<Int> = _watchlistCount.asStateFlow()

    init {
        fetchAllCategories()
        loadLocalStats()
    }

    private fun fetchAllCategories() {
        val api = RetrofitClient.apiService

        // Each category fetched independently so one failure doesn't kill the others
        viewModelScope.launch {
            try {
                _trendingMovies.value = api.getTrendingMovies().results
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Trending fetch failed", e)
            }
        }
        viewModelScope.launch {
            try {
                _popularMovies.value = api.getPopularMovies().results
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Popular fetch failed", e)
            }
        }
        viewModelScope.launch {
            try {
                _nowPlayingMovies.value = api.getNowPlayingMovies().results
            } catch (e: Exception) {
                Log.e("HomeViewModel", "NowPlaying fetch failed", e)
            }
        }
        viewModelScope.launch {
            try {
                _topRatedMovies.value = api.getTopRatedMovies().results
            } catch (e: Exception) {
                Log.e("HomeViewModel", "TopRated fetch failed", e)
            }
        }
    }

    private fun loadLocalStats() {
        val db = AppDatabase.getDatabase(application)
        viewModelScope.launch {
            db.logDao().getTotalFilmsWatched()
                .catch { /* ignore */ }
                .collect { _totalFilmsLogged.value = it }
        }
        viewModelScope.launch {
            db.logDao().getTotalMinutesWatched()
                .catch { /* ignore */ }
                .collect { _totalMinutesLogged.value = it ?: 0 }
        }
        viewModelScope.launch {
            db.watchlistDao().getWatchlistCount()
                .catch { /* ignore */ }
                .collect { _watchlistCount.value = it }
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
