package com.example.agenticmarketer.models

// Request model for Hugging Face's Inference Providers text-to-image endpoint.
// Unlike OpenRouter/Gemini, the response is NOT JSON — it's the raw image
// bytes (PNG/JPEG) directly in the HTTP body, so there's no matching
// "response" data class here. See HuggingFaceApiService.kt, which returns
// ResponseBody instead of a parsed type.

data class HuggingFaceImageRequest(
    val inputs: String,
    val parameters: HuggingFaceImageParameters? = null
)

data class HuggingFaceImageParameters(
    val width: Int? = null,
    val height: Int? = null,
    val num_inference_steps: Int? = null
)
