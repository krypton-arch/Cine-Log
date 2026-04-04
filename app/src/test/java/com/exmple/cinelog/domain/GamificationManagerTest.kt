package com.exmple.cinelog.domain

import com.exmple.cinelog.data.local.entity.Badge
import com.exmple.cinelog.data.local.entity.Challenge
import com.exmple.cinelog.data.local.entity.LogEntry
import com.exmple.cinelog.data.local.entity.MovieEntity
import com.exmple.cinelog.data.local.dao.LogWithMovie
import com.exmple.cinelog.data.local.entity.UserProfile
import com.exmple.cinelog.data.repository.ArchiveGamificationRepository
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
import java.time.YearMonth
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class GamificationManagerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockLogRepo = mockk<LogRepository>(relaxed = true)
    private val mockGamificationRepo = mockk<ArchiveGamificationRepository>(relaxed = true)
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
    fun `checkBadges unlocks old_soul badge when release year includes full date`() = runTest {
        val profile = UserProfile(xp = 0, level = 1, currentStreak = 0, lastLogDate = null, totalFilmsWatched = 0, totalMinutesWatched = 0)
        val logs = List(10) {
            LogWithMovie(
                logEntry = LogEntry(movieId = it, watchDate = 0, rating = 4f, review = "", moodTag = null, isRewatch = false),
                movie = MovieEntity(
                    movieId = it,
                    title = "Movie $it",
                    posterPath = null,
                    releaseYear = "1975-03-22",
                    genres = "",
                    runtime = null,
                    director = null,
                    overview = null
                )
            )
        }
        val badge = Badge("old_soul", "Old Soul", "", "badge_old_soul", false, 0)

        coEvery { mockGamificationRepo.getUnlockedBadges() } returns flowOf(emptyList())
        coEvery { mockLogRepo.getAllLogs() } returns flowOf(logs)
        coEvery { mockGamificationRepo.getAllBadges() } returns flowOf(listOf(badge))
        coEvery { mockGamificationRepo.getUserProfile() } returns flowOf(profile)

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
        val zoneId = ZoneId.systemDefault()
        val currentMonth = YearMonth.now(zoneId)
        val challenge = MonthlyChallengeEngine.challengeForMonth(currentMonth, zoneId)
        val logs = sampleMonthlyLogs(currentMonth, zoneId)
        val expectedProgress = MonthlyChallengeEngine.evaluateProgress(challenge, logs, zoneId)

        coEvery { mockGamificationRepo.getChallengeById(challenge.challengeId) } returns challenge
        coEvery { mockLogRepo.getAllLogs() } returns flowOf(logs)

        manager.checkChallenges()

        coVerify { mockGamificationRepo.updateChallengeProgress(challenge, expectedProgress.coerceIn(0, challenge.targetCount)) }
    }

    private fun sampleMonthlyLogs(yearMonth: YearMonth, zoneId: ZoneId): List<LogWithMovie> {
        val firstSaturday = generateSequence(yearMonth.atDay(1)) { it.plusDays(1) }
            .first { it.month == yearMonth.month && it.dayOfWeek.value == 6 }
        val firstSunday = generateSequence(firstSaturday) { it.plusDays(1) }
            .first { it.month == yearMonth.month && it.dayOfWeek.value == 7 }
        val dayThree = yearMonth.atDay(3)
        val dayFour = yearMonth.atDay(4)
        val dayFive = yearMonth.atDay(5)

        return listOf(
            sampleLog(movieId = 1, watchDate = firstSaturday.atStartOfDay(zoneId).toInstant().toEpochMilli(), review = "Great", genres = "Drama", runtime = 320, releaseYear = "1975-03-22"),
            sampleLog(movieId = 2, watchDate = firstSunday.atStartOfDay(zoneId).toInstant().toEpochMilli(), review = "Sharp", genres = "Comedy", runtime = 280, releaseYear = "1968-10-29"),
            sampleLog(movieId = 3, watchDate = dayThree.atStartOfDay(zoneId).toInstant().toEpochMilli(), review = "Layered", genres = "Sci-Fi", runtime = 240, releaseYear = "1979-08-17"),
            sampleLog(movieId = 4, watchDate = dayFour.atStartOfDay(zoneId).toInstant().toEpochMilli(), review = "Moody", genres = "Thriller", runtime = 210, releaseYear = "1984-06-08"),
            sampleLog(movieId = 5, watchDate = dayFive.atStartOfDay(zoneId).toInstant().toEpochMilli(), review = "", genres = "Romance", runtime = 190, releaseYear = "1994-01-01")
        )
    }

    private fun sampleLog(
        movieId: Int,
        watchDate: Long,
        review: String,
        genres: String,
        runtime: Int,
        releaseYear: String
    ): LogWithMovie {
        return LogWithMovie(
            logEntry = LogEntry(
                movieId = movieId,
                watchDate = watchDate,
                rating = 4f,
                review = review,
                moodTag = null,
                isRewatch = false
            ),
            movie = MovieEntity(
                movieId = movieId,
                title = "Movie $movieId",
                posterPath = null,
                releaseYear = releaseYear,
                genres = genres,
                runtime = runtime,
                director = null,
                overview = null
            )
        )
    }
}
