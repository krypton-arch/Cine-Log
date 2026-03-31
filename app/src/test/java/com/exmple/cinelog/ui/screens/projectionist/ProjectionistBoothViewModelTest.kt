package com.exmple.cinelog.ui.screens.projectionist

import com.exmple.cinelog.data.local.dao.LogWithMovie
import com.exmple.cinelog.data.local.dao.WatchlistItemWithMovie
import com.exmple.cinelog.data.repository.AiConversationMessage
import com.exmple.cinelog.data.repository.AiRepository
import com.exmple.cinelog.data.repository.GeminiRepository
import com.exmple.cinelog.data.repository.LogRepository
import com.exmple.cinelog.data.repository.WatchlistRepository
import com.exmple.cinelog.data.repository.ArchiveGamificationRepository
import com.exmple.cinelog.data.local.entity.UserProfile
import com.exmple.cinelog.ui.screens.ProjectionistStrings
import com.exmple.cinelog.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.every
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectionistBoothViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val aiRepository = mockk<AiRepository>()
    private val geminiRepo = mockk<GeminiRepository>()
    private val logRepo = mockk<LogRepository>()
    private val watchlistRepo = mockk<WatchlistRepository>()
    private val archiveGamificationRepo = mockk<ArchiveGamificationRepository>()

    private val viewModel by lazy {
        ProjectionistBoothViewModel(aiRepository, geminiRepo, logRepo, watchlistRepo, archiveGamificationRepo)
    }

    @Test
    fun `sendMessage success updates messages with result`() = runTest {
        // Arrange
        val userMsg = "What should I watch?"
        val aiResponse = "The archive demands you see Nosferatu."
        
        every { aiRepository.getBoothConversation() } returns flowOf(emptyList())
        coEvery { aiRepository.saveBoothConversation(any()) } returns Unit
        every { logRepo.getAllLogs() } returns flowOf(emptyList<LogWithMovie>())
        every { watchlistRepo.getAllWatchlistItems() } returns flowOf(emptyList<WatchlistItemWithMovie>())
        every { archiveGamificationRepo.getUserProfile() } returns flowOf(null as UserProfile?)
        coEvery { geminiRepo.sendMessage(any(), userMsg) } returns Result.success(aiResponse)

        // Act
        viewModel.onInputChanged(userMsg)
        viewModel.sendMessage()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(3, state.messages.size) // Greeting + User + AI
        assertEquals(aiResponse, state.messages.last().text)
        assertFalse(state.messages.last().isUser)
        assertFalse(state.isLoading)
        coVerify(exactly = 2) { aiRepository.saveBoothConversation(any()) }
    }

    @Test
    fun `sendMessage failure updates messages with mapped error`() = runTest {
        // Arrange
        val userMsg = "Error test"
        
        every { aiRepository.getBoothConversation() } returns flowOf(emptyList())
        coEvery { aiRepository.saveBoothConversation(any()) } returns Unit
        every { logRepo.getAllLogs() } returns flowOf(emptyList<LogWithMovie>())
        every { watchlistRepo.getAllWatchlistItems() } returns flowOf(emptyList<WatchlistItemWithMovie>())
        every { archiveGamificationRepo.getUserProfile() } returns flowOf(null as UserProfile?)
        coEvery { geminiRepo.sendMessage(any(), userMsg) } returns Result.failure(Exception("429"))

        // Act
        viewModel.onInputChanged(userMsg)
        viewModel.sendMessage()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(ProjectionistStrings.RATE_LIMITED, state.messages.last().text)
    }

    @Test
    fun `init restores persisted conversation from repository`() = runTest {
        val persistedMessage = AiConversationMessage(
            text = "The booth remembers.",
            isUser = false,
            timestamp = 123L
        )

        every { aiRepository.getBoothConversation() } returns flowOf(listOf(persistedMessage))
        every { logRepo.getAllLogs() } returns flowOf(emptyList<LogWithMovie>())
        every { watchlistRepo.getAllWatchlistItems() } returns flowOf(emptyList<WatchlistItemWithMovie>())
        every { archiveGamificationRepo.getUserProfile() } returns flowOf(null as UserProfile?)

        val restoredViewModel = ProjectionistBoothViewModel(
            aiRepository,
            geminiRepo,
            logRepo,
            watchlistRepo,
            archiveGamificationRepo
        )

        advanceUntilIdle()

        assertEquals(listOf("The booth remembers."), restoredViewModel.uiState.value.messages.map { it.text })
    }
}
