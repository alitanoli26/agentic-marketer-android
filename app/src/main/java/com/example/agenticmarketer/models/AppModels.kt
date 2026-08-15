package com.example.agenticmarketer.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Blog(
    val id: String = "",
    val userId: String = "",
    val topic: String = "",
    val tone: String = "",
    val content: String = "",
    val humanizedContent: String = "",
    @ServerTimestamp val createdAt: Date? = null
)

data class SocialPost(
    val id: String = "",
    val userId: String = "",
    val topic: String = "",
    val platform: String = "",
    val caption: String = "",
    val imageUrl: String = "",
    val scheduledTime: Long? = null,
    val status: String = "draft", // draft, scheduled, posted
    @ServerTimestamp val createdAt: Date? = null
)

data class ScheduledPost(
    val id: String = "",
    val userId: String = "",
    val postId: String = "",
    val platform: String = "",
    val scheduledTime: Long = 0,
    val status: String = "pending"
)