package com.exmple.cinelog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "badges")
data class Badge(
    @PrimaryKey
    val badgeId: String,
    val name: String,
    val description: String,
    val iconRes: String,
    val isUnlocked: Boolean,
    val unlockedDate: Long?
)
