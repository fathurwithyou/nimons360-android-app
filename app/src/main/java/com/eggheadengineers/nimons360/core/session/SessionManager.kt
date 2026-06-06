package com.eggheadengineers.nimons360.core.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {
    private val crypto = SessionCrypto()

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_PROFILE_IMAGE_URL = stringPreferencesKey("user_profile_image_url")
        private val KEY_UPLOADED_PHOTO_URL = stringPreferencesKey("user_uploaded_photo_url")
        private val KEY_UPLOADED_PHOTO_OWNER = stringPreferencesKey("user_uploaded_photo_owner")
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[KEY_TOKEN] = crypto.encrypt(token) }
    }

    suspend fun getToken(): String? {
        val stored = context.dataStore.data.map { it[KEY_TOKEN] }.first() ?: return null
        val decrypted = crypto.decrypt(stored)
        if (decrypted != null) {
            return decrypted
        }
        saveToken(stored)
        return stored
    }

    suspend fun clearToken() {
        context.dataStore.edit { it.remove(KEY_TOKEN) }
    }

    suspend fun isLoggedIn(): Boolean = getToken() != null

    suspend fun saveUserName(name: String) {
        context.dataStore.edit { it[KEY_USER_NAME] = name }
    }

    suspend fun getUserName(): String? =
        context.dataStore.data.map { it[KEY_USER_NAME] }.first()

    suspend fun saveUserId(id: String) {
        context.dataStore.edit { it[KEY_USER_ID] = id }
    }

    suspend fun getUserId(): String? =
        context.dataStore.data.map { it[KEY_USER_ID] }.first()

    suspend fun saveUserProfileImageUrl(url: String?) {
        context.dataStore.edit {
            if (url.isNullOrBlank()) {
                it.remove(KEY_USER_PROFILE_IMAGE_URL)
            } else {
                it[KEY_USER_PROFILE_IMAGE_URL] = url
            }
        }
    }

    suspend fun getUserProfileImageUrl(): String? =
        context.dataStore.data.map { prefs ->
            prefs.effectiveUploadedUrl() ?: prefs[KEY_USER_PROFILE_IMAGE_URL]
        }.first()

    suspend fun saveUploadedProfileImageUrl(url: String?) {
        val userId = getUserId()
        context.dataStore.edit {
            if (url.isNullOrBlank() || userId == null) {
                it.remove(KEY_UPLOADED_PHOTO_URL)
                it.remove(KEY_UPLOADED_PHOTO_OWNER)
            } else {
                it[KEY_UPLOADED_PHOTO_URL] = url
                it[KEY_UPLOADED_PHOTO_OWNER] = userId
            }
        }
    }

    suspend fun getUploadedProfileImageUrl(): String? =
        context.dataStore.data.map { it.effectiveUploadedUrl() }.first()

    fun observeProfileImageUrl(): Flow<String?> =
        context.dataStore.data.map { prefs ->
            prefs.effectiveUploadedUrl() ?: prefs[KEY_USER_PROFILE_IMAGE_URL]
        }

    private fun Preferences.effectiveUploadedUrl(): String? {
        val owner = this[KEY_UPLOADED_PHOTO_OWNER]
        val currentUserId = this[KEY_USER_ID]
        return if (owner != null && owner == currentUserId) this[KEY_UPLOADED_PHOTO_URL] else null
    }
}
