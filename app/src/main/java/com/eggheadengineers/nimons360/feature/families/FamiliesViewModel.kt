package com.eggheadengineers.nimons360.feature.families

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import com.eggheadengineers.nimons360.data.network.userFriendlyMessage
import com.eggheadengineers.nimons360.domain.model.Family
import com.eggheadengineers.nimons360.domain.repository.FamilyRepository

enum class FamiliesFilter { ALL, MY_FAMILIES }

data class FamiliesUiState(
    val families: List<Family> = emptyList(),
    val filter: FamiliesFilter = FamiliesFilter.ALL,
    val pinnedIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val displayedFamilies: List<Family>
        get() {
            val query = searchQuery.trim()
            val base = families.map {
                it.copy(isPinned = it.id in pinnedIds)
            }
            val filtered = if (query.isBlank()) {
                base
            } else {
                base.filter { it.name.contains(query, ignoreCase = true) }
            }
            return filtered.sortedWith(
                compareByDescending<Family> { it.isPinned }.thenBy { it.name.lowercase() }
            )
        }
}

class FamiliesViewModel(private val familyRepository: FamilyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FamiliesUiState())
    val uiState: StateFlow<FamiliesUiState> = _uiState
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            familyRepository.getPinnedFamilyIds().collect {
                ids -> _uiState.update {
                    it.copy(pinnedIds = ids)
                }
            }
        }
        viewModelScope.launch {
            familyRepository.observeFamilyChange().collect {
                load()
            }
        }
        load()
    }

    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                withTimeout(LOAD_TIMEOUT_MS) {
                    val result = when (uiState.value.filter) {
                        FamiliesFilter.ALL -> familyRepository.getDiscoverFamilies()
                        FamiliesFilter.MY_FAMILIES -> familyRepository.getMyFamilies()
                    }
                    _uiState.update {
                        state -> state.copy(
                            families = result.getOrDefault(emptyList()),
                            isLoading = false,
                            error = result.exceptionOrNull()?.userFriendlyMessage("Failed to load families")
                        )
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _uiState.update {
                    it.copy(
                    isLoading = false, 
                    error = "The server is taking too long to respond. Please try again later."
                )
            }
        }
    }

    fun setFilter(filter: FamiliesFilter) {
        _uiState.update { it.copy(filter = filter) }
        load()
    }

    fun setSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun togglePin(familyId: String) {
        viewModelScope.launch {
            if (familyId in uiState.value.pinnedIds) {
                familyRepository.unpinFamily(familyId)
            } else {
                familyRepository.pinFamily(familyId)
            }
        }
    }

    class Factory(private val repo: FamilyRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = FamiliesViewModel(repo) as T
    }

    private companion object {
        const val LOAD_TIMEOUT_MS = 15_000L
    }
}

