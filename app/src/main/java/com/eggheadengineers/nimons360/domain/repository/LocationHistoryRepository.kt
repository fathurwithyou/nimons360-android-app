package com.eggheadengineers.nimons360.domain.repository

import com.eggheadengineers.nimons360.domain.model.LocationHistoryPoint
import kotlinx.coroutines.flow.Flow

interface LocationHistoryRepository {
    fun observeAll(): Flow<List<LocationHistoryPoint>>
    suspend fun record(lat: Double, lng: Double, recordedAt: Long = System.currentTimeMillis())
}
