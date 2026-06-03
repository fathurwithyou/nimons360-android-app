package com.eggheadengineers.nimons360.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eggheadengineers.nimons360.domain.model.FavoriteLocation
import com.eggheadengineers.nimons360.domain.repository.FavoriteLocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class AnalyticsUiState(
    val locations: List<FavoriteLocation> = emptyList(),
) {
    val recentLocations: List<FavoriteLocation>
        get() = locations.sortedByDescending { it.updatedAt }.take(5)

    val segmentDistancesKm: List<Double>
        get() = locations.sortedBy { it.createdAt }
            .zipWithNext { a, b -> distanceKm(a.lat, a.lng, b.lat, b.lng) }

    val totalDistanceKm: Double
        get() = segmentDistancesKm.sum()

    val photoCount: Int
        get() = locations.sumOf { it.photoPaths.size }
}

class AnalyticsViewModel(
    favoriteLocationRepository: FavoriteLocationRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState

    init {
        favoriteLocationRepository.observeAll()
            .onEach { locations -> _uiState.value = AnalyticsUiState(locations) }
            .launchIn(viewModelScope)
    }

    class Factory(
        private val favoriteLocationRepository: FavoriteLocationRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            AnalyticsViewModel(favoriteLocationRepository) as T
    }
}

fun analyticsCsv(state: AnalyticsUiState): String {
    val header = "id,name,description,latitude,longitude,photo_count,created_at,updated_at"
    val rows = state.locations.sortedByDescending { it.updatedAt }.joinToString("\n") { location ->
        listOf(
            location.id.toString(),
            location.name.csvEscape(),
            location.description.csvEscape(),
            location.lat.toString(),
            location.lng.toString(),
            location.photoPaths.size.toString(),
            location.createdAt.toString(),
            location.updatedAt.toString(),
        ).joinToString(",")
    }
    return "$header\n$rows\n"
}

private fun String.csvEscape(): String =
    if (any { it == '"' || it == ',' || it == '\n' }) {
        "\"${replace("\"", "\"\"")}\""
    } else {
        this
    }

private fun distanceKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2).pow(2.0) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2.0)
    return earthRadiusKm * 2 * atan2(sqrt(a), sqrt(1 - a))
}
