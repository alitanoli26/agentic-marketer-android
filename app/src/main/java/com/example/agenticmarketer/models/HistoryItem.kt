package com.example.agenticmarketer.models

import com.example.agenticmarketer.utils.TimeUtils  // ✅ ADD THIS IMPORT

data class HistoryItem(
    val id: String = "",
    val userId: String = "",
    val topic: String = "",
    val content: String = "",
    val type: String = "",
    val timestamp: Long = 0,
    val imageUrl: String = ""
) {
    val typeIcon: String
        get() = when (type) {
            "blog" -> "📝"
            "caption" -> "📱"
            "hashtags" -> "#️⃣"
            "image" -> "🖼️"
            else -> "📄"
        }

    val typeLabel: String
        get() = when (type) {
            "blog" -> "Blog"
            "caption" -> "Caption"
            "hashtags" -> "Hashtags"
            "image" -> "Image"
            else -> "Content"
        }

    val formattedDate: String
        get() = TimeUtils.formatTimestamp(timestamp)

    val previewContent: String
        get() = if (content.length > 120) content.take(120) + "..." else content
}