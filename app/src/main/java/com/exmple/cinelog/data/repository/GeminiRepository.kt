package com.exmple.cinelog.data.repository

import com.exmple.cinelog.BuildConfig
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

interface GeminiRepository {
    suspend fun sendMessage(systemPrompt: String, userMessage: String): Result<String>
}

@Singleton
class GeminiRepositoryImpl @Inject constructor() : GeminiRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    override suspend fun sendMessage(systemPrompt: String, userMessage: String): Result<String> {
        return runCatching {
            val apiKey = BuildConfig.GEMINI_API_KEY.trim()
            require(apiKey.isNotEmpty()) { "Gemini API key is missing." }

            val requestBody = GeminiGenerateContentRequest(
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = systemPrompt.trim()))
                ),
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(
                            GeminiPart(
                                text = userMessage.ifBlank {
                                    "Reply to the system instruction."
                                }
                            )
                        )
                    )
                )
            )

            val request = Request.Builder()
                .url("$BASE_URL/$MODEL:generateContent?key=$apiKey")
                .post(gson.toJson(requestBody).toRequestBody(JSON_MEDIA_TYPE))
                .build()

            withContext(Dispatchers.IO) {
                client.newCall(request).execute().use { response ->
                    val rawBody = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        throw IllegalStateException(buildErrorMessage(response.code, rawBody))
                    }

                    val payload = gson.fromJson(rawBody, GeminiGenerateContentResponse::class.java)
                    val text = payload.candidates
                        .orEmpty()
                        .asSequence()
                        .flatMap { it.content?.parts.orEmpty().asSequence() }
                        .mapNotNull { it.text?.trim() }
                        .firstOrNull { it.isNotEmpty() }

                    when {
                        !text.isNullOrBlank() -> text
                        !payload.promptFeedback?.blockReason.isNullOrBlank() -> {
                            throw IllegalStateException(
                                "Gemini blocked the prompt: ${payload.promptFeedback?.blockReason}."
                            )
                        }
                        else -> throw IllegalStateException("Gemini returned an empty response.")
                    }
                }
            }
        }
    }

    private fun buildErrorMessage(code: Int, rawBody: String): String {
        val apiMessage = runCatching {
            gson.fromJson(rawBody, GeminiErrorEnvelope::class.java).error?.message?.trim()
        }.getOrNull()

        return if (!apiMessage.isNullOrBlank()) {
            "Gemini API error ($code): $apiMessage"
        } else {
            "Gemini API error ($code)."
        }
    }

    private data class GeminiGenerateContentRequest(
        val systemInstruction: GeminiContent,
        val contents: List<GeminiContent>,
        val generationConfig: GeminiGenerationConfig = GeminiGenerationConfig()
    )

    private data class GeminiGenerationConfig(
        val temperature: Float = 0.7f,
        val topP: Float = 0.9f,
        val maxOutputTokens: Int = 3072
    )

    private data class GeminiContent(
        val parts: List<GeminiPart>,
        val role: String? = null
    )

    private data class GeminiPart(
        val text: String? = null
    )

    private data class GeminiGenerateContentResponse(
        val candidates: List<GeminiCandidate>? = null,
        val promptFeedback: GeminiPromptFeedback? = null
    )

    private data class GeminiCandidate(
        val content: GeminiContent? = null
    )

    private data class GeminiPromptFeedback(
        val blockReason: String? = null
    )

    private data class GeminiErrorEnvelope(
        val error: GeminiErrorBody? = null
    )

    private data class GeminiErrorBody(
        val message: String? = null
    )

    private companion object {
        const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        const val MODEL = "gemini-2.5-flash"
        val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }
}
