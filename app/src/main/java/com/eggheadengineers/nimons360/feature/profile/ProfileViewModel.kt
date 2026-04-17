package com.eggheadengineers.nimons360.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.eggheadengineers.nimons360.data.network.userFriendlyMessage
import com.eggheadengineers.nimons360.domain.model.Profile
import com.eggheadengineers.nimons360.domain.repository.AuthRepository
import com.eggheadengineers.nimons360.domain.repository.ProfileRepository

data class ProfileUiState(
    val profile: Profile? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSignedOut: Boolean = false,
    val updateMessage: String? = null,
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState(isLoading = true)
            profileRepository.getProfile().fold(
                onSuccess = {
                    _uiState.value = ProfileUiState(profile = it)
                },
                onFailure = {
                    _uiState.value = ProfileUiState(error = it.userFriendlyMessage("Failed to load profile"))
                }
            )
        }
    }

    fun updateName(name: String) {
        viewModelScope.launch {
            _uiState.value = uiState.value.copy(isLoading = true)
            profileRepository.updateName(name).fold(
                onSuccess = { updated ->
                    _uiState.value = uiState.value.copy(
                        profile = updated,
                        isLoading = false,
                        updateMessage = "Your account details are now up to date."
                    )
                },
                onFailure = {
                    _uiState.value = uiState.value.copy(
                        isLoading = false,
                        error = it.userFriendlyMessage("Failed to update profile")
                    )
                }
            )
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = uiState.value.copy(isSignedOut = true)
        }
    }

    fun clearUpdateMessage() {
        _uiState.value = uiState.value.copy(updateMessage = null)
    }

    class Factory(
        private val authRepo: AuthRepository,
        private val profileRepo: ProfileRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = ProfileViewModel(authRepo, profileRepo) as T
    }
}