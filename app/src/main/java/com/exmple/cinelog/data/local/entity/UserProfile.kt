package com.exmple.cinelog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1,
    val xp: Int,
    val level: Int,
    val currentStreak: Int,
    val lastLogDate: String?,
    val totalFilmsWatched: Int,
    val totalMinutesWatched: Int
)
