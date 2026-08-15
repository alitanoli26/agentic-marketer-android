package com.example.agenticmarketer.utils

/**
 * Public-safe configuration template.
 * Keep real values in a protected local build configuration or backend.
 */
object Config {
    const val OPENROUTER_API_KEY = "REPLACE_WITH_LOCAL_OPENROUTER_KEY"
    const val GEMINI_API_KEY = "REPLACE_WITH_LOCAL_GEMINI_KEY"
    const val HUGGINGFACE_API_KEY = "REPLACE_WITH_LOCAL_HUGGINGFACE_KEY"

    // Client-side warning threshold; provider quotas can change independently.
    const val IMAGE_DAILY_LIMIT = 20
}
