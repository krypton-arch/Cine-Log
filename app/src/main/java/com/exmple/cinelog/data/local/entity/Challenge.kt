package com.exmple.cinelog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "challenges")
data class Challenge(
    @PrimaryKey
    val challengeId: String,
    val title: String,
    val description: String,
    val targetCount: Int,
    val currentCount: Int,
    val deadline: Long?,
    val isCompleted: Boolean
)
