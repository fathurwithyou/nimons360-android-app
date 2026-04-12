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

data class FamilyDetailUiState(
    val family: FamilyDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val feedback: FamilyDetailFeedback? = null,
)

data class FamilyDetailFeedback(
    val title: String,
    val message: String,
    val isSuccess: Boolean,    
)

class FamilyDetailViewModel(
    private val familyId: String,
    private val familyRepository: FamilyRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FamilyDetailUiState())
    val uiState: StateFlow<FamilyDetailUiState> = _uiState

    init {
        load()
    }

    fun load(feedback: FamilyDetailFeedback? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, feedback = null)
            familyRepository.getFamilyDetail(familyId).fold(
                onSuccess = {
                    _uiState.value = FamilyDetailUiState(detail = it, feedback = feedback)
                },
                onFailure = {
                    _uiState.value = FamilyDetailUiState(error = it.userFriendlyMessage("Failed to load family details"))
                }
            )
        }
    }

    fun joinFamily(code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            familyRepository.joinFamily(familyId, code).fold(
                onSuccess = {
                    load(
                        FamilyDetailFeedback(
                            title = "Joined family!",
                            message = "You are now part of this family.",
                            isSuccess = true,
                        )
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        feedback = FamilyDetailFeedback(
                            title = "Couldn't join family",
                            message = it.userFriendlyMessage("Check the family code and try again."),
                            isSuccess = false,
                        )
                    )
                },
            )
        }
    }

    fun leaveFamily() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            familyRepository.leaveFamily(familyId).fold(
                onSuccess = {
                    load(
                        FamilyDetailFeedback(
                            title = "Left family",
                            message = "You can join again any time with the family code.",
                            isSuccess = true,
                        )
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        feedback = FamilyDetailFeedback(
                            title = "Couldn't leave family",
                            message = it.userFriendlyMessage("Please try again in a moment."),
                            isSuccess = false,
                        )
                    )
                },
            )
        }
    }

    fun showFeedback(feedback: FamilyDetailFeedback) {
        _uiState.value = _uiState.value.copy(feedback = feedback)
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(feedback = null)
    }

    class Factory(
        private val familyId: String,
        private val repo: FamilyRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = FamilyDetailViewModel(familyId, repo) as T
    }
}
