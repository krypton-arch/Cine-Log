package com.exmple.cinelog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_insights")
data class AiInsightEntity(
    @PrimaryKey val id: Int = 1, // singleton row
    val insightText: String,
    val generatedAt: Long // System.currentTimeMillis()
)
