package com.exmple.cinelog.ui.screens

import com.exmple.cinelog.data.local.dao.LogWithMovie
import com.exmple.cinelog.data.local.dao.WatchlistItemWithMovie
import com.exmple.cinelog.data.repository.GeminiRepository
import com.exmple.cinelog.data.repository.LogRepository
import com.exmple.cinelog.data.repository.WatchlistRepository
import com.exmple.cinelog.data.repository.ArchiveGamificationRepository
import com.exmple.cinelog.data.local.entity.UserProfile
import com.exmple.cinelog.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
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

    private val geminiRepo = mockk<GeminiRepository>()
    private val logRepo = mockk<LogRepository>()
    private val watchlistRepo = mockk<WatchlistRepository>()
    private val archiveGamificationRepo = mockk<ArchiveGamificationRepository>()

    private val viewModel by lazy {
        ProjectionistBoothViewModel(geminiRepo, logRepo, watchlistRepo, archiveGamificationRepo)
    }

    @Test
    fun `sendMessage success updates messages with result`() = runTest {
        // Arrange
        val userMsg = "What should I watch?"
        val aiResponse = "The archive demands you see Nosferatu."
        
        coEvery { logRepo.getAllLogs() } returns flowOf(emptyList<LogWithMovie>())
        coEvery { watchlistRepo.getAllWatchlistItems() } returns flowOf(emptyList<WatchlistItemWithMovie>())
        coEvery { archiveGamificationRepo.getUserProfile() } returns flowOf(null as UserProfile?)
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
    }

    @Test
    fun `sendMessage failure updates messages with mapped error`() = runTest {
        // Arrange
        val userMsg = "Error test"
        
        coEvery { logRepo.getAllLogs() } returns flowOf(emptyList<LogWithMovie>())
        coEvery { watchlistRepo.getAllWatchlistItems() } returns flowOf(emptyList<WatchlistItemWithMovie>())
        coEvery { archiveGamificationRepo.getUserProfile() } returns flowOf(null as UserProfile?)
        coEvery { geminiRepo.sendMessage(any(), userMsg) } returns Result.failure(Exception("429"))

        // Act
        viewModel.onInputChanged(userMsg)
        viewModel.sendMessage()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(ProjectionistStrings.RATE_LIMITED, state.messages.last().text)
    }
}
