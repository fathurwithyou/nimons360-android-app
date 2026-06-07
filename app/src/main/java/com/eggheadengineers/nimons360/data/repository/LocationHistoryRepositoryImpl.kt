package com.eggheadengineers.nimons360.data.repository

import com.eggheadengineers.nimons360.data.local.LocationHistoryDao
import com.eggheadengineers.nimons360.data.local.LocationHistoryEntity
import com.eggheadengineers.nimons360.domain.model.LocationHistoryPoint
import com.eggheadengineers.nimons360.domain.repository.LocationHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocationHistoryRepositoryImpl(
    private val dao: LocationHistoryDao,
) : LocationHistoryRepository {
    override fun observeAll(): Flow<List<LocationHistoryPoint>> =
        dao.observeAll().map { points -> points.map { it.toDomain() } }

    override suspend fun record(lat: Double, lng: Double, recordedAt: Long) {
        if (lat == 0.0 && lng == 0.0) return
        dao.insert(LocationHistoryEntity(lat = lat, lng = lng, recordedAt = recordedAt))
    }

    private fun LocationHistoryEntity.toDomain() = LocationHistoryPoint(
        id = id,
        lat = lat,
        lng = lng,
        recordedAt = recordedAt,
    )
}
