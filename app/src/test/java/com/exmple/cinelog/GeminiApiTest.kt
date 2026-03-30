package com.exmple.cinelog

import com.exmple.cinelog.data.repository.GeminiRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Test

class GeminiApiTest {
    @Test
    fun testApiCall() = runBlocking {
        assumeTrue(System.getenv("RUN_LIVE_GEMINI_TEST") == "true")

        try {
            val repository = GeminiRepositoryImpl()
            val result = repository.sendMessage("You are a helpful assistant.", "Say hello.")
            if (result.isSuccess) {
                println("SUCCESS: " + result.getOrNull())
            } else {
                println("FAILURE: " + result.exceptionOrNull()?.message)
                result.exceptionOrNull()?.printStackTrace()
                throw AssertionError("API Call Failed")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
