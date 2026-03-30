package com.exmple.cinelog.data.repository

import com.exmple.cinelog.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import javax.inject.Inject
import javax.inject.Singleton

interface GeminiRepository {
    suspend fun sendMessage(systemPrompt: String, userMessage: String): Result<String>
}

@Singleton
class GeminiRepositoryImpl @Inject constructor() : GeminiRepository {

    // Using BuildConfig.GEMINI_API_KEY from secrets-gradle-plugin
    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash", // Fallback to 1.5 if 2.0 isn't fully rollout in SDK yet
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    override suspend fun sendMessage(systemPrompt: String, userMessage: String): Result<String> {
        return try {
            val response = model.generateContent(
                content { 
                    text(systemPrompt)
                    text("\n\nUser Question: $userMessage")
                }
            )
            Result.success(response.text ?: "...")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
