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
            val relayBaseUrl = BuildConfig.GEMINI_PROXY_BASE_URL.trim().trimEnd('/')
            require(relayBaseUrl.isNotEmpty()) {
                "Gemini relay is not configured."
            }

            val requestBody = GeminiRelayRequest(
                systemPrompt = systemPrompt.trim(),
                userMessage = userMessage.ifBlank {
                    "Reply to the system instruction."
                }
            )

            val request = Request.Builder()
                .url("$relayBaseUrl/$RELAY_PATH")
                .post(gson.toJson(requestBody).toRequestBody(JSON_MEDIA_TYPE))
                .build()

            withContext(Dispatchers.IO) {
                client.newCall(request).execute().use { response ->
                    val rawBody = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        throw IllegalStateException(buildErrorMessage(response.code, rawBody))
                    }

                    val payload = gson.fromJson(rawBody, GeminiRelayResponse::class.java)
                    val text = payload.text?.trim()

                    when {
                        !text.isNullOrBlank() -> text
                        !payload.error.isNullOrBlank() -> throw IllegalStateException(payload.error.trim())
                        else -> throw IllegalStateException("Gemini relay returned an empty response.")
                    }
                }
            }
        }
    }

    private fun buildErrorMessage(code: Int, rawBody: String): String {
        val apiMessage = runCatching {
            gson.fromJson(rawBody, GeminiRelayResponse::class.java).error?.trim()
        }.getOrNull()

        return if (!apiMessage.isNullOrBlank()) {
            "Gemini relay error ($code): $apiMessage"
        } else {
            "Gemini relay error ($code)."
        }
    }

    private data class GeminiRelayRequest(
        val systemPrompt: String,
        val userMessage: String
    )

    private data class GeminiRelayResponse(
        val text: String? = null,
        val error: String? = null
    )

    private companion object {
        const val RELAY_PATH = "v1/gemini/generate"
        val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }
}
