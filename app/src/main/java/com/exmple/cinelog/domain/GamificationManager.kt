package com.exmple.cinelog.domain

import com.exmple.cinelog.data.local.entity.LogEntry
import com.exmple.cinelog.data.local.entity.UserProfile
import com.exmple.cinelog.data.repository.GamificationRepository
import com.exmple.cinelog.data.repository.LogRepository
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class GamificationManager(
    private val gamificationRepository: GamificationRepository,
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
        val currentProfile = gamificationRepository.getUserProfile().firstOrNull() ?: UserProfile(
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

        gamificationRepository.updateProfile(updatedProfile)
        checkBadges(updatedProfile)
    }

    suspend fun checkBadges(profile: UserProfile? = null) {
        val unlockedBadges = gamificationRepository.getUnlockedBadges().firstOrNull()?.map { it.badgeId } ?: emptyList()
        val logs = logRepository.getAllLogs().firstOrNull() ?: emptyList()
        val currentProfile = profile ?: gamificationRepository.getUserProfile().firstOrNull() ?: return

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
        val badges = gamificationRepository.getAllBadges().firstOrNull() ?: return
        val badge = badges.find { it.badgeId == badgeId } ?: return
        if (badge.isUnlocked) return // Already unlocked
        
        gamificationRepository.unlockBadge(badge)

        val profile = gamificationRepository.getUserProfile().firstOrNull()
        if (profile != null) {
            val newXp = profile.xp + XP_UNLOCK_BADGE
            gamificationRepository.updateProfile(profile.copy(
                xp = newXp,
                level = calculateLevel(newXp)
            ))
        }
    }

    suspend fun checkChallenges() {
        val activeChallenges = gamificationRepository.getActiveChallenges().firstOrNull() ?: return
        val logs = logRepository.getAllLogs().firstOrNull() ?: emptyList()
        
        activeChallenges.forEach { challenge ->
            val newCount = when (challenge.challengeId) {
                "indie_films" -> {
                    logs.count { 
                        it.movie.genres.contains("Drama", ignoreCase = true) || 
                        it.movie.genres.contains("Noir", ignoreCase = true) ||
                        it.movie.genres.contains("Indie", ignoreCase = true)
                    }
                }
                "weekend_warrior" -> {
                    val now = System.currentTimeMillis()
                    val weekAgo = now - 7 * 24 * 60 * 60 * 1000L
                    logs.count { it.logEntry.watchDate >= weekAgo }
                }
                "genre_explorer" -> {
                    logs.flatMap { it.movie.genres.split(",").map { g -> g.trim() } }
                        .filter { it.isNotEmpty() }
                        .distinct()
                        .size
                }
                "review_streak" -> {
                    logs.count { !it.logEntry.review.isNullOrBlank() }
                }
                else -> challenge.currentCount
            }
            
            gamificationRepository.updateChallengeProgress(challenge, minOf(newCount, challenge.targetCount))
            
            if (newCount >= challenge.targetCount && !challenge.isCompleted) {
                gamificationRepository.completeChallenge(challenge)
            }
        }
    }
}
