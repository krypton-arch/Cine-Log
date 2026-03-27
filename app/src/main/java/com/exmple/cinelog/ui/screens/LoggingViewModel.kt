package com.exmple.cinelog.ui.screens

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.exmple.cinelog.data.local.AppDatabase
import com.exmple.cinelog.data.local.entity.LogEntry
import com.exmple.cinelog.data.local.entity.MovieEntity
import com.exmple.cinelog.data.repository.GamificationRepository
import com.exmple.cinelog.data.repository.LogRepository
import com.exmple.cinelog.domain.GamificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

class LoggingViewModel(
    private val logRepository: LogRepository,
    private val gamificationManager: GamificationManager
) : ViewModel() {

    private val _rating = MutableStateFlow(0f)
    val rating: StateFlow<Float> = _rating.asStateFlow()

    private val _reviewText = MutableStateFlow("")
    val reviewText: StateFlow<String> = _reviewText.asStateFlow()

    private val _selectedAtmosphere = MutableStateFlow<String?>(null)
    val selectedAtmosphere: StateFlow<String?> = _selectedAtmosphere.asStateFlow()

    fun updateRating(newRating: Float) {
        _rating.value = newRating
    }

    fun updateReviewText(text: String) {
        _reviewText.value = text
    }

    fun toggleAtmosphere(tag: String) {
        if (_selectedAtmosphere.value == tag) {
            _selectedAtmosphere.value = null
        } else {
            _selectedAtmosphere.value = tag
        }
    }

    fun logMovie(movie: MovieEntity, wasOnWatchlist: Boolean, onComplete: () -> Unit) {
        viewModelScope.launch {
            val entry = LogEntry(
                movieId = movie.movieId,
                watchDate = Instant.now().toEpochMilli(),
                rating = _rating.value,
                review = _reviewText.value,
                moodTag = _selectedAtmosphere.value,
                isRewatch = false
            )
            
            // 1. Insert into logs
            logRepository.logMovie(movie, entry)
            
            // 2. Trigger Gamification
            val hasReview = _reviewText.value.isNotBlank()
            gamificationManager.processMovieLog(
                logEntry = entry,
                hasReview = hasReview,
                wasOnWatchlist = wasOnWatchlist
            )
            
            // 3. Update challenge progress
            gamificationManager.checkChallenges()
            
            onComplete()
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoggingViewModel::class.java)) {
                val db = AppDatabase.getDatabase(application)
                val logRepo = LogRepository(db.logDao(), db.movieDao())
                val gamificationRepo = GamificationRepository(db.gamificationDao(), db.userProfileDao())
                val gamificationManager = GamificationManager(gamificationRepo, logRepo)
                @Suppress("UNCHECKED_CAST")
                return LoggingViewModel(logRepo, gamificationManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
