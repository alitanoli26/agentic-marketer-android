
package com.example.agenticmarketer.repositories

import android.util.Log
import com.example.agenticmarketer.api.RetrofitClient
import com.example.agenticmarketer.models.ChatMessage
import com.example.agenticmarketer.models.OpenRouterRequest
import com.example.agenticmarketer.utils.Config
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AIRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val modelNames = listOf(
    "openrouter/auto"
)

    // Kept for backward compatibility with existing callers that only need the text.
    suspend fun generateAIResponse(prompt: String): String? {
        val (text, _) = generateAIResponseWithError(prompt)
        return text
    }

    // Tries each model in modelNames in order. Moves to the next model on 429 (rate
    // limited) or 404 (model removed) so the app keeps working even if one free
    // model is temporarily busy or gets pulled from OpenRouter's free tier.
    suspend fun generateAIResponseWithError(prompt: String): Pair<String?, String?> = withContext(Dispatchers.IO) {
        var lastError = "Unknown error"

        for (model in modelNames) {
            val request = OpenRouterRequest(
                model = model,
                messages = listOf(ChatMessage(role = "user", content = prompt))
            )

            try {
                Log.d("AIRepository", "Requesting OpenRouter ($model) with prompt: $prompt")
                val authHeader = "Bearer ${Config.OPENROUTER_API_KEY}"
                val response = RetrofitClient.openRouterApi.generateContent(authHeader, request = request)

                if (response.isSuccessful) {
                    val body = response.body()
                    val text = body?.choices?.firstOrNull()?.message?.content

                    if (text == null) {
                        Log.e("AIRepository", "Response successful but content is null. Raw body: $body")
                        lastError = "AI returned an empty response. Try again."
                        continue
                    }
                    Log.d("AIRepository", "Response successful: ${text.take(100)}...")
                    return@withContext text to null
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AIRepository", "[$model] error code: ${response.code()}, body: $errorBody")

                    lastError = when (response.code()) {
                        401 -> "Invalid API key. Check Config.kt."
                        402 -> "OpenRouter account has no credits/quota left."
                        404 -> "Model not found ($model). It may have been removed."
                        429 -> "Free model ($model) is busy right now."
                        else -> "API error ${response.code()}: ${errorBody ?: "unknown"}"
                    }

                    // Only worth trying the next model for these specific errors.
                    if (response.code() == 429 || response.code() == 404) {
                        continue
                    } else {
                        return@withContext null to lastError
                    }
                }
            } catch (e: Exception) {
                Log.e("AIRepository", "Network failure or Exception with $model", e)
                lastError = "Network error: ${e.message ?: "could not reach server"}"
            }
        }

        // All models in the list failed.
        null to lastError
    }

    suspend fun saveToFirebase(topic: String, content: String, type: String): Boolean = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: "anonymous"
        val data = hashMapOf(
            "userId" to userId,
            "topic" to topic,
            "content" to content,
            "type" to type,
            "timestamp" to System.currentTimeMillis()
        )

        try {
            firestore.collection("generated_content").add(data).await()
            true
        } catch (e: Exception) {
            Log.e("AIRepository", "Firebase save failed", e)
            false
        }
    }
}