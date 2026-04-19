package com.eggheadengineers.nimons360.domain.model

data class LiveStream(
    val id: String,
    val familyId: String,
    val broadcasterId: String,
    val broadcasterName: String,
    val title: String,
    val startedAt: Long,
    val rtmpUrl: String,
    val streamKey: String,
    val hlsUrl: String,
)
