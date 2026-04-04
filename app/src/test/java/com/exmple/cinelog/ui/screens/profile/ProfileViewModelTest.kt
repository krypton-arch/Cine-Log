package com.exmple.cinelog.ui.screens.profile

import com.exmple.cinelog.data.local.dao.LogWithMovie
import com.exmple.cinelog.data.local.dao.WatchlistItemWithMovie
import com.exmple.cinelog.data.local.entity.AiInsightEntity
import com.exmple.cinelog.data.local.entity.MovieEntity
import com.exmple.cinelog.data.local.entity.Priority
import com.exmple.cinelog.data.local.entity.UserProfile
import com.exmple.cinelog.data.local.entity.WatchlistEntry
import com.exmple.cinelog.data.repository.AiRepository
import com.exmple.cinelog.data.repository.ArchiveGamificationRepository
import com.exmple.cinelog.data.repository.GeminiRepository
import com.exmple.cinelog.data.repository.LogRepository
import com.exmple.cinelog.data.repository.WatchlistRepository
import com.exmple.cinelog.domain.GamificationManager
import com.exmple.cinelog.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val archiveRepo = mockk<ArchiveGamificationRepository>()
    private val logRepo = mockk<LogRepository>()
    private val watchlistRepo = mockk<WatchlistRepository>()
    private val gamificationManager = mockk<GamificationManager>()
    private val aiRepository = mockk<AiRepository>()
    private val geminiRepository = mockk<GeminiRepository>()

    @Test
    fun `requestDailyInsight uses recent logs and watchlist titles in prompt`() = runTest {
        val promptSlot = slot<String>()

        coEvery { gamificationManager.syncMonthlyChallenge(any()) } returns Unit
        every { archiveRepo.getUserProfile() } returns flowOf(
            UserProfile(
                xp = 120,
                level = 1,
                currentStreak = 3,
                lastLogDate = null,
                totalFilmsWatched = 1,
                totalMinutesWatched = 120
            )
        )
        every { archiveRepo.getAllBadges() } returns flowOf(emptyList())
        every { archiveRepo.getAllChallenges() } returns flowOf(emptyList())
        every { logRepo.getAllLogs() } returns flowOf(
            listOf(
                loggedMovie(movieId = 1, title = "Recent Favorite", releaseYear = "1975-10-22"),
                loggedMovie(movieId = 2, title = "Second Entry", releaseYear = "1984-06-08")
            )
        )
        every { watchlistRepo.getAllWatchlistItems() } returns flowOf(
            listOf(
                watchlistMovie(movieId = 10, title = "Queued Classic")
            )
        )
        every { aiRepository.getDailyInsight() } returns flowOf(null as AiInsightEntity?)
        coEvery { aiRepository.saveInsight(any(), any()) } returns Unit
        every { gamificationManager.getLevelName(any()) } returns "Cinephile"
        every { gamificationManager.buildCurrentMonthlyChallengeSnapshot(any(), any(), any()) } returns null
        coEvery { geminiRepository.sendMessage(capture(promptSlot), any()) } returns Result.success("Cryptic insight")

        ProfileViewModel(
            archiveGamificationRepository = archiveRepo,
            logRepository = logRepo,
            watchlistRepository = watchlistRepo,
            gamificationManager = gamificationManager,
            aiRepository = aiRepository,
            geminiRepository = geminiRepository
        )

        advanceUntilIdle()

        assertTrue(promptSlot.captured.contains("Films logged recently: Recent Favorite, Second Entry"))
        assertTrue(promptSlot.captured.contains("Watchlist: Queued Classic"))
        assertTrue(promptSlot.captured.contains("Favorite decade: 1970s"))
    }

    @Test
    fun `daily insight refreshes when a newer log exists than cached insight`() = runTest {
        val promptSlot = slot<String>()

        coEvery { gamificationManager.syncMonthlyChallenge(any()) } returns Unit
        every { archiveRepo.getUserProfile() } returns flowOf(
            UserProfile(
                xp = 200,
                level = 1,
                currentStreak = 4,
                lastLogDate = null,
                totalFilmsWatched = 2,
                totalMinutesWatched = 240
            )
        )
        every { archiveRepo.getAllBadges() } returns flowOf(emptyList())
        every { archiveRepo.getAllChallenges() } returns flowOf(emptyList())
        every { logRepo.getAllLogs() } returns flowOf(
            listOf(
                loggedMovie(movieId = 99, title = "Fresh Log", releaseYear = "1999-09-09", watchDate = 5_000L)
            )
        )
        every { watchlistRepo.getAllWatchlistItems() } returns flowOf(emptyList())
        every { aiRepository.getDailyInsight() } returns flowOf(
            AiInsightEntity(
                insightText = "Old insight",
                generatedAt = 4_000L
            )
        )
        coEvery { aiRepository.saveInsight(any(), any()) } returns Unit
        every { gamificationManager.getLevelName(any()) } returns "Cinephile"
        every { gamificationManager.buildCurrentMonthlyChallengeSnapshot(any(), any(), any()) } returns null
        coEvery { geminiRepository.sendMessage(capture(promptSlot), any()) } returns Result.success("Updated insight")

        ProfileViewModel(
            archiveGamificationRepository = archiveRepo,
            logRepository = logRepo,
            watchlistRepository = watchlistRepo,
            gamificationManager = gamificationManager,
            aiRepository = aiRepository,
            geminiRepository = geminiRepository
        )

        advanceUntilIdle()

        coVerify(exactly = 1) { geminiRepository.sendMessage(any(), any()) }
        assertTrue(promptSlot.captured.contains("Films logged recently: Fresh Log"))
    }

    private fun loggedMovie(
        movieId: Int,
        title: String,
        releaseYear: String,
        watchDate: Long = movieId.toLong()
    ): LogWithMovie {
        return LogWithMovie(
            logEntry = com.exmple.cinelog.data.local.entity.LogEntry(
                movieId = movieId,
                watchDate = watchDate,
                rating = 4f,
                review = "Review",
                moodTag = null,
                isRewatch = false
            ),
            movie = MovieEntity(
                movieId = movieId,
                title = title,
                posterPath = null,
                releaseYear = releaseYear,
                genres = "Drama",
                runtime = 120,
                director = "Director $movieId",
                overview = null
            )
        )
    }

    private fun watchlistMovie(movieId: Int, title: String): WatchlistItemWithMovie {
        return WatchlistItemWithMovie(
            watchlistEntry = WatchlistEntry(
                id = movieId,
                movieId = movieId,
                priority = Priority.CASUAL,
                addedDate = movieId.toLong(),
                notes = null
            ),
            movie = MovieEntity(
                movieId = movieId,
                title = title,
                posterPath = null,
                releaseYear = "1962",
                genres = "Drama",
                runtime = 95,
                director = null,
                overview = null
            )
        )
    }
}
