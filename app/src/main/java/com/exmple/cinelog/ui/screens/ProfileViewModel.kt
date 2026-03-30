package com.exmple.cinelog.ui.screens

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.exmple.cinelog.data.local.AppDatabase
import com.exmple.cinelog.data.local.entity.Badge
import com.exmple.cinelog.data.local.entity.Challenge
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
    val topGenres: List<Pair<String, Float>> = emptyList(),
    val activeChallenge: Challenge? = null,
    val favoriteDecade: String = "N/A",
    val topDirector: String = "N/A"
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
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
                logRepository.getAllLogs(),
                gamificationRepository.getActiveChallenges()
            ) { profile, badges, logs, challenges ->
                
                val levelName = profile?.level?.let { gamificationManager.getLevelName(it) } ?: "Cinephile"
                
                var totalMinutes = 0
                var totalRating = 0f
                var countWithRating = 0
                val genreCounts = mutableMapOf<String, Int>()
                val directorCounts = mutableMapOf<String, Int>()
                val yearCounts = mutableMapOf<String, Int>()
                val decadeCounts = mutableMapOf<Int, Int>()

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

                    val director = logWithMovie.movie.director
                    if (!director.isNullOrBlank()) {
                        directorCounts[director] = directorCounts.getOrDefault(director, 0) + 1
                    }

                    val yearStr = logWithMovie.movie.releaseYear
                    if (!yearStr.isNullOrBlank()) {
                        yearCounts[yearStr] = yearCounts.getOrDefault(yearStr, 0) + 1
                        val year = yearStr.toIntOrNull()
                        if (year != null && year > 1800) {
                            val decade = (year / 10) * 10
                            decadeCounts[decade] = decadeCounts.getOrDefault(decade, 0) + 1
                        }
                    }
                }

                val totalHours = totalMinutes / 60
                val avgRating = if (countWithRating > 0) totalRating / countWithRating else 0f
                val topGenres = genreCounts.entries
                    .sortedByDescending { it.value }
                    .take(4)
                    .map { it.key to (it.value.toFloat() / maxOf(1, genreCounts.values.sum())) * 100 }

                val topYear = yearCounts.maxByOrNull { it.value }?.key ?: "NONE"
                val topDirector = directorCounts.maxByOrNull { it.value }?.key ?: "NONE"
                val favoriteDecade = decadeCounts.maxByOrNull { it.value }?.key?.let { "${it}s" } ?: "NONE"
                val activeChallenge = challenges.firstOrNull()

                ProfileUiState(
                    userProfile = profile,
                    levelName = levelName,
                    badges = badges,
                    totalHours = totalHours,
                    avgRating = avgRating,
                    totalFilms = logs.size,
                    topGenres = topGenres,
                    activeChallenge = activeChallenge,
                    favoriteDecade = favoriteDecade,
                    topDirector = topDirector
                )
            }.catch { /* Handle error */ }
            .collect { state ->
                _uiState.value = state
            }
        }
    }

}
