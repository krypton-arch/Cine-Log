package com.exmple.cinelog.ui.screens

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.exmple.cinelog.data.local.AppDatabase
import com.exmple.cinelog.data.local.entity.Badge
import com.exmple.cinelog.data.local.entity.UserProfile
import com.exmple.cinelog.data.repository.GamificationRepository
import com.exmple.cinelog.data.repository.LogRepository
import com.exmple.cinelog.domain.GamificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val levelName: String = "Cinephile",
    val badges: List<Badge> = emptyList(),
    val totalHours: Int = 0,
    val avgRating: Float = 0f,
    val totalFilms: Int = 0,
    val topGenres: List<Pair<String, Float>> = emptyList()
)

class ProfileViewModel(
    private val gamificationRepository: GamificationRepository,
    private val logRepository: LogRepository,
    private val gamificationManager: GamificationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                gamificationRepository.getUserProfile(),
                gamificationRepository.getAllBadges(),
                logRepository.getAllLogs()
            ) { profile, badges, logs ->
                
                val levelName = profile?.level?.let { gamificationManager.getLevelName(it) } ?: "Cinephile"
                
                var totalMinutes = 0
                var totalRating = 0f
                var countWithRating = 0
                val genreCounts = mutableMapOf<String, Int>()

                logs.forEach { logWithMovie ->
                    totalMinutes += logWithMovie.movie.runtime ?: 0
                    if (logWithMovie.logEntry.rating > 0) {
                        totalRating += logWithMovie.logEntry.rating
                        countWithRating++
                    }
                    val genresList = logWithMovie.movie.genres.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    genresList.forEach { genre ->
                        genreCounts[genre] = genreCounts.getOrDefault(genre, 0) + 1
                    }
                }

                val totalHours = totalMinutes / 60
                val avgRating = if (countWithRating > 0) totalRating / countWithRating else 0f
                val topGenres = genreCounts.entries
                    .sortedByDescending { it.value }
                    .take(4)
                    .map { it.key to (it.value.toFloat() / maxOf(1, genreCounts.values.sum())) * 100 }

                ProfileUiState(
                    userProfile = profile,
                    levelName = levelName,
                    badges = badges,
                    totalHours = totalHours,
                    avgRating = avgRating,
                    totalFilms = logs.size,
                    topGenres = topGenres
                )
            }.catch { /* Handle error */ }
            .collect { state ->
                _uiState.value = state
            }
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                val db = AppDatabase.getDatabase(application)
                val gamificationRepo = GamificationRepository(db.gamificationDao(), db.userProfileDao())
                val logRepo = LogRepository(db.logDao(), db.movieDao())
                val gamificationManager = GamificationManager(gamificationRepo, logRepo)
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(gamificationRepo, logRepo, gamificationManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
