@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.eggheadengineers.nimons360.data.dto

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class WsEnvelopeDto(
    @SerialName("type") val type: String,
    @SerialName("payload") val payload: JsonElement? = null,
    @SerialName("timestamp") val timestamp: String? = null,
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
    @EncodeDefault
    @SerialName("metadata") val metadata: JsonObject = JsonObject(emptyMap()),
)

@Serializable
data class MemberPresenceUpdatedPayloadDto(
    @SerialName("userId") val userId: Int? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("fullName") val fullName: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("rotation") val rotation: Float? = null,
    @SerialName("batteryLevel") val batteryLevel: Int? = null,
    @SerialName("isCharging") val isCharging: Boolean? = null,
    @SerialName("internetStatus") val internetStatus: String? = null,
    @SerialName("metadata") val metadata: JsonObject? = null,
    @SerialName("profileImageUrl") val profileImageUrl: String? = null,
)
