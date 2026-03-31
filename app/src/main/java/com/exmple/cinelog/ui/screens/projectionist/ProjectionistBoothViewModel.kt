package com.exmple.cinelog.ui.screens.projectionist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exmple.cinelog.data.repository.AiConversationMessage
import com.exmple.cinelog.data.repository.AiRepository
import com.exmple.cinelog.data.repository.GeminiRepository
import com.exmple.cinelog.data.repository.ArchiveGamificationRepository
import com.exmple.cinelog.data.repository.LogRepository
import com.exmple.cinelog.data.repository.WatchlistRepository
import com.exmple.cinelog.domain.ProjectionistContext
import com.exmple.cinelog.domain.PromptAssembler
import com.exmple.cinelog.domain.ProjectionistTranscriptLine
import com.exmple.cinelog.ui.screens.ProjectionistStrings
import com.exmple.cinelog.utils.rethrowIfCancellation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

private const val DEFAULT_GREETING =
    "The archive is open, and the projector is already humming. Tell me your mood, a film you love, or the kind of night you're after, and I'll find something worth falling into."

data class Message(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class ProjectionistUiState(
    val messages: List<Message> = listOf(
        Message(
            DEFAULT_GREETING,
            isUser = false
        )
    ),
    val isLoading: Boolean = false,
    val inputText: String = ""
)

@HiltViewModel
class ProjectionistBoothViewModel @Inject constructor(
    private val aiRepository: AiRepository,
    private val geminiRepository: GeminiRepository,
    private val logRepository: LogRepository,
    private val watchlistRepository: WatchlistRepository,
    private val archiveGamificationRepository: ArchiveGamificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectionistUiState())
    val uiState: StateFlow<ProjectionistUiState> = _uiState.asStateFlow()
    private val contextState: StateFlow<ProjectionistContext> = combine(
        logRepository.getAllLogs(),
        watchlistRepository.getAllWatchlistItems(),
        archiveGamificationRepository.getUserProfile()
    ) { logWithMovies, watchlist, profile ->
        buildContext(logWithMovies, watchlist, profile)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ProjectionistContext.EMPTY
    )

    init {
        viewModelScope.launch {
            aiRepository.getBoothConversation()
                .catch { error ->
                    error.rethrowIfCancellation()
                    Log.e("ProjectionistBoothViewModel", "Failed to restore booth conversation", error)
                }
                .collect { persistedMessages ->
                    _uiState.update { state ->
                        state.copy(messages = persistedMessages.toUiMessages().ifEmpty { defaultMessages() })
                    }
                }
        }
    }

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val userMessage = _uiState.value.inputText.trim()
        if (userMessage.isEmpty() || _uiState.value.isLoading) return
        val previousMessages = _uiState.value.messages
        val queuedMessages = previousMessages + Message(userMessage, isUser = true)

        _uiState.update {
            it.copy(
                messages = queuedMessages,
                inputText = "",
                isLoading = true
            )
        }

        viewModelScope.launch {
            try {
                aiRepository.saveBoothConversation(queuedMessages.toStoredMessages())
                val systemPrompt = PromptAssembler.build(
                    context = contextState.value,
                    conversationHistory = previousMessages.toTranscriptLines()
                )
                val result = geminiRepository.sendMessage(systemPrompt, userMessage)
                val finalMessages = queuedMessages + Message(
                    result.getOrElse { error -> mapError(error) },
                    isUser = false
                )

                aiRepository.saveBoothConversation(finalMessages.toStoredMessages())
                _uiState.update { state -> state.copy(messages = finalMessages, isLoading = false) }
            } catch (error: Throwable) {
                error.rethrowIfCancellation()
                val fallbackMessages = queuedMessages + Message(ProjectionistStrings.SERVER_ERROR, isUser = false)
                aiRepository.saveBoothConversation(fallbackMessages.toStoredMessages())
                _uiState.update { it.copy(messages = fallbackMessages, isLoading = false) }
            }
        }
    }

    private fun buildContext(
        logWithMovies: List<com.exmple.cinelog.data.local.dao.LogWithMovie>,
        watchlist: List<com.exmple.cinelog.data.local.dao.WatchlistItemWithMovie>,
        profile: com.exmple.cinelog.data.local.entity.UserProfile?
    ): ProjectionistContext {
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

    private fun List<Message>.toStoredMessages(): List<AiConversationMessage> {
        return map { message ->
            AiConversationMessage(
                text = message.text,
                isUser = message.isUser,
                timestamp = message.timestamp
            )
        }
    }

    private fun List<AiConversationMessage>.toUiMessages(): List<Message> {
        return map { message ->
            Message(
                text = message.text,
                isUser = message.isUser,
                timestamp = message.timestamp
            )
        }
    }

    private fun List<Message>.toTranscriptLines(): List<ProjectionistTranscriptLine> {
        return filterNot(::isDefaultGreeting)
            .map { message ->
                ProjectionistTranscriptLine(
                    speaker = if (message.isUser) "User" else "The Projectionist",
                    text = message.text
                )
            }
    }

    private fun isDefaultGreeting(message: Message): Boolean {
        return !message.isUser && message.text == DEFAULT_GREETING
    }

    private fun defaultMessages(): List<Message> {
        return listOf(Message(DEFAULT_GREETING, isUser = false))
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
