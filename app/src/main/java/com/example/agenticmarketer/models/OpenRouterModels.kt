package com.example.agenticmarketer.models

data class OpenRouterRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val stream: Boolean = false,
    // Some image-capable models (e.g. google/gemini-3.1-flash-image) require this
    // to know image output is expected, even though it's also inferred server-side.
    val modalities: List<String>? = null
)

data class ChatMessage(
    val role: String,
    val content: String
)

// Separate response-side message model because the response can carry an
// "images" array alongside (or instead of) text content — ChatMessage above
// stays a plain string for outgoing requests so existing text-only callers
// (AIRepository) don't need to change.
data class ResponseMessage(
    val role: String? = null,
    val content: String? = null,
    val images: List<ResponseImage>? = null
)

data class ResponseImage(
    val type: String? = null,
    val image_url: ImageUrl? = null
)

data class ImageUrl(
    val url: String? = null // data:image/png;base64,<...> for generated images
)

data class OpenRouterResponse(
    val choices: List<Choice>? = null,
    val error: OpenRouterError? = null
)

data class Choice(
    val message: ResponseMessage? = null,
    val finish_reason: String? = null
)

data class OpenRouterError(
    val code: Int,
    val message: String,
    val metadata: Map<String, Any>? = null
)

data class AIContent(
    val id: String = "",
    val topic: String = "",
    val content: String = "",
    val type: String = "", // "blog", "caption", "humanized"
    val timestamp: Long = System.currentTimeMillis()
)
