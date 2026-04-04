package com.exmple.cinelog.domain

import com.exmple.cinelog.data.local.dao.LogWithMovie
import com.exmple.cinelog.data.local.entity.Challenge
import java.time.DayOfWeek
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

enum class MonthlyChallengeStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED
}

data class MonthlyChallengeSnapshot(
    val monthLabel: String,
    val title: String,
    val description: String,
    val currentCount: Int,
    val targetCount: Int,
    val rewardXp: Int,
    val progress: Float,
    val status: MonthlyChallengeStatus,
    val statusLabel: String,
    val supportingText: String
)

private enum class MonthlyChallengeRule {
    TOTAL_LOGS,
    REVIEWED_LOGS,
    DISTINCT_GENRES,
    TOTAL_MINUTES,
    CLASSIC_FILMS,
    WEEKEND_LOGS
}

private data class MonthlyChallengeTemplate(
    val id: String,
    val title: String,
    val description: String,
    val targetCount: Int,
    val rewardXp: Int,
    val rule: MonthlyChallengeRule
)

object MonthlyChallengeEngine {
    private const val CHALLENGE_PREFIX = "monthly"

    private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

    private val templates = listOf(
        MonthlyChallengeTemplate(
            id = "closing_night",
            title = "Closing Night Sprint",
            description = "Log 8 films before the month wraps and keep the reel moving.",
            targetCount = 8,
            rewardXp = 120,
            rule = MonthlyChallengeRule.TOTAL_LOGS
        ),
        MonthlyChallengeTemplate(
            id = "critic_notes",
            title = "Critic's Notes",
            description = "Write reviews for 4 films you log this month.",
            targetCount = 4,
            rewardXp = 110,
            rule = MonthlyChallengeRule.REVIEWED_LOGS
        ),
        MonthlyChallengeTemplate(
            id = "genre_passport",
            title = "Genre Passport",
            description = "Log films from 5 distinct genres this month.",
            targetCount = 5,
            rewardXp = 120,
            rule = MonthlyChallengeRule.DISTINCT_GENRES
        ),
        MonthlyChallengeTemplate(
            id = "long_take",
            title = "Long Take Marathon",
            description = "Reach 900 logged minutes this month.",
            targetCount = 900,
            rewardXp = 130,
            rule = MonthlyChallengeRule.TOTAL_MINUTES
        ),
        MonthlyChallengeTemplate(
            id = "time_capsule",
            title = "Time Capsule",
            description = "Log 3 films released before 1980 this month.",
            targetCount = 3,
            rewardXp = 130,
            rule = MonthlyChallengeRule.CLASSIC_FILMS
        ),
        MonthlyChallengeTemplate(
            id = "weekend_matinee",
            title = "Weekend Matinee",
            description = "Log 3 Saturday or Sunday viewings this month.",
            targetCount = 3,
            rewardXp = 100,
            rule = MonthlyChallengeRule.WEEKEND_LOGS
        )
    )

    fun challengeIdForMonth(yearMonth: YearMonth): String {
        val template = templateForMonth(yearMonth)
        return "$CHALLENGE_PREFIX:${yearMonth}:${template.id}"
    }

    fun challengeForMonth(
        yearMonth: YearMonth,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Challenge {
        val template = templateForMonth(yearMonth)
        return Challenge(
            challengeId = challengeIdForMonth(yearMonth),
            title = template.title,
            description = template.description,
            targetCount = template.targetCount,
            currentCount = 0,
            deadline = endOfMonthMillis(yearMonth, zoneId),
            isCompleted = false
        )
    }

    fun rewardXpForChallenge(challenge: Challenge): Int {
        return templateForChallenge(challenge)?.rewardXp ?: 0
    }

    private fun templateForMonth(yearMonth: YearMonth): MonthlyChallengeTemplate {
        val index = Math.floorMod(yearMonth.year * 12 + yearMonth.monthValue, templates.size)
        return templates[index]
    }

    private fun templateForChallenge(challenge: Challenge): MonthlyChallengeTemplate? {
        val parsed = parseChallengeId(challenge.challengeId) ?: return null
        return templates.firstOrNull { it.id == parsed.templateId }
    }

    fun evaluateProgress(
        challenge: Challenge,
        logs: List<LogWithMovie>,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Int {
        val parsed = parseChallengeId(challenge.challengeId) ?: return challenge.currentCount
        val template = templates.firstOrNull { it.id == parsed.templateId } ?: return challenge.currentCount
        return evaluateProgress(template, logs, parsed.yearMonth, zoneId)
    }

    fun buildSnapshot(
        challenge: Challenge,
        logs: List<LogWithMovie>,
        zoneId: ZoneId = ZoneId.systemDefault(),
        nowMillis: Long = System.currentTimeMillis()
    ): MonthlyChallengeSnapshot? {
        val parsed = parseChallengeId(challenge.challengeId) ?: return null
        val template = templates.firstOrNull { it.id == parsed.templateId } ?: return null
        val rawCount = evaluateProgress(template, logs, parsed.yearMonth, zoneId)
        val displayCount = if (challenge.isCompleted) {
            challenge.targetCount
        } else {
            rawCount.coerceIn(0, challenge.targetCount)
        }
        val status = when {
            challenge.isCompleted -> MonthlyChallengeStatus.COMPLETED
            displayCount == 0 -> MonthlyChallengeStatus.NOT_STARTED
            else -> MonthlyChallengeStatus.IN_PROGRESS
        }
        val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
        val daysRemaining = ChronoUnit.DAYS.between(today, parsed.yearMonth.atEndOfMonth()).toInt().coerceAtLeast(0)
        val supportingText = when (status) {
            MonthlyChallengeStatus.COMPLETED -> "${template.rewardXp} XP archived for this month."
            MonthlyChallengeStatus.NOT_STARTED -> if (daysRemaining == 0) "Ends today." else "$daysRemaining days left this month."
            MonthlyChallengeStatus.IN_PROGRESS -> if (daysRemaining == 0) "Ends today." else "$daysRemaining days left this month."
        }

        return MonthlyChallengeSnapshot(
            monthLabel = parsed.yearMonth.format(monthFormatter),
            title = template.title,
            description = template.description,
            currentCount = displayCount,
            targetCount = challenge.targetCount,
            rewardXp = template.rewardXp,
            progress = if (challenge.targetCount == 0) 0f else displayCount.toFloat() / challenge.targetCount.toFloat(),
            status = status,
            statusLabel = when (status) {
                MonthlyChallengeStatus.NOT_STARTED -> "READY TO START"
                MonthlyChallengeStatus.IN_PROGRESS -> "IN PROGRESS"
                MonthlyChallengeStatus.COMPLETED -> "COMPLETED"
            },
            supportingText = supportingText
        )
    }

    private fun evaluateProgress(
        template: MonthlyChallengeTemplate,
        logs: List<LogWithMovie>,
        yearMonth: YearMonth,
        zoneId: ZoneId
    ): Int {
        val monthLogs = logsForMonth(logs, yearMonth, zoneId)
        return when (template.rule) {
            MonthlyChallengeRule.TOTAL_LOGS -> monthLogs.size
            MonthlyChallengeRule.REVIEWED_LOGS -> monthLogs.count { !it.logEntry.review.isNullOrBlank() }
            MonthlyChallengeRule.DISTINCT_GENRES -> monthLogs
                .flatMap { log ->
                    log.movie.genres
                        .split(",")
                        .map { it.trim().lowercase(Locale.getDefault()) }
                        .filter { it.isNotEmpty() }
                }
                .distinct()
                .size
            MonthlyChallengeRule.TOTAL_MINUTES -> monthLogs.sumOf { it.movie.runtime ?: 0 }
            MonthlyChallengeRule.CLASSIC_FILMS -> monthLogs.count {
                val year = it.movie.releaseYear?.take(4)?.toIntOrNull()
                year != null && year < 1980
            }
            MonthlyChallengeRule.WEEKEND_LOGS -> monthLogs.count {
                val watchedOn = Instant.ofEpochMilli(it.logEntry.watchDate)
                    .atZone(zoneId)
                    .toLocalDate()
                watchedOn.dayOfWeek == DayOfWeek.SATURDAY || watchedOn.dayOfWeek == DayOfWeek.SUNDAY
            }
        }
    }

    private fun logsForMonth(
        logs: List<LogWithMovie>,
        yearMonth: YearMonth,
        zoneId: ZoneId
    ): List<LogWithMovie> {
        val start = yearMonth.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endExclusive = yearMonth.plusMonths(1).atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return logs.filter { it.logEntry.watchDate in start until endExclusive }
    }

    private fun endOfMonthMillis(
        yearMonth: YearMonth,
        zoneId: ZoneId
    ): Long {
        return yearMonth
            .plusMonths(1)
            .atDay(1)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli() - 1L
    }

    private fun parseChallengeId(challengeId: String): ParsedChallengeId? {
        val parts = challengeId.split(":")
        if (parts.size != 3 || parts.first() != CHALLENGE_PREFIX) return null
        val yearMonth = runCatching { YearMonth.parse(parts[1]) }.getOrNull() ?: return null
        return ParsedChallengeId(yearMonth = yearMonth, templateId = parts[2])
    }

    private data class ParsedChallengeId(
        val yearMonth: YearMonth,
        val templateId: String
    )
}
