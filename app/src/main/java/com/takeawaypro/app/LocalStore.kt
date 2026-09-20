package com.takeawaypro.app

import android.content.Context
import kotlinx.serialization.json.Json

private const val PREFS_NAME = "takeaway_pro_store"
private const val KEY_SESSION = "session_json"

/**
 * Saves and restores only the current session (JWT + account) on this device,
 * so a restaurant/café or the admin doesn't have to log in again every time
 * the app is closed. The catalog, orders, restaurants and notifications now
 * live on the real backend and are fetched fresh over the network each time.
 */
class LocalStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun load(): SessionSnapshot? {
        val raw = prefs.getString(KEY_SESSION, null) ?: return null
        return try {
            json.decodeFromString(SessionSnapshot.serializer(), raw)
        } catch (e: Exception) {
            // Corrupt or outdated session (e.g. after a model change) — start fresh
            // rather than crash the app.
            null
        }
    }

    fun save(session: SessionSnapshot) {
        val raw = json.encodeToString(SessionSnapshot.serializer(), session)
        prefs.edit().putString(KEY_SESSION, raw).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY_SESSION).apply()
    }
}
