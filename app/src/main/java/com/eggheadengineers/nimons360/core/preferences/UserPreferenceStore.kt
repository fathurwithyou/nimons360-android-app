package com.eggheadengineers.nimons360.core.preferences

import android.content.Context

class UserPreferenceStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun isNotificationEnabled(): Boolean =
        prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)

    fun setNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun isLocationSharingEnabled(): Boolean =
        prefs.getBoolean(KEY_LOCATION_SHARING_ENABLED, true)

    fun setLocationSharingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LOCATION_SHARING_ENABLED, enabled).apply()
    }

    fun getSelectedPinId(): String =
        prefs.getString(KEY_SELECTED_PIN_ID, "avatar") ?: "avatar"

    fun setSelectedPinId(id: String) {
        prefs.edit().putString(KEY_SELECTED_PIN_ID, id).apply()
    }

    companion object {
        private const val PREF_NAME = "user_preferences"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_LOCATION_SHARING_ENABLED = "location_sharing_enabled"
        private const val KEY_SELECTED_PIN_ID = "selected_pin_id"
    }
}
