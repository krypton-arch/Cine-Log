package com.exmple.cinelog.domain

import com.exmple.cinelog.data.local.entity.Badge
import com.exmple.cinelog.data.local.entity.Challenge
import com.exmple.cinelog.data.local.entity.LogEntry
import com.exmple.cinelog.data.local.entity.MovieEntity
import com.exmple.cinelog.data.local.dao.LogWithMovie
import com.exmple.cinelog.data.local.entity.UserProfile
import com.exmple.cinelog.data.repository.GamificationRepository
import com.exmple.cinelog.data.repository.LogRepository
import com.exmple.cinelog.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class GamificationManagerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockLogRepo = mockk<LogRepository>(relaxed = true)
    private val mockGamificationRepo = mockk<GamificationRepository>(relaxed = true)
    private val manager = GamificationManager(mockGamificationRepo, mockLogRepo)

    @Test
    fun `calculateLevel returns correct level based on xp`() {
        assertEquals(1, manager.calculateLevel(0))
        assertEquals(1, manager.calculateLevel(499))
        assertEquals(2, manager.calculateLevel(500))
        assertEquals(3, manager.calculateLevel(1000))
        assertEquals(4, manager.calculateLevel(2000))
        assertEquals(5, manager.calculateLevel(4000))
    }

    @Test
    fun `processMovieLog adds base XP correctly`() = runTest {
        val initialProfile = UserProfile(xp = 0, level = 1, currentStreak = 0, lastLogDate = null, totalFilmsWatched = 0, totalMinutesWatched = 0)
        coEvery { mockGamificationRepo.getUserProfile() } returns flowOf(initialProfile)
        
        val logEntry = LogEntry(movieId = 1, watchDate = System.currentTimeMillis(), rating = 5f, review = "", moodTag = null, isRewatch = false)

        manager.processMovieLog(logEntry, hasReview = false, wasOnWatchlist = false)

        coVerify { 
            mockGamificationRepo.updateProfile(match { 
                it.xp == 10 && it.currentStreak == 1 && it.lastLogDate == LocalDate.now().toString() 
            }) 
        }
    }

    @Test
    fun `processMovieLog adds bonus XP for review and watchlist`() = runTest {
        val initialProfile = UserProfile(xp = 0, level = 1, currentStreak = 0, lastLogDate = null, totalFilmsWatched = 0, totalMinutesWatched = 0)
        coEvery { mockGamificationRepo.getUserProfile() } returns flowOf(initialProfile)
        
        val logEntry = LogEntry(movieId = 1, watchDate = System.currentTimeMillis(), rating = 5f, review = "Great", moodTag = null, isRewatch = false)

        manager.processMovieLog(logEntry, hasReview = true, wasOnWatchlist = true)

        coVerify { 
            mockGamificationRepo.updateProfile(match { 
                it.xp == 45 // 10 (base) + 20 (review) + 15 (watchlist)
            }) 
        }
    }

    @Test
    fun `processMovieLog updates streak correctly when consecutive day log`() = runTest {
        val yesterday = LocalDate.now().minusDays(1).toString()
        val initialProfile = UserProfile(xp = 10, level = 1, currentStreak = 1, lastLogDate = yesterday, totalFilmsWatched = 1, totalMinutesWatched = 120)
        coEvery { mockGamificationRepo.getUserProfile() } returns flowOf(initialProfile)
        
        val logEntry = LogEntry(movieId = 2, watchDate = System.currentTimeMillis(), rating = 5f, review = "", moodTag = null, isRewatch = false)

        manager.processMovieLog(logEntry, hasReview = false, wasOnWatchlist = false)

        coVerify { 
            mockGamificationRepo.updateProfile(match { 
                it.xp == 25 && it.currentStreak == 2 // 10 (initial) + 10 (log) + 5 (streak)
            }) 
        }
    }

    @Test
    fun `checkBadges unlocks first_log badge`() = runTest {
        val profile = UserProfile(xp = 0, level = 1, currentStreak = 0, lastLogDate = null, totalFilmsWatched = 0, totalMinutesWatched = 0)
        val logs = listOf(LogWithMovie(logEntry = LogEntry(movieId = 1, watchDate = 0, rating = 5f, review = "", moodTag = null, isRewatch = false), movie = MovieEntity(movieId = 1, title = "A", posterPath = null, releaseYear = null, genres = "", runtime = null, director = null, overview = null)))
        val badge = Badge("first_log", "First Frame", "", "badge1", false, 0)
        
        coEvery { mockGamificationRepo.getUnlockedBadges() } returns flowOf(emptyList())
        coEvery { mockLogRepo.getAllLogs() } returns flowOf(logs)
        coEvery { mockGamificationRepo.getAllBadges() } returns flowOf(listOf(badge))
        coEvery { mockGamificationRepo.getUserProfile() } returns flowOf(profile)

        manager.checkBadges(profile)

        coVerify { mockGamificationRepo.unlockBadge(badge) }
        coVerify { mockGamificationRepo.updateProfile(match { it.xp == 50 }) }
    }

    @Test
    fun `checkBadges unlocks horror_fiend badge`() = runTest {
        val profile = UserProfile(xp = 0, level = 1, currentStreak = 0, lastLogDate = null, totalFilmsWatched = 0, totalMinutesWatched = 0)
        val logs = List(10) { 
            LogWithMovie(
                logEntry = LogEntry(movieId = it, watchDate = 0, rating = 5f, review = "", moodTag = null, isRewatch = false),
                movie = MovieEntity(movieId = it, title = "Movie $it", genres = "Horror", posterPath = null, releaseYear = null, runtime = null, director = null, overview = null)
            ) 
        }
        val badge = Badge("horror_fiend", "Horror Fiend", "", "badge2", false, 0)
        
        coEvery { mockGamificationRepo.getUnlockedBadges() } returns flowOf(emptyList())
        coEvery { mockLogRepo.getAllLogs() } returns flowOf(logs)
        coEvery { mockGamificationRepo.getAllBadges() } returns flowOf(listOf(badge))

        manager.checkBadges(profile)

        coVerify { mockGamificationRepo.unlockBadge(badge) }
    }

    @Test
    fun `checkBadges unlocks binge_king badge`() = runTest {
        val profile = UserProfile(xp = 0, level = 1, currentStreak = 0, lastLogDate = null, totalFilmsWatched = 0, totalMinutesWatched = 0)
        val recentDate = System.currentTimeMillis() - 10000 // Very recent
        val logs = List(5) { 
            LogWithMovie(
                logEntry = LogEntry(movieId = it, watchDate = recentDate, rating = 5f, review = "", moodTag = null, isRewatch = false),
                movie = MovieEntity(movieId = it, title = "Movie $it", posterPath = null, releaseYear = null, genres = "", runtime = null, director = null, overview = null)
            ) 
        }
        val badge = Badge("binge_king", "Binge King", "", "badge3", false, 0)
        
        coEvery { mockGamificationRepo.getUnlockedBadges() } returns flowOf(emptyList())
        coEvery { mockLogRepo.getAllLogs() } returns flowOf(logs)
        coEvery { mockGamificationRepo.getAllBadges() } returns flowOf(listOf(badge))

        manager.checkBadges(profile)

        coVerify { mockGamificationRepo.unlockBadge(badge) }
    }

    @Test
    fun `checkChallenges updates progress correctly`() = runTest {
        val challenge = Challenge("review_streak", "Review Streak", "", 10, 0, null, false)
        val logs = List(3) { 
            LogWithMovie(
                logEntry = LogEntry(movieId = it, watchDate = 0, rating = 5f, review = "Good", moodTag = null, isRewatch = false),
                movie = MovieEntity(movieId = it, title = "Movie $it", posterPath = null, releaseYear = null, genres = "", runtime = null, director = null, overview = null)
            ) 
        }
        
        coEvery { mockGamificationRepo.getActiveChallenges() } returns flowOf(listOf(challenge))
        coEvery { mockLogRepo.getAllLogs() } returns flowOf(logs)

        manager.checkChallenges()

        coVerify { mockGamificationRepo.updateChallengeProgress(challenge, 3) }
    }
}
