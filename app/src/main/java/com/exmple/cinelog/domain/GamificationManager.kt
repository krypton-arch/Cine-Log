package com.exmple.cinelog.domain

import com.exmple.cinelog.data.local.dao.LogWithMovie
import com.exmple.cinelog.data.local.entity.Challenge
import com.exmple.cinelog.data.local.entity.LogEntry
import com.exmple.cinelog.data.local.entity.UserProfile
import com.exmple.cinelog.data.repository.ArchiveGamificationRepository
import com.exmple.cinelog.data.repository.LogRepository
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

import javax.inject.Inject

class GamificationManager @Inject constructor(
    private val archiveGamificationRepository: ArchiveGamificationRepository,
    private val logRepository: LogRepository
) {

    // XP constants
    private val XP_LOG_MOVIE = 10
    private val XP_WRITE_REVIEW = 20
    private val XP_COMPLETE_WATCHLIST = 15
    private val XP_MAINTAIN_STREAK = 5
    private val XP_UNLOCK_BADGE = 50

    fun calculateLevel(xp: Int): Int {
        return when {
            xp >= 4000 -> 5 // Legend
            xp >= 2000 -> 4 // Director
            xp >= 1000 -> 3 // Auteur
            xp >= 500 -> 2  // Critic
            else -> 1       // Cinephile
        }
    }

    fun getLevelName(level: Int): String {
        return when (level) {
            5 -> "Legend"
            4 -> "Director"
            3 -> "Auteur"
            2 -> "Critic"
            else -> "Cinephile"
        }
    }

    fun getXpForNextLevel(currentLevel: Int): Int {
        return when (currentLevel) {
            1 -> 500
            2 -> 1000
            3 -> 2000
            4 -> 4000
            else -> 10000
        }
    }

    suspend fun processMovieLog(logEntry: LogEntry, hasReview: Boolean, wasOnWatchlist: Boolean) {
        val currentProfile = archiveGamificationRepository.getUserProfile().firstOrNull() ?: UserProfile(
            xp = 0, level = 1, currentStreak = 0, lastLogDate = null,
            totalFilmsWatched = 0, totalMinutesWatched = 0
        )

        var newXp = currentProfile.xp + XP_LOG_MOVIE
        if (hasReview) newXp += XP_WRITE_REVIEW
        if (wasOnWatchlist) newXp += XP_COMPLETE_WATCHLIST

        val today = LocalDate.now()
        val lastLogDate = try {
            currentProfile.lastLogDate?.let { LocalDate.parse(it) }
        } catch (e: Exception) { null }
        
        var newStreak = currentProfile.currentStreak

        if (lastLogDate != null) {
            val yesterday = today.minusDays(1)
            if (lastLogDate == yesterday) {
                newStreak += 1
                newXp += XP_MAINTAIN_STREAK
            } else if (lastLogDate != today) {
                newStreak = 1 // Reset
            }
            // If lastLogDate == today, keep the same streak (already logged today)
        } else {
            newStreak = 1
        }

        // Calculate total films and minutes from actual DB
        val totalFilms = (logRepository.getTotalFilmsWatched().firstOrNull() ?: 0)
        val totalMinutes = logRepository.getTotalMinutesWatched().firstOrNull() ?: 0
        val newLevel = calculateLevel(newXp)

        val updatedProfile = currentProfile.copy(
            xp = newXp,
            level = newLevel,
            currentStreak = newStreak,
            lastLogDate = today.toString(),
            totalFilmsWatched = totalFilms,
            totalMinutesWatched = totalMinutes
        )

        archiveGamificationRepository.updateProfile(updatedProfile)
        checkBadges(updatedProfile)
    }

    fun buildCurrentMonthlyChallengeSnapshot(
        challenges: List<Challenge>,
        logs: List<LogWithMovie>,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): MonthlyChallengeSnapshot? {
        val currentMonth = YearMonth.now(zoneId)
        val currentChallenge = challenges.firstOrNull {
            it.challengeId == MonthlyChallengeEngine.challengeIdForMonth(currentMonth)
        } ?: MonthlyChallengeEngine.challengeForMonth(currentMonth, zoneId)

        return MonthlyChallengeEngine.buildSnapshot(
            challenge = currentChallenge,
            logs = logs,
            zoneId = zoneId
        )
    }

    suspend fun checkBadges(profile: UserProfile? = null) {
        val unlockedBadges = archiveGamificationRepository.getUnlockedBadges().firstOrNull()?.map { it.badgeId } ?: emptyList()
        val logs = logRepository.getAllLogs().firstOrNull() ?: emptyList()
        val currentProfile = profile ?: archiveGamificationRepository.getUserProfile().firstOrNull() ?: return

        // First Frame (1 film)
        if ("first_log" !in unlockedBadges && logs.isNotEmpty()) {
            unlockBadge("first_log")
        }

        // Centurion (100 films)
        if ("centurion" !in unlockedBadges && logs.size >= 100) {
            unlockBadge("centurion")
        }

        // Horror Fiend (10 horror)
        if ("horror_fiend" !in unlockedBadges) {
            val horrorCount = logs.count { it.movie.genres.contains("Horror", ignoreCase = true) }
            if (horrorCount >= 10) unlockBadge("horror_fiend")
        }

        // Old Soul (10 pre-1980)
        if ("old_soul" !in unlockedBadges) {
            val oldSoulCount = logs.count {
                val year = it.movie.releaseYear?.toIntOrNull() ?: 9999
                year < 1980
            }
            if (oldSoulCount >= 10) unlockBadge("old_soul")
        }

        // Binge King (5 films in 7 days)
        if ("binge_king" !in unlockedBadges) {
            val now = System.currentTimeMillis()
            val weekAgo = now - 7 * 24 * 60 * 60 * 1000L
            val recentLogs = logs.count { it.logEntry.watchDate >= weekAgo }
            if (recentLogs >= 5) unlockBadge("binge_king")
        }

        // Marathon Runner (1000 minutes)
        if ("marathon" !in unlockedBadges) {
            val totalMinutes = currentProfile.totalMinutesWatched
            if (totalMinutes >= 1000) unlockBadge("marathon")
        }

        // The Critic (10 reviews)
        if ("critic" !in unlockedBadges) {
            val reviewCount = logs.count { !it.logEntry.review.isNullOrBlank() }
            if (reviewCount >= 10) unlockBadge("critic")
        }

        // Week Warrior (7-day streak)
        if ("streak_7" !in unlockedBadges) {
            if (currentProfile.currentStreak >= 7) unlockBadge("streak_7")
        }
    }

    private suspend fun unlockBadge(badgeId: String) {
        val badges = archiveGamificationRepository.getAllBadges().firstOrNull() ?: return
        val badge = badges.find { it.badgeId == badgeId } ?: return
        if (badge.isUnlocked) return // Already unlocked
        
        archiveGamificationRepository.unlockBadge(badge)

        val profile = archiveGamificationRepository.getUserProfile().firstOrNull()
        if (profile != null) {
            val newXp = profile.xp + XP_UNLOCK_BADGE
            archiveGamificationRepository.updateProfile(profile.copy(
                xp = newXp,
                level = calculateLevel(newXp)
            ))
        }
    }

    suspend fun syncMonthlyChallenge(zoneId: ZoneId = ZoneId.systemDefault()) {
        val currentMonth = YearMonth.now(zoneId)
        val challengeId = MonthlyChallengeEngine.challengeIdForMonth(currentMonth)
        val currentChallenge = archiveGamificationRepository.getChallengeById(challengeId)
            ?: MonthlyChallengeEngine.challengeForMonth(currentMonth, zoneId).also {
                archiveGamificationRepository.upsertChallenge(it)
            }

        val logs = logRepository.getAllLogs().firstOrNull() ?: emptyList()
        val rawProgress = MonthlyChallengeEngine.evaluateProgress(currentChallenge, logs, zoneId)
        val cappedProgress = rawProgress.coerceIn(0, currentChallenge.targetCount)

        if (!currentChallenge.isCompleted && cappedProgress != currentChallenge.currentCount) {
            archiveGamificationRepository.updateChallengeProgress(currentChallenge, cappedProgress)
        }

        if (rawProgress >= currentChallenge.targetCount && !currentChallenge.isCompleted) {
            archiveGamificationRepository.completeChallenge(currentChallenge.copy(currentCount = cappedProgress))
            awardMonthlyChallengeXp(currentChallenge)
        }
    }

    suspend fun checkChallenges() {
        syncMonthlyChallenge()
    }

    private suspend fun awardMonthlyChallengeXp(challenge: Challenge) {
        val rewardXp = MonthlyChallengeEngine.rewardXpForChallenge(challenge)
        if (rewardXp <= 0) return

        val profile = archiveGamificationRepository.getUserProfile().firstOrNull() ?: return
        val newXp = profile.xp + rewardXp
        archiveGamificationRepository.updateProfile(
            profile.copy(
                xp = newXp,
                level = calculateLevel(newXp)
            )
        )
    }
}
