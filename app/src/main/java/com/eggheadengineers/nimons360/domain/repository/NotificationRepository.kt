package com.eggheadengineers.nimons360.domain.repository

interface NotificationRepository {
    suspend fun subscribeDeviceToken(fcmToken: String): Result<Boolean>
    suspend fun unsubscribeDeviceToken(): Result<Boolean>
    suspend fun sendFamilyNotification(familyId: String, message: String): Result<Boolean>
    suspend fun sendGreeting(familyId: String, targetUserId: String, message: String): Result<Boolean>
}
