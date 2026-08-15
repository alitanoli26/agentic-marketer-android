package com.alihassan.agenticmarketer.content

/** Representative persistence boundary; production code should use Firebase SDK models. */
class FirestoreManager(private val userId: String) : HistoryStore {
    override suspend fun save(record: ContentRecord) {
        require(userId.isNotBlank()) { "Authenticated user is required" }
        // In the Android app, write to users/{userId}/content with Firestore security rules.
        println("Persisting ${record.kind} content for $userId")
    }
}
