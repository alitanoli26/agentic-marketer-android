package com.alihassan.agenticmarketer.content

/**
 * Representative architecture sample for the Agentic Marketer case study.
 * The original thesis describes this boundary; adapt interfaces to the final app module.
 */
class ContentRepository(
    private val geminiApi: GeminiApi,
    private val historyStore: HistoryStore,
) {
    suspend fun generateBlog(prompt: String): Result<String> = runCatching {
        require(prompt.isNotBlank()) { "Prompt must not be blank" }
        val response = geminiApi.generateText(GeminiRequest(prompt))
        val text = response.text.trim()
        require(text.isNotEmpty()) { "The model returned an empty response" }
        historyStore.save(ContentRecord(kind = "blog", body = text))
        text
    }
}

data class GeminiRequest(val prompt: String)
data class GeminiResponse(val text: String)
data class ContentRecord(val kind: String, val body: String)

interface GeminiApi {
    suspend fun generateText(request: GeminiRequest): GeminiResponse
}

interface HistoryStore {
    suspend fun save(record: ContentRecord)
}
