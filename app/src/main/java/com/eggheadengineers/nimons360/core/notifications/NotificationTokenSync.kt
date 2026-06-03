package com.eggheadengineers.nimons360.core.notifications

import com.eggheadengineers.nimons360.core.preferences.UserPreferenceStore
import com.eggheadengineers.nimons360.domain.repository.NotificationRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class NotificationTokenSync(
    private val notificationRepository: NotificationRepository,
    private val userPreferenceStore: UserPreferenceStore,
) {
    suspend fun syncPreference(enabled: Boolean) {
        userPreferenceStore.setNotificationEnabled(enabled)
        if (!enabled) {
            notificationRepository.unsubscribeDeviceToken()
            return
        }

        val token = currentTokenOrNull() ?: return
        notificationRepository.subscribeDeviceToken(token)
    }

    suspend fun syncCurrentTokenIfEnabled() {
        if (!userPreferenceStore.isNotificationEnabled()) return
        val token = currentTokenOrNull() ?: return
        notificationRepository.subscribeDeviceToken(token)
    }

    suspend fun syncRefreshedToken(token: String) {
        if (!userPreferenceStore.isNotificationEnabled()) return
        notificationRepository.subscribeDeviceToken(token)
    }

    private suspend fun currentTokenOrNull(): String? = runCatching {
        FirebaseApp.getInstance()
        FirebaseMessaging.getInstance().token.await()
    }.getOrNull()
}
