package com.eggheadengineers.nimons360.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class WsEnvelopeDto(
    @SerialName("type") val type: String,
    @SerialName("payload") val payload: JsonElement?,
    @SerialName("timestamp") val timestamp: String?,
)

@Serializable
data class UpdatePresencePayloadDto(
    @SerialName("name") val name: String,
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("rotation") val rotation: Float,
    @SerialName("batteryLevel") val batteryLevel: Int,
    @SerialName("isCharging") val isCharging: Boolean,
    @SerialName("internetStatus") val internetStatus: String,
    @SerialName("metadata") val metadata: JsonObject = JsonObject(emptyMap()),
)

@Serializable
data class MemberPresenceUpdatedPayloadDto(
    @SerialName("userId") val userId: String?,
    @SerialName("id") val id: String?,
    @SerialName("fullName") val fullName: String?,
    @SerialName("email") val email: String?,
    @SerialName("latitude") val latitude: Double?,
    @SerialName("longitude") val longitude: Double?,
    @SerialName("rotation") val rotation: Float?,
    @SerialName("batteryLevel") val batteryLevel: Int?,
    @SerialName("isCharging") val isCharging: Boolean?,
    @SerialName("internetStatus") val internetStatus: String?,
    @SerialName("metadata") val metadata: JsonObject?,
)
