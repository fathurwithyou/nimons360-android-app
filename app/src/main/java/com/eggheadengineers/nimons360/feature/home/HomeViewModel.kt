package com.eggheadengineers.nimons360.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelProvider
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

data class HomeUiState(
    val myFamilies: List<String> = emptyList(),
    val discoverFamilies: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState
    private var loadJob: Job? = null


    init {
        viewModelScope.launch {
            familyRepository.observeFamilyChange().collect {
                load()
            }
        }
        load()
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = uiState.value.copy(isLoading = true, error = null)
            try {
                withTimeout(LOAD_TIMEOUT_MS) {
                    val myResult = familyRepository.getMyFamilies()
                    val discoverResult = familyRepository.getDiscoverFamilies()
                    val firstError = myResult.exceptionOrNull() ?: discoverResult.exceptionOrNull()
                    _uiState.value = HomeUiState(
                        myFamilies = myResult.getOrDefault(emptyList()),
                        discoverFamilies = discoverResult.getOrDefault(emptyList()),
                        isLoading = false,
                        error = firstError?.userFriendlyMessage("Failed to load families")
                    )
                }
            } catch (e: TimeoutCancellationException) {
                _uiState.value = uiState.value.copy(
                    isLoading = false, 
                    error = "The server is taking too long to respond. Please try again later."
                )
            }
        }
    }

    class Factory(private val familyRepository: FamilyRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = HomeViewModel() as T
    }

    private companion object {
        const val LOAD_TIMEOUT_MS = 15_000L
    }
}


