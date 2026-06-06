package com.eggheadengineers.nimons360.feature.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.eggheadengineers.nimons360.NimonsApplication
import com.eggheadengineers.nimons360.core.notifications.NotificationTokenSync
import com.eggheadengineers.nimons360.core.presence.PresenceLocationService
import com.eggheadengineers.nimons360.core.preferences.UserPreferenceStore
import com.eggheadengineers.nimons360.core.session.SessionManager
import com.eggheadengineers.nimons360.core.validation.validatePersonName
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
    val notificationsEnabled: Boolean = true,
    val locationSharingEnabled: Boolean = true,
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val userPreferenceStore: UserPreferenceStore,
    private val notificationTokenSync: NotificationTokenSync,
    private val sessionManager: SessionManager,
    private val appContext: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        _uiState.value = _uiState.value.copy(
            notificationsEnabled = userPreferenceStore.isNotificationEnabled(),
            locationSharingEnabled = userPreferenceStore.isLocationSharingEnabled(),
        )
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = uiState.value.copy(isLoading = true, error = null)
            profileRepository.getProfile().fold(
                onSuccess = { serverProfile ->
                    sessionManager.saveUserProfileImageUrl(serverProfile.profileImageUrl)
                    val uploadedUrl = sessionManager.getUploadedProfileImageUrl()
                    val profile = if (!uploadedUrl.isNullOrBlank()) {
                        serverProfile.copy(profileImageUrl = uploadedUrl)
                    } else {
                        serverProfile
                    }
                    _uiState.value = uiState.value.copy(profile = profile, isLoading = false)
                },
                onFailure = {
                    _uiState.value = uiState.value.copy(
                        isLoading = false,
                        error = it.userFriendlyMessage("Failed to load profile"),
                    )
                }
            )
        }
    }

    fun updateName(name: String) {
        val validationError = validatePersonName(name)
        if (validationError != null) {
            _uiState.value = uiState.value.copy(error = validationError)
            return
        }
        viewModelScope.launch {
            _uiState.value = uiState.value.copy(isLoading = true, error = null)
            profileRepository.updateName(name).fold(
                onSuccess = { updated ->
                    sessionManager.saveUserName(updated.name)
                    sessionManager.saveUserProfileImageUrl(updated.profileImageUrl)
                    (appContext as? NimonsApplication)?.familyRepository?.notifyMemberChanged()
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

    fun uploadPhoto(fileName: String, bytes: ByteArray, mediaType: String) {
        viewModelScope.launch {
            _uiState.value = uiState.value.copy(isLoading = true, error = null)
            profileRepository.uploadPhoto(fileName, bytes, mediaType).fold(
                onSuccess = { updated ->
                    val versionedUrl = updated.profileImageUrl?.let { "$it?v=${System.currentTimeMillis()}" }
                    sessionManager.saveUserProfileImageUrl(updated.profileImageUrl)
                    sessionManager.saveUploadedProfileImageUrl(versionedUrl)
                    _uiState.value = uiState.value.copy(
                        profile = updated.copy(profileImageUrl = versionedUrl ?: updated.profileImageUrl),
                        isLoading = false,
                        updateMessage = "Your profile photo has been updated.",
                    )
                },
                onFailure = {
                    _uiState.value = uiState.value.copy(
                        isLoading = false,
                        error = it.userFriendlyMessage("Failed to upload profile photo"),
                    )
                },
            )
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _uiState.value = uiState.value.copy(
            notificationsEnabled = enabled,
            updateMessage = if (enabled) {
                "Notifications are enabled on this device."
            } else {
                "Notifications are disabled on this device."
            },
        )
        viewModelScope.launch {
            runCatching { notificationTokenSync.syncPreference(enabled) }.onFailure {
                _uiState.value = uiState.value.copy(
                    error = it.userFriendlyMessage("Failed to sync notification preference"),
                )
            }
        }
    }

    fun setLocationSharingEnabled(enabled: Boolean) {
        userPreferenceStore.setLocationSharingEnabled(enabled)
        if (enabled) {
            PresenceLocationService.start(appContext)
        } else {
            PresenceLocationService.stop(appContext)
        }
        _uiState.value = uiState.value.copy(
            locationSharingEnabled = enabled,
            updateMessage = if (enabled) {
                "Location sharing is enabled."
            } else {
                "Location sharing is disabled."
            },
        )
    }

    fun showError(message: String) {
        _uiState.value = uiState.value.copy(error = message)
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

    fun clearError() {
        _uiState.value = uiState.value.copy(error = null)
    }

    class Factory(
        private val authRepo: AuthRepository,
        private val profileRepo: ProfileRepository,
        private val userPreferenceStore: UserPreferenceStore,
        private val notificationTokenSync: NotificationTokenSync,
        private val sessionManager: SessionManager,
        private val appContext: Context,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            ProfileViewModel(
                authRepo,
                profileRepo,
                userPreferenceStore,
                notificationTokenSync,
                sessionManager,
                appContext.applicationContext,
            ) as T
    }
}
