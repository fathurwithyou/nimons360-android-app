package com.eggheadengineers.nimons360.data.repository

import com.eggheadengineers.nimons360.data.local.FavoriteLocationDao
import com.eggheadengineers.nimons360.data.local.FavoriteLocationEntity
import com.eggheadengineers.nimons360.domain.model.FavoriteLocation
import com.eggheadengineers.nimons360.domain.repository.FavoriteLocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteLocationRepositoryImpl(
    private val dao: FavoriteLocationDao,
) : FavoriteLocationRepository {

    override fun observeAll(): Flow<List<FavoriteLocation>> =
        dao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun add(name: String, lat: Double, lng: Double) {
        dao.insert(FavoriteLocationEntity(name = name, lat = lat, lng = lng))
    }

    override suspend fun delete(id: Long) {
        dao.deleteById(id)
    }

    private fun FavoriteLocationEntity.toDomain() = FavoriteLocation(
        id = id,
        name = name,
        lat = lat,
        lng = lng,
        createdAt = createdAt,
    )
}
