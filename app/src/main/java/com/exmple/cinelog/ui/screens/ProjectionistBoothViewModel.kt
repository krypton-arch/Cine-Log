package com.exmple.cinelog.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exmple.cinelog.data.repository.GeminiRepository
import com.exmple.cinelog.data.repository.LogRepository
import com.exmple.cinelog.data.repository.WatchlistRepository
import com.exmple.cinelog.data.repository.GamificationRepository
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
        Message("The archive is open. What shadows are we chasing today?", isUser = false)
    ),
    val isLoading: Boolean = false,
    val inputText: String = ""
)

@HiltViewModel
class ProjectionistBoothViewModel @Inject constructor(
    private val geminiRepository: GeminiRepository,
    private val logRepository: LogRepository,
    private val watchlistRepository: WatchlistRepository,
    private val gamificationRepository: GamificationRepository
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
                val systemPrompt = PromptAssembler.build(context, userMessage)
                
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
        val logs = logRepository.getAllLogs().first().map { it.logEntry }
        val watchlist = watchlistRepository.getWatchlist().first()
        val profile = gamificationRepository.getUserProfile().first()
        
        // Simple top genre/decade logic based on logs
        val topGenre = logs.groupBy { it.genre }.maxByOrNull { it.value.size }?.key ?: "Unknown"
        val favoriteDecade = logs.groupBy { 
            val year = it.releaseDate.split("-").firstOrNull()?.toIntOrNull() ?: 0
            if (year > 0) "${(year / 10) * 10}s" else "Unknown"
        }.maxByOrNull { it.value.size }?.key ?: "Unknown"

        return ProjectionistContext(
            recentLogs = logs.take(10),
            topGenre = topGenre,
            topDirector = "Unknown", // Would need director info in LogEntry for more depth
            favoriteDecade = favoriteDecade,
            watchlistTop5 = watchlist.take(5),
            totalFilmsLogged = profile?.totalFilmsWatched ?: 0
        )
    }

    private fun mapError(e: Throwable): String {
        return when (e) {
            is IOException -> ProjectionistStrings.OFFLINE
            else -> {
                if (e.message?.contains("429") == true) ProjectionistStrings.RATE_LIMITED
                else ProjectionistStrings.SERVER_ERROR
            }
        }
    }
}
