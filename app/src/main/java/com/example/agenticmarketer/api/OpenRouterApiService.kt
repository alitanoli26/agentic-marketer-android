package com.example.agenticmarketer.api

import com.example.agenticmarketer.models.OpenRouterRequest
import com.example.agenticmarketer.models.OpenRouterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterApiService {
    @POST("api/v1/chat/completions")
    suspend fun generateContent(
        @Header("Authorization") auth: String,
        @Header("HTTP-Referer") referer: String = "https://github.com/agentic-marketer",
        @Header("X-Title") title: String = "Agentic Marketer Lite",
        @Body request: OpenRouterRequest
    ): Response<OpenRouterResponse>
}
