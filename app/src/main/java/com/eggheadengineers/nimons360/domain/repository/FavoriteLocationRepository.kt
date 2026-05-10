package com.eggheadengineers.nimons360.domain.repository

import com.eggheadengineers.nimons360.domain.model.FavoriteLocation
import com.eggheadengineers.nimons360.domain.model.FavoriteLocationPhotoInput
import kotlinx.coroutines.flow.Flow

interface FavoriteLocationRepository {
    fun observeAll(): Flow<List<FavoriteLocation>>
    suspend fun add(
        name: String,
        description: String,
        lat: Double,
        lng: Double,
        photos: List<FavoriteLocationPhotoInput> = emptyList(),
    )
    suspend fun update(
        id: Long,
        name: String,
        description: String,
        photosToAdd: List<FavoriteLocationPhotoInput> = emptyList(),
    )
    suspend fun delete(id: Long)
}
