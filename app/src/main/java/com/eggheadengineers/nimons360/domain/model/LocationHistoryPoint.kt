package com.eggheadengineers.nimons360.domain.model

data class LocationHistoryPoint(
    val id: Long,
    val lat: Double,
    val lng: Double,
    val recordedAt: Long,
)
