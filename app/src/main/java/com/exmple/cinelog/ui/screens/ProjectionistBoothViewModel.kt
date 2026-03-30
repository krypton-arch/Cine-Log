package com.exmple.cinelog.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exmple.cinelog.data.repository.GeminiRepository
import com.exmple.cinelog.data.repository.LogRepository
import com.exmple.cinelog.data.repository.WatchlistRepository
import com.exmple.cinelog.data.repository.ArchiveGamificationRepository
import com.exmple.cinelog.domain.ProjectionistContext
import com.exmple.cinelog.domain.PromptAssembler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

data class Message(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class ProjectionistUiState(
    val messages: List<Message> = listOf(
        Message(
            "The archive is open, and the projector is already humming. Tell me your mood, a film you love, or the kind of night you're after, and I'll find something worth falling into.",
            isUser = false
        )
    ),
    val isLoading: Boolean = false,
    val inputText: String = ""
)

@HiltViewModel
class ProjectionistBoothViewModel @Inject constructor(
    private val geminiRepository: GeminiRepository,
    private val logRepository: LogRepository,
    private val watchlistRepository: WatchlistRepository,
    private val archiveGamificationRepository: ArchiveGamificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectionistUiState())
    val uiState: StateFlow<ProjectionistUiState> = _uiState.asStateFlow()

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val userMessage = _uiState.value.inputText.trim()
        if (userMessage.isEmpty() || _uiState.value.isLoading) return

        _uiState.update { 
            it.copy(
                messages = it.messages + Message(userMessage, isUser = true),
                inputText = "",
                isLoading = true
            )
        }

        viewModelScope.launch {
            try {
                val context = buildContext()
                val systemPrompt = PromptAssembler.build(context)
                
                val result = geminiRepository.sendMessage(systemPrompt, userMessage)
                
                _uiState.update { state ->
                    state.copy(
                        messages = state.messages + Message(
                            result.getOrElse { e -> mapError(e) },
                            isUser = false
                        ),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    messages = it.messages + Message(ProjectionistStrings.SERVER_ERROR, isUser = false),
                    isLoading = false
                )}
            }
        }
    }

    private suspend fun buildContext(): ProjectionistContext {
        val logWithMovies = logRepository.getAllLogs().first()
        val watchlist = watchlistRepository.getAllWatchlistItems().first()
        val profile = archiveGamificationRepository.getUserProfile().first()
        
        val topGenre = logWithMovies
            .flatMap { it.movie.genres.split(",").map { g -> g.trim() } }
            .filter { it.isNotEmpty() }
            .groupBy { it }
            .maxByOrNull { it.value.size }?.key ?: "Unknown"

        val favoriteDecade = logWithMovies.groupBy { 
            val year = it.movie.releaseYear?.toIntOrNull() ?: 0
            if (year > 0) "${(year / 10) * 10}s" else "Unknown"
        }.maxByOrNull { it.value.size }?.key ?: "Unknown"

        val topDirector = logWithMovies
            .mapNotNull { it.movie.director }
            .filter { it.isNotBlank() }
            .groupBy { it }
            .maxByOrNull { it.value.size }?.key ?: "Unknown"

        return ProjectionistContext(
            recentLogs = logWithMovies.take(10).map { it.movie.title },
            topGenre = topGenre,
            topDirector = topDirector,
            favoriteDecade = favoriteDecade,
            watchlistTop5 = watchlist.take(5).map { it.movie },
            totalFilmsLogged = profile?.totalFilmsWatched ?: 0
        )
    }

    private fun mapError(e: Throwable): String {
        return when (e) {
            is IOException -> ProjectionistStrings.OFFLINE
            else -> {
                when {
                    e.message?.contains("429") == true -> ProjectionistStrings.RATE_LIMITED
                    e.message?.contains("Gemini relay", ignoreCase = true) == true -> ProjectionistStrings.SECURE_RELAY_UNAVAILABLE
                    else -> ProjectionistStrings.SERVER_ERROR
                }
            }
        }
    }
}
