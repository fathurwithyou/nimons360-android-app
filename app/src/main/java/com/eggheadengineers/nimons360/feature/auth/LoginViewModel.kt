package com.eggheadengineers.nimons360.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.eggheadengineers.nimons360.domain.repository.AuthRepository
import com.eggheadengineers.nimons360.data.network.userFriendlyMessage

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(email: String, password: String) {
        if (email.isBlank()) {
            _uiState.value = LoginUiState.Error("Enter your email")
            return
        }
        if (password.isBlank()) {
            _uiState.value = LoginUiState.Error("Enter your password")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            authRepository.login(email.trim(), password.trim()).fold(
                onSuccess = {
                    _uiState.value = LoginUiState.Success
                },
                onFailure = {
                    _uiState.value = LoginUiState.Error(it.userFriendlyMessage("Login failed"))
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }

    class Factory(private val repo: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = LoginViewModel(repo) as T
    }
}
