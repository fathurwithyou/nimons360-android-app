package com.eggheadengineers.nimons360.domain.model

data class FavoriteLocation(
    val id: Long,
    val name: String,
    val description: String,
    val lat: Double,
    val lng: Double,
    val photoPaths: List<String> = emptyList(),
    val createdAt: Long,
    val updatedAt: Long,
)

data class FavoriteLocationPhotoInput(
    val fileName: String,
    val bytes: ByteArray,
)
