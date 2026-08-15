package com.example.agenticmarketer.utils

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tracks how many free-tier Gemini image requests the user has made today.
 *
 * This is purely a local, client-side counter so the UI can show "X of Y
 * left today" and disable generation before the user gets a server-side 429
 * from Google. It resets automatically at local midnight — it does NOT mirror
 * Google's actual quota (which resets at midnight Pacific time and can vary
 * by account), so treat the bar as a helpful estimate, not a guarantee.
 */
object ImageQuotaTracker {

    private const val PREFS_NAME = "image_quota_prefs"
    private const val KEY_COUNT = "count"
    private const val KEY_DATE = "date"

    private val dayFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private fun todayKey(): String = dayFormatter.format(Date())

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Number of requests already used today (resets to 0 on a new day). */
    fun getUsedToday(context: Context): Int {
        val p = prefs(context)
        val storedDate = p.getString(KEY_DATE, null)
        return if (storedDate == todayKey()) {
            p.getInt(KEY_COUNT, 0)
        } else {
            0
        }
    }

    /** Remaining requests today, never negative. */
    fun getRemainingToday(context: Context, dailyLimit: Int): Int {
        return (dailyLimit - getUsedToday(context)).coerceAtLeast(0)
    }

    /** Call this right after a successful (or attempted) generation call. */
    fun recordUsage(context: Context) {
        val p = prefs(context)
        val today = todayKey()
        val storedDate = p.getString(KEY_DATE, null)
        val currentCount = if (storedDate == today) p.getInt(KEY_COUNT, 0) else 0

        p.edit()
            .putString(KEY_DATE, today)
            .putInt(KEY_COUNT, currentCount + 1)
            .apply()
    }

    fun hasQuotaLeft(context: Context, dailyLimit: Int): Boolean {
        return getUsedToday(context) < dailyLimit
    }
}
