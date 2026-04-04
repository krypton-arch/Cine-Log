package com.exmple.cinelog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.exmple.cinelog.data.local.entity.Badge
import com.exmple.cinelog.data.local.entity.Challenge
import kotlinx.coroutines.flow.Flow

@Dao
interface GamificationDao {
    @Query("SELECT * FROM badges")
    fun getAllBadges(): Flow<List<Badge>>

    @Query("SELECT * FROM badges WHERE isUnlocked = 1")
    fun getUnlockedBadges(): Flow<List<Badge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadges(badges: List<Badge>)

    @Update
    suspend fun updateBadge(badge: Badge)

    @Query("SELECT * FROM challenges")
    fun getAllChallenges(): Flow<List<Challenge>>

    @Query("SELECT * FROM challenges WHERE isCompleted = 0")
    fun getActiveChallenges(): Flow<List<Challenge>>

    @Query("SELECT * FROM challenges WHERE challengeId = :challengeId LIMIT 1")
    suspend fun getChallengeById(challengeId: String): Challenge?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<Challenge>)

    @Update
    suspend fun updateChallenge(challenge: Challenge)
}
