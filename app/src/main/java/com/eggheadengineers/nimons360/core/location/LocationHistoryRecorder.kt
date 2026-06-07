package com.eggheadengineers.nimons360.core.location

import com.eggheadengineers.nimons360.domain.repository.LocationHistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LocationHistoryRecorder(
    private val locationTracker: LocationTracker,
    private val locationHistoryRepository: LocationHistoryRepository,
    private val scope: CoroutineScope,
) {
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return

        job = scope.launch {
            var lastRecordedAt = 0L
            locationTracker.locationUpdates(minTimeMs = LOCATION_HISTORY_INTERVAL_MS).collectLatest { location ->
                val now = System.currentTimeMillis()
                if (lastRecordedAt == 0L || now - lastRecordedAt >= LOCATION_HISTORY_INTERVAL_MS) {
                    locationHistoryRepository.record(
                        lat = location.latitude,
                        lng = location.longitude,
                        recordedAt = now,
                    )
                    lastRecordedAt = now
                }
            }
        }
    }

    companion object {
        private const val LOCATION_HISTORY_INTERVAL_MS = 3 * 60 * 1_000L
    }
}
