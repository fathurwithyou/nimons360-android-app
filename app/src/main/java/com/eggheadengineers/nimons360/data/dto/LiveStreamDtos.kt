package com.eggheadengineers.nimons360.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LiveStreamDto(
    @SerialName("id") val id: String,
    @SerialName("familyId") val familyId: String,
    @SerialName("broadcasterId") val broadcasterId: String,
    @SerialName("broadcasterName") val broadcasterName: String,
    @SerialName("title") val title: String,
    @SerialName("startedAt") val startedAt: Long,
    @SerialName("rtmpUrl") val rtmpUrl: String,
    @SerialName("streamKey") val streamKey: String,
    @SerialName("hlsUrl") val hlsUrl: String,
)

@Serializable
data class StartStreamRequestDto(
    @SerialName("familyId") val familyId: String,
    @SerialName("broadcasterId") val broadcasterId: String,
    @SerialName("broadcasterName") val broadcasterName: String,
    @SerialName("title") val title: String? = null,
)

@Serializable
data class EndStreamRequestDto(
    @SerialName("broadcasterId") val broadcasterId: String,
)

@Serializable
data class LiveStreamApiResponse(
    @SerialName("data") val data: LiveStreamDto?,
)

@Serializable
data class LiveStreamListApiResponse(
    @SerialName("data") val data: List<LiveStreamDto>?,
)

@Serializable
data class StreamEndedPayloadDto(
    @SerialName("id") val id: String,
    @SerialName("familyId") val familyId: String,
)
