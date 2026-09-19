package com.takeawaypro.app

import android.content.Context
import kotlinx.serialization.json.Json

private const val PREFS_NAME = "takeaway_pro_store"
private const val KEY_SNAPSHOT = "app_snapshot_json"

/**
 * Saves and restores the app's data on this one device, so a restaurant/café
 * doesn't lose its account, products, orders and notifications when the app
 * is closed. This is a local, single-device stand-in for a real backend —
 * nothing here syncs between phones. A production version needs the data
 * (accounts, products, orders, stock) to live in a real database reachable
 * from every device, per the original architecture plan.
 */
class LocalStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun load(): AppSnapshot? {
        val raw = prefs.getString(KEY_SNAPSHOT, null) ?: return null
        return try {
            json.decodeFromString(AppSnapshot.serializer(), raw)
        } catch (e: Exception) {
            // Corrupt or outdated snapshot (e.g. after a model change) — start fresh
            // rather than crash the app.
            null
        }
    }

    fun save(snapshot: AppSnapshot) {
        val raw = json.encodeToString(AppSnapshot.serializer(), snapshot)
        prefs.edit().putString(KEY_SNAPSHOT, raw).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY_SNAPSHOT).apply()
    }
}
