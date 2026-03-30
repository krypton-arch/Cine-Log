package com.exmple.cinelog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.exmple.cinelog.data.local.entity.AiInsightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateInsight(insight: AiInsightEntity)

    @Query("SELECT * FROM ai_insights WHERE id = 1")
    fun getDailyInsight(): Flow<AiInsightEntity?>
    
    @Query("DELETE FROM ai_insights")
    suspend fun clearInsights()
}
