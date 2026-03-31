package com.exmple.cinelog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_insights")
data class AiInsightEntity(
    @PrimaryKey val id: Int = 1, // singleton row for AI cache state
    val insightText: String? = null,
    val generatedAt: Long? = null,
    val conversationJson: String? = null,
    val conversationUpdatedAt: Long? = null
)
