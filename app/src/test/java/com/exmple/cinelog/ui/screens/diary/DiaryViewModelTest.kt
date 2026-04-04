package com.exmple.cinelog.ui.screens.diary

import com.exmple.cinelog.data.local.dao.LogWithMovie
import com.exmple.cinelog.data.local.entity.LogEntry
import com.exmple.cinelog.data.local.entity.MovieEntity
import com.exmple.cinelog.data.repository.LogRepository
import com.exmple.cinelog.domain.GamificationManager
import com.exmple.cinelog.utils.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.YearMonth
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class DiaryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<LogRepository>(relaxed = true)
    private val gamificationManager = mockk<GamificationManager>(relaxed = true)

    @Test
    fun `updateLogEntry does not sync monthly challenge`() = runTest {
        every { repository.getAllLogs() } returns flowOf(emptyList())
        val viewModel = DiaryViewModel(repository, gamificationManager)
        val entry = LogEntry(
            logId = 7,
            movieId = 1,
            watchDate = 123L,
            rating = 4f,
            review = "Updated review",
            moodTag = null,
            isRewatch = false
        )

        viewModel.updateLogEntry(entry)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.updateLogEntry(entry) }
        coVerify(exactly = 0) { gamificationManager.syncMonthlyChallenge(ZoneId.systemDefault()) }
    }

    @Test
    fun `month stats include logs through the final millisecond of the month`() = runTest {
        val zoneId = ZoneId.systemDefault()
        val yearMonth = YearMonth.of(2026, 4)
        val endExclusive = yearMonth.plusMonths(1).atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val logs = listOf(
            logWithMovie(movieId = 1, watchDate = endExclusive - 1, rating = 4f),
            logWithMovie(movieId = 2, watchDate = endExclusive, rating = 5f)
        )
        every { repository.getAllLogs() } returns flowOf(logs)

        val viewModel = DiaryViewModel(repository, gamificationManager)
        advanceUntilIdle()

        viewModel.onMonthChanged(yearMonth)

        assertEquals(1, viewModel.uiState.value.monthStats.totalLogged)
        assertEquals(1, viewModel.uiState.value.monthLogs[yearMonth.lengthOfMonth()]?.size)
    }

    private fun logWithMovie(movieId: Int, watchDate: Long, rating: Float): LogWithMovie {
        return LogWithMovie(
            logEntry = LogEntry(
                movieId = movieId,
                watchDate = watchDate,
                rating = rating,
                review = null,
                moodTag = null,
                isRewatch = false
            ),
            movie = MovieEntity(
                movieId = movieId,
                title = "Movie $movieId",
                posterPath = null,
                releaseYear = "2026",
                genres = "Drama",
                runtime = 100,
                director = null,
                overview = null
            )
        )
    }
}
