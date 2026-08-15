package com.example.agenticmarketer.api

import com.example.agenticmarketer.models.GeminiGenerateRequest
import com.example.agenticmarketer.models.GeminiGenerateResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface GeminiApiService {
    // Model is part of the path (not the body) for Gemini's native API, e.g.
    // /v1beta/models/gemini-2.5-flash-image:generateContent
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GeminiGenerateRequest
    ): Response<GeminiGenerateResponse>
}
