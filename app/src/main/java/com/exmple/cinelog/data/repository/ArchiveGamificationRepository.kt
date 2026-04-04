package com.exmple.cinelog.data.repository

import com.exmple.cinelog.data.local.dao.GamificationDao
import com.exmple.cinelog.data.local.dao.UserProfileDao
import com.exmple.cinelog.data.local.entity.Badge
import com.exmple.cinelog.data.local.entity.Challenge
import com.exmple.cinelog.data.local.entity.UserProfile
import kotlinx.coroutines.flow.Flow

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArchiveGamificationRepository @Inject constructor(
    private val gamificationDao: GamificationDao,
    private val userProfileDao: UserProfileDao
) {
    // User Profile
    fun getUserProfile(): Flow<UserProfile?> {
        return userProfileDao.getUserProfile()
    }

    suspend fun updateProfile(profile: UserProfile) {
        userProfileDao.upsertProfile(profile)
    }

    // Badges
    fun getAllBadges(): Flow<List<Badge>> {
        return gamificationDao.getAllBadges()
    }

    fun getUnlockedBadges(): Flow<List<Badge>> {
        return gamificationDao.getUnlockedBadges()
    }

    suspend fun unlockBadge(badge: Badge) {
        val unlockedBadge = badge.copy(isUnlocked = true, unlockedDate = System.currentTimeMillis())
        gamificationDao.updateBadge(unlockedBadge)
    }

    suspend fun initializeBadges(badges: List<Badge>) {
        gamificationDao.insertBadges(badges)
    }

    // Challenges
    fun getAllChallenges(): Flow<List<Challenge>> {
        return gamificationDao.getAllChallenges()
    }

    fun getActiveChallenges(): Flow<List<Challenge>> {
        return gamificationDao.getActiveChallenges()
    }

    suspend fun getChallengeById(challengeId: String): Challenge? {
        return gamificationDao.getChallengeById(challengeId)
    }

    suspend fun upsertChallenge(challenge: Challenge) {
        gamificationDao.insertChallenges(listOf(challenge))
    }

    suspend fun completeChallenge(challenge: Challenge) {
        val completedChallenge = challenge.copy(isCompleted = true, currentCount = challenge.targetCount)
        gamificationDao.updateChallenge(completedChallenge)
    }

    suspend fun updateChallengeProgress(challenge: Challenge, count: Int) {
        gamificationDao.updateChallenge(challenge.copy(currentCount = count))
    }

    suspend fun initializeChallenges(challenges: List<Challenge>) {
        gamificationDao.insertChallenges(challenges)
    }
}
