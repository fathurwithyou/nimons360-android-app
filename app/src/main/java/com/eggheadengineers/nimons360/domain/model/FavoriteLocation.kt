package com.eggheadengineers.nimons360.domain.model

data class FavoriteLocation(
    val id: Long,
    val name: String,
    val lat: Double,
    val lng: Double,
    val createdAt: Long,
)
