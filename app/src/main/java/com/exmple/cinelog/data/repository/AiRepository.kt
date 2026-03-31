package com.exmple.cinelog.data.repository

import com.exmple.cinelog.data.local.dao.AiDao
import com.exmple.cinelog.data.local.entity.AiInsightEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

data class AiConversationMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long
)

@Singleton
class AiRepository @Inject constructor(
    private val aiDao: AiDao
) {
    private val gson = Gson()
    private val aiStateMutex = Mutex()

    fun getDailyInsight(): Flow<AiInsightEntity?> {
        return aiDao.observeState().map { state ->
            state?.takeIf { !it.insightText.isNullOrBlank() && it.generatedAt != null }
        }
    }

    fun getBoothConversation(): Flow<List<AiConversationMessage>> {
        return aiDao.observeState().map { state ->
            val rawConversation = state?.conversationJson
            if (rawConversation.isNullOrBlank()) {
                emptyList()
            } else {
                deserializeConversation(rawConversation)
            }
        }
    }

    suspend fun saveInsight(insightText: String, generatedAt: Long) {
        updateState { current ->
            current.copy(
                insightText = insightText,
                generatedAt = generatedAt
            )
        }
    }

    suspend fun saveBoothConversation(messages: List<AiConversationMessage>) {
        val latestTimestamp = messages.maxOfOrNull(AiConversationMessage::timestamp)
        updateState { current ->
            current.copy(
                conversationJson = messages.takeIf { it.isNotEmpty() }?.let(::serializeConversation),
                conversationUpdatedAt = latestTimestamp
            )
        }
    }

    private suspend fun updateState(transform: (AiInsightEntity) -> AiInsightEntity) {
        aiStateMutex.withLock {
            val current = aiDao.getStateSnapshot() ?: AiInsightEntity()
            aiDao.upsertState(transform(current))
        }
    }

    private fun serializeConversation(messages: List<AiConversationMessage>): String {
        return gson.toJson(messages)
    }

    private fun deserializeConversation(rawConversation: String): List<AiConversationMessage> {
        return try {
            val listType = object : TypeToken<List<AiConversationMessage>>() {}.type
            gson.fromJson<List<AiConversationMessage>>(rawConversation, listType).orEmpty()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
