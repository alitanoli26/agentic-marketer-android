package com.example.agenticmarketer.utils

import android.content.Context

/**
 * Lightweight SharedPreferences cache for the logged-in user's profile info.
 *
 * Why this exists:
 * - Firebase Auth's displayName / Firestore reads need a network round-trip the
 *   first time, which causes a flash of "Marketer" / email-derived name on Home
 *   and Profile screens.
 * - Caching the real name locally (set once at signup, refreshed on login)
 *   means the UI can show it instantly, with no flicker, even offline.
 */
object UserPrefs {

    private const val PREFS_NAME = "user_prefs"
    private const val KEY_NAME = "cached_user_name"
    private const val KEY_EMAIL = "cached_user_email"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Save the user's real name (entered at signup) to the local cache. */
    fun saveName(context: Context, name: String) {
        prefs(context).edit().putString(KEY_NAME, name).apply()
    }

    /** Returns the cached real name, or null if nothing is cached yet. */
    fun getName(context: Context): String? {
        val name = prefs(context).getString(KEY_NAME, null)
        return if (name.isNullOrBlank()) null else name
    }

    fun saveEmail(context: Context, email: String) {
        prefs(context).edit().putString(KEY_EMAIL, email).apply()
    }

    fun getEmail(context: Context): String? = prefs(context).getString(KEY_EMAIL, null)

    /** Clear cached profile info — call this on logout so the next user doesn't see stale data. */
    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
