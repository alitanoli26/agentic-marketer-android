package com.example.agenticmarketer.models

// Request/response models for Google's native Gemini API (generativelanguage.googleapis.com),
// used for image generation. This is a different wire format from OpenRouter's
// OpenAI-style chat/completions (see OpenRouterModels.kt) — Gemini uses
// "contents" -> "parts", and returns generated images as inline base64 data
// rather than an OpenAI-style "images" array.

data class GeminiGenerateRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null
)

data class GeminiContent(
    val role: String = "user",
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiInlineData? = null
)

data class GeminiInlineData(
    val mimeType: String,
    val data: String // base64
)

data class GeminiGenerationConfig(
    // Must include "IMAGE" (and usually "TEXT") to get image output back.
    val responseModalities: List<String>
)

data class GeminiGenerateResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiError? = null
)

data class GeminiCandidate(
    val content: GeminiContent? = null,
    val finishReason: String? = null
)

data class GeminiError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)
