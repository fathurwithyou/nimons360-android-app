package com.eggheadengineers.nimons360.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.eggheadengineers.nimons360.core.battery.BatteryProvider
import com.eggheadengineers.nimons360.core.battery.BatteryState
import com.eggheadengineers.nimons360.core.location.LocationTracker
import com.eggheadengineers.nimons360.core.network.ConnectivityObserver
import com.eggheadengineers.nimons360.core.network.NetworkStatus
import com.eggheadengineers.nimons360.core.sensor.OrientationProvider
import com.eggheadengineers.nimons360.domain.model.Family
import com.eggheadengineers.nimons360.domain.model.FavoriteLocation
import com.eggheadengineers.nimons360.domain.model.MemberPresence
import com.eggheadengineers.nimons360.domain.repository.FamilyRepository
import com.eggheadengineers.nimons360.domain.repository.FavoriteLocationRepository
import com.eggheadengineers.nimons360.domain.repository.PresenceRepository

data class MapUiState(
    val myLat: Double = 0.0,
    val myLng: Double = 0.0,
    val myRotation: Float = 0f,
    val members: Map<String, MemberPresence> = emptyMap(),
    val battery: BatteryState = BatteryState(0, false),
    val networkStatus: NetworkStatus = NetworkStatus.OFFLINE,
    val selectedMemberId: String? = null,
    val hasLocationPermission: Boolean = false,
    val families: List<Family> = emptyList(),
    val selectedFamilyIds: Set<String> = emptySet(),
    val favoriteLocations: List<FavoriteLocation> = emptyList(),
    val pendingFavoriteLat: Double? = null,
    val pendingFavoriteLng: Double? = null,
) {
    val selectedMember: MemberPresence? 
        get() = selectedMemberId?.let { members[it] }
    val filteredMembers: Map<String, MemberPresence> 
        get() {
            if (selectedFamilyIds.isEmpty()) return members
            val allowedUserIds = families
                .filter { it.id in selectedFamilyIds }
                .flatMap { it.members }
                .map { it.id }
                .toSet()
            return members.filter { (userId, _) -> userId in allowedUserIds }
        }
}

class MapViewModel(
    private val presenceRepository: PresenceRepository,
    private val familyRepository: FamilyRepository,
    private val favoriteLocationRepository: FavoriteLocationRepository,
    private val locationTracker: LocationTracker,
    private val orientationProvider: OrientationProvider,
    private val batteryProvider: BatteryProvider,
    private val connectivityObserver: ConnectivityObserver,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState

    private var presenceJob: Job? = null
    private var staleCleanupJob: Job? = null
    private var trackingStarted = false

    init {
        loadFamilies()
        viewModelScope.launch {
            familyRepository.observeFamilyChanges().collect { loadFamilies() }
        }
        viewModelScope.launch {
            favoriteLocationRepository.observeAll().collect { favorites ->
                _uiState.update { it.copy(favoriteLocations = favorites) }
            }
        }
    }

    private fun loadFamilies() {
        viewModelScope.launch {
            familyRepository.getMyFamilies().onSuccess { families ->
                _uiState.update { it.copy(families = families) }
            }
        }
    }

    fun clearFamilyFilter() {
        _uiState.update { it.copy(selectedFamilyIds = emptySet()) }
    }

    fun toggleFamily(familyId: String) {
        _uiState.update { state ->
            val newIds = if (familyId in state.selectedFamilyIds) {
                state.selectedFamilyIds - familyId
            } else {
                state.selectedFamilyIds + familyId
            }
            state.copy(selectedFamilyIds = newIds)
        }
    }

    fun onPermissionGranted() {
        _uiState.update { it.copy(hasLocationPermission = true) }
        if (!trackingStarted) {
            trackingStarted = true
            startTracking()
        }
    }

    private fun startTracking() {
        presenceRepository.connect()

        viewModelScope.launch {
            presenceRepository.observeMembers().collect { members ->
                _uiState.update { it.copy(members = members) }
            }
        }

        staleCleanupJob = viewModelScope.launch {
            while (true) {
                delay(10_000)
                presenceRepository.removeStaleMembers(90_000)
            }
        }

        viewModelScope.launch {
            batteryProvider.batteryStateFlow().collect { battery ->
                _uiState.update { it.copy(battery = battery) }
            }
        }

        viewModelScope.launch {
            connectivityObserver.status.collect { status ->
                _uiState.update { it.copy(networkStatus = status) }
            }
        }

        viewModelScope.launch {
            orientationProvider.azimuthFlow().collect { azimuth ->
                _uiState.update { it.copy(myRotation = azimuth) }
            }
        }

        presenceJob = viewModelScope.launch {
            locationTracker.locationUpdates(3000).collect { location ->
                _uiState.update { it.copy(myLat = location.latitude, myLng = location.longitude) }
                val s = _uiState.value
                val internetStatus = when (s.networkStatus) {
                    NetworkStatus.WIFI -> "wifi"
                    NetworkStatus.MOBILE -> "mobile"
                    NetworkStatus.OFFLINE -> "offline"
                }
                presenceRepository.sendPresence(
                    lat = location.latitude,
                    lng = location.longitude,
                    rotation = s.myRotation,
                    battery = s.battery.level,
                    charging = s.battery.charging,
                    internetStatus = internetStatus,
                )
            }
        }
    }

    fun requestAddFavorite(lat: Double, lng: Double) {
        _uiState.update { it.copy(pendingFavoriteLat = lat, pendingFavoriteLng = lng) }
    }

    fun confirmAddFavorite(name: String) {
        val state = _uiState.value
        val lat = state.pendingFavoriteLat ?: return
        val lng = state.pendingFavoriteLng ?: return
        _uiState.update { it.copy(pendingFavoriteLat = null, pendingFavoriteLng = null) }
        viewModelScope.launch {
            favoriteLocationRepository.add(name, lat, lng)
        }
    }

    fun cancelAddFavorite() {
        _uiState.update { it.copy(pendingFavoriteLat = null, pendingFavoriteLng = null) }
    }

    fun deleteFavorite(id: Long) {
        viewModelScope.launch {
            favoriteLocationRepository.delete(id)
        }
    }

    fun selectMember(member: MemberPresence?) {
        _uiState.update { it.copy(selectedMemberId = member?.userId) }
    }

    override fun onCleared() {
        super.onCleared()
        presenceRepository.disconnect()
        presenceJob?.cancel()
        staleCleanupJob?.cancel()
    }

    class Factory(
        private val presenceRepo: PresenceRepository,
        private val familyRepo: FamilyRepository,
        private val favoriteLocationRepo: FavoriteLocationRepository,
        private val locationTracker: LocationTracker,
        private val orientationProvider: OrientationProvider,
        private val batteryProvider: BatteryProvider,
        private val connectivityObserver: ConnectivityObserver,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = MapViewModel(
            presenceRepo, familyRepo, favoriteLocationRepo, locationTracker,
            orientationProvider, batteryProvider, connectivityObserver,
        ) as T
    }
}