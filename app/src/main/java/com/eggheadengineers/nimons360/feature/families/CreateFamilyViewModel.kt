package com.eggheadengineers.nimons360.feature.families

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.eggheadengineers.nimons360.data.network.userFriendlyMessage
import com.eggheadengineers.nimons360.domain.model.FamilyDetail
import com.eggheadengineers.nimons360.domain.repository.FamilyRepository

sealed interface CreateFamilyUiState {
    data object Idle : CreateFamilyUiState
    data object Loading : CreateFamilyUiState
    data class Success(val familyId: String) : CreateFamilyUiState
    data class Error(val message: String) : CreateFamilyUiState
}

class CreateFamilyViewModel(private val familyRepository: FamilyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<CreateFamilyUiState>(CreateFamilyUiState.Idle)
    val uiState: StateFlow<CreateFamilyUiState> = _uiState

    fun createFamily(name: String, iconUrl: String) {
        if (name.isBlank()) {
            _uiState.value = CreateFamilyUiState.Error("Enter family name")
            return
        }
        if (iconUrl.isBlank()) {
            _uiState.value = CreateFamilyUiState.Error("Choose an icon")
            return
        }

        viewModelScope.launch {
            _uiState.value = CreateFamilyUiState.Loading
            familyRepository.createFamily(name.trim(), iconUrl).fold(
                onSuccess = {
                    _uiState.value = CreateFamilyUiState.Success(it.id)
                },
                onFailure = {
                    _uiState.value = CreateFamilyUiState.Error(it.userFriendlyMessage("Failed to create family"))
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = CreateFamilyUiState.Idle
    }

    class Factory(private val repo: FamilyRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = CreateFamilyViewModel(repo) as T
    }
}