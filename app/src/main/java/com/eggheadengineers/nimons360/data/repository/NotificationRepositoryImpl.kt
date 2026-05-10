package com.eggheadengineers.nimons360.data.repository

import com.eggheadengineers.nimons360.data.dto.SendFamilyNotificationRequestDto
import com.eggheadengineers.nimons360.data.dto.SendGreetingNotificationRequestDto
import com.eggheadengineers.nimons360.data.dto.SubscribeDeviceTokenRequestDto
import com.eggheadengineers.nimons360.data.network.ApiService
import com.eggheadengineers.nimons360.data.network.requireSuccess
import com.eggheadengineers.nimons360.domain.repository.NotificationRepository

class NotificationRepositoryImpl(
    private val apiService: ApiService,
) : NotificationRepository {

    override suspend fun subscribeDeviceToken(fcmToken: String): Result<Boolean> = runCatching {
        val response = apiService.subscribeDeviceToken(SubscribeDeviceTokenRequestDto(fcmToken))
        response.requireSuccess("Failed to subscribe notification token")
        response.body()?.data?.subscribed ?: error("Empty subscribe response")
    }

    override suspend fun unsubscribeDeviceToken(): Result<Boolean> = runCatching {
        val response = apiService.unsubscribeDeviceToken()
        response.requireSuccess("Failed to unsubscribe notification token")
        response.body()?.data?.unsubscribed ?: error("Empty unsubscribe response")
    }

    override suspend fun sendFamilyNotification(familyId: String, message: String): Result<Boolean> = runCatching {
        val response = apiService.sendFamilyNotification(
            SendFamilyNotificationRequestDto(
                familyId = familyId.toIntOrNull() ?: error("Invalid familyId"),
                message = message,
            )
        )
        response.requireSuccess("Failed to send family notification")
        response.body()?.data?.sent ?: error("Empty family notification response")
    }

    override suspend fun sendGreeting(
        familyId: String,
        targetUserId: String,
        message: String,
    ): Result<Boolean> = runCatching {
        val response = apiService.sendGreetingNotification(
            SendGreetingNotificationRequestDto(
                familyId = familyId.toIntOrNull() ?: error("Invalid familyId"),
                targetUserId = targetUserId.toIntOrNull() ?: error("Invalid targetUserId"),
                message = message,
            )
        )
        response.requireSuccess("Failed to send greeting")
        response.body()?.data?.delivered ?: error("Empty greeting response")
    }
}
