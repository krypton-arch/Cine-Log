package com.exmple.cinelog.data.repository

import com.exmple.cinelog.data.local.dao.AiDao
import com.exmple.cinelog.data.local.entity.AiInsightEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepository @Inject constructor(
    private val aiDao: AiDao
) {
    fun getDailyInsight(): Flow<AiInsightEntity?> {
        return aiDao.getDailyInsight()
    }

    suspend fun insertInsight(insight: AiInsightEntity) {
        aiDao.insertOrUpdateInsight(insight)
    }

    suspend fun clearInsights() {
        aiDao.clearInsights()
    }
}
