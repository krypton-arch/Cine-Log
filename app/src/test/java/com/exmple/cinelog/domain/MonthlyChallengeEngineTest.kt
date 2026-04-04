package com.exmple.cinelog.domain

import com.exmple.cinelog.data.local.dao.LogWithMovie
import com.exmple.cinelog.data.local.entity.LogEntry
import com.exmple.cinelog.data.local.entity.MovieEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth
import java.time.ZoneId

class MonthlyChallengeEngineTest {

    @Test
    fun `evaluateProgress counts classic films when release year includes full date`() {
        val zoneId = ZoneId.systemDefault()
        val yearMonth = monthForChallengeTitle("Time Capsule")
        val challenge = MonthlyChallengeEngine.challengeForMonth(yearMonth, zoneId)
        val midMonthWatchDate = yearMonth.atDay(15).atStartOfDay(zoneId).toInstant().toEpochMilli()

        val logs = listOf(
            classicLog(movieId = 1, watchDate = midMonthWatchDate, releaseYear = "1975-03-22"),
            classicLog(movieId = 2, watchDate = midMonthWatchDate, releaseYear = "1968-10-29"),
            classicLog(movieId = 3, watchDate = midMonthWatchDate, releaseYear = "1984-06-08")
        )

        val progress = MonthlyChallengeEngine.evaluateProgress(challenge, logs, zoneId)

        assertEquals(2, progress)
    }

    @Test
    fun `evaluateProgress includes logs up to the last millisecond of the month`() {
        val zoneId = ZoneId.systemDefault()
        val yearMonth = monthForChallengeTitle("Closing Night Sprint")
        val challenge = MonthlyChallengeEngine.challengeForMonth(yearMonth, zoneId)
        val endExclusive = yearMonth.plusMonths(1).atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        val logs = listOf(
            classicLog(movieId = 1, watchDate = endExclusive - 1, releaseYear = "2001-01-01"),
            classicLog(movieId = 2, watchDate = endExclusive, releaseYear = "2002-01-01")
        )

        val progress = MonthlyChallengeEngine.evaluateProgress(challenge, logs, zoneId)

        assertEquals(1, progress)
    }

    private fun monthForChallengeTitle(title: String): YearMonth {
        return generateSequence(YearMonth.of(2026, 1)) { it.plusMonths(1) }
            .take(24)
            .first { MonthlyChallengeEngine.challengeForMonth(it).title == title }
    }

    private fun classicLog(movieId: Int, watchDate: Long, releaseYear: String): LogWithMovie {
        return LogWithMovie(
            logEntry = LogEntry(
                movieId = movieId,
                watchDate = watchDate,
                rating = 4f,
                review = null,
                moodTag = null,
                isRewatch = false
            ),
            movie = MovieEntity(
                movieId = movieId,
                title = "Movie $movieId",
                posterPath = null,
                releaseYear = releaseYear,
                genres = "Drama",
                runtime = 120,
                director = null,
                overview = null
            )
        )
    }
}
