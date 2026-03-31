package com.exmple.cinelog.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exmple.cinelog.data.remote.RemoteMovie
import com.exmple.cinelog.data.remote.RetrofitClient
import com.exmple.cinelog.data.repository.LogRepository
import com.exmple.cinelog.data.repository.WatchlistRepository
import com.exmple.cinelog.utils.rethrowIfCancellation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val logRepository: LogRepository,
    private val watchlistRepository: WatchlistRepository
) : ViewModel() {

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
            } catch (error: Throwable) {
                error.rethrowIfCancellation()
                Log.e("HomeViewModel", "Trending fetch failed", error)
            }
        }
        viewModelScope.launch {
            try {
                _popularMovies.value = api.getPopularMovies().results
            } catch (error: Throwable) {
                error.rethrowIfCancellation()
                Log.e("HomeViewModel", "Popular fetch failed", error)
            }
        }
        viewModelScope.launch {
            try {
                _nowPlayingMovies.value = api.getNowPlayingMovies().results
            } catch (error: Throwable) {
                error.rethrowIfCancellation()
                Log.e("HomeViewModel", "NowPlaying fetch failed", error)
            }
        }
        viewModelScope.launch {
            try {
                _topRatedMovies.value = api.getTopRatedMovies().results
            } catch (error: Throwable) {
                error.rethrowIfCancellation()
                Log.e("HomeViewModel", "TopRated fetch failed", error)
            }
        }
    }

    private fun loadLocalStats() {
        viewModelScope.launch {
            logRepository.getTotalFilmsWatched()
                .catch { error -> error.rethrowIfCancellation() }
                .collect { _totalFilmsLogged.value = it }
        }
        viewModelScope.launch {
            logRepository.getTotalMinutesWatched()
                .catch { error -> error.rethrowIfCancellation() }
                .collect { _totalMinutesLogged.value = it ?: 0 }
        }
        viewModelScope.launch {
            watchlistRepository.getWatchlistCount()
                .catch { error -> error.rethrowIfCancellation() }
                .collect { _watchlistCount.value = it }
        }
    }
}
