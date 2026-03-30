package com.exmple.cinelog.data.repository

import com.exmple.cinelog.data.local.dao.AiDao
import com.exmple.cinelog.data.local.entity.AiInsightEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepository @Inject constructor(
    private val aiDao: AiDao
) {
    suspend fun getDailyInsight(): AiInsightEntity? {
        return aiDao.getDailyInsight()
    }

    suspend fun insertInsight(insight: AiInsightEntity) {
        aiDao.insertInsight(insight)
    }

    suspend fun clearOldInsights() {
        aiDao.clearOldInsights()
    }
}
