package com.eggheadengineers.nimons360.data.dto

import com.google.gson.annotations.SerializedName

data class WsEnvelopeDto(
    @SerializedName("type") val type: String,
    @SerializedName("payload") val payload: Any?,
    @SerializedName("timestamp") val timestamp: String?,
)

data class UpdatePresencePayloadDto(
    @SerializedName("name") val name: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("rotation") val rotation: Float,
    @SerializedName("batteryLevel") val batteryLevel: Int,
    @SerializedName("isCharging") val isCharging: Boolean,
    @SerializedName("internetStatus") val internetStatus: String,
    @SerializedName("metadata") val metadata: Map<String, Any> = emptyMap(),
)

data class MemberPresenceUpdatedPayloadDto(
    @SerializedName("userId") val userId: String?,
    @SerializedName("id") val id: String?,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("rotation") val rotation: Float?,
    @SerializedName("batteryLevel") val batteryLevel: Int?,
    @SerializedName("isCharging") val isCharging: Boolean?,
    @SerializedName("internetStatus") val internetStatus: String?,
    @SerializedName("metadata") val metadata: Map<String, Any>?,
)
