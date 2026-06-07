package com.eggheadengineers.nimons360.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eggheadengineers.nimons360.domain.model.FavoriteLocation
import com.eggheadengineers.nimons360.domain.model.LocationHistoryPoint
import com.eggheadengineers.nimons360.domain.repository.FavoriteLocationRepository
import com.eggheadengineers.nimons360.domain.repository.LocationHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class DailyDistance(
    val dayKey: String,
    val dayOfMonth: Int,
    val monthKey: String,
    val distanceKm: Double,
)

data class MonthlyDistance(
    val monthKey: String,
    val distanceKm: Double,
)

data class AnalyticsUiState(
    val history: List<LocationHistoryPoint> = emptyList(),
    val markedLocations: List<FavoriteLocation> = emptyList(),
) {
    val sortedHistory: List<LocationHistoryPoint>
        get() = history.sortedBy { it.recordedAt }

    val recentLocations: List<LocationHistoryPoint>
        get() = history.sortedByDescending { it.recordedAt }

    val dailyDistances: List<DailyDistance>
        get() = buildDailyDistances(sortedHistory)

    val monthlyDistances: List<MonthlyDistance>
        get() = dailyDistances
            .groupBy { it.monthKey }
            .map { (month, days) -> MonthlyDistance(month, days.sumOf { it.distanceKm }) }
            .sortedBy { it.monthKey }

    val selectedMonthKey: String
        get() = monthlyDistances.lastOrNull()?.monthKey ?: currentMonthKey()

    val selectedMonthDailyDistances: List<DailyDistance>
        get() = buildMonthGraph(selectedMonthKey, dailyDistances)

    val selectedMonthTotalKm: Double
        get() = monthlyDistances.firstOrNull { it.monthKey == selectedMonthKey }?.distanceKm ?: 0.0

    val totalDistanceKm: Double
        get() = dailyDistances.sumOf { it.distanceKm }

    val monthlyDistanceAverageKm: Double
        get() = monthlyDistances.map { it.distanceKm }.averageOrZero()

    val dailyDistanceAverageKm: Double
        get() = dailyDistances.filter { it.distanceKm > 0.0 }.map { it.distanceKm }.averageOrZero()

    val activeDays: Int
        get() = sortedHistory.map { dayKey(it.recordedAt) }.toSet().size

    val photoCount: Int
        get() = markedLocations.sumOf { it.photoPaths.size }
}

class AnalyticsViewModel(
    locationHistoryRepository: LocationHistoryRepository,
    favoriteLocationRepository: FavoriteLocationRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState

    init {
        combine(
            locationHistoryRepository.observeAll(),
            favoriteLocationRepository.observeAll(),
        ) { history, markedLocations ->
            AnalyticsUiState(history = history, markedLocations = markedLocations)
        }
            .onEach { state -> _uiState.value = state }
            .launchIn(viewModelScope)
    }

    class Factory(
        private val locationHistoryRepository: LocationHistoryRepository,
        private val favoriteLocationRepository: FavoriteLocationRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            AnalyticsViewModel(locationHistoryRepository, favoriteLocationRepository) as T
    }
}

fun analyticsCsv(state: AnalyticsUiState): String {
    val analyticsRows = listOf(
        "metric,value",
        "total_distance_km,${state.totalDistanceKm}",
        "monthly_distance_average_km,${state.monthlyDistanceAverageKm}",
        "daily_distance_average_km,${state.dailyDistanceAverageKm}",
        "active_days,${state.activeDays}",
        "location_history_count,${state.history.size}",
        "marked_location_count,${state.markedLocations.size}",
        "photo_count,${state.photoCount}",
        "",
        "daily_distance",
        "day,month,distance_km",
    ) + state.dailyDistances.map { "${it.dayKey},${it.monthKey},${it.distanceKm}" }

    val historyHeader = listOf(
        "",
        "location_history",
        "id,latitude,longitude,recorded_at",
    )
    val historyRows = state.history.sortedByDescending { it.recordedAt }.map { location ->
        listOf(
            location.id.toString(),
            location.lat.toString(),
            location.lng.toString(),
            location.recordedAt.toString(),
        ).joinToString(",")
    }
    val markedLocationHeader = listOf(
        "",
        "marked_locations",
        "id,name,description,latitude,longitude,photo_count,created_at,updated_at",
    )
    val markedLocationRows = state.markedLocations.sortedByDescending { it.updatedAt }.map { location ->
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
    return (analyticsRows + historyHeader + historyRows + markedLocationHeader + markedLocationRows)
        .joinToString("\n") + "\n"
}

private fun buildDailyDistances(locations: List<LocationHistoryPoint>): List<DailyDistance> =
    locations.zipWithNext { previous, current ->
        val timestamp = current.recordedAt
        DailyDistance(
            dayKey = dayKey(timestamp),
            dayOfMonth = dayOfMonth(timestamp),
            monthKey = monthKey(timestamp),
            distanceKm = distanceKm(previous.lat, previous.lng, current.lat, current.lng),
        )
    }.groupBy { it.dayKey }
        .map { (_, segments) ->
            val first = segments.first()
            first.copy(distanceKm = segments.sumOf { it.distanceKm })
        }
        .sortedBy { it.dayKey }

private fun buildMonthGraph(monthKey: String, dailyDistances: List<DailyDistance>): List<DailyDistance> {
    val calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.US)
    val monthFormat = SimpleDateFormat("yyyy-MM", Locale.US)
    calendar.time = monthFormat.parse(monthKey) ?: return emptyList()
    val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val byDay = dailyDistances.filter { it.monthKey == monthKey }.associateBy { it.dayOfMonth }
    return (1..maxDay).map { day ->
        byDay[day] ?: DailyDistance(
            dayKey = "$monthKey-${day.toString().padStart(2, '0')}",
            dayOfMonth = day,
            monthKey = monthKey,
            distanceKm = 0.0,
        )
    }
}

private fun List<Double>.averageOrZero(): Double =
    if (isEmpty()) 0.0 else average()

private fun dayKey(timestamp: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(timestamp)

private fun monthKey(timestamp: Long): String =
    SimpleDateFormat("yyyy-MM", Locale.US).format(timestamp)

private fun dayOfMonth(timestamp: Long): Int {
    val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    return calendar.get(Calendar.DAY_OF_MONTH)
}

private fun currentMonthKey(): String =
    SimpleDateFormat("yyyy-MM", Locale.US).format(System.currentTimeMillis())

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
