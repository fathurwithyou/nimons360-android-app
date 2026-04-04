package com.eggheadengineers.nimons360.domain.repository

import com.eggheadengineers.nimons360.domain.model.FavoriteLocation
import kotlinx.coroutines.flow.Flow

interface FavoriteLocationRepository {
    fun observeAll(): Flow<List<FavoriteLocation>>
    suspend fun add(name: String, lat: Double, lng: Double)
    suspend fun delete(id: Long)
}
