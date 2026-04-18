package com.eggheadengineers.nimons360.feature.livestream

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eggheadengineers.nimons360.data.network.userFriendlyMessage
import com.eggheadengineers.nimons360.domain.model.LiveStream
import com.eggheadengineers.nimons360.domain.repository.LiveStreamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BroadcasterUiState(
    val title: String = "",
    val status: BroadcastStatus = BroadcastStatus.Idle,
    val stream: LiveStream? = null,
    val error: String? = null,
    val startedAt: Long? = null,
)

enum class BroadcastStatus { Idle, Preparing, Live, Stopping }

class BroadcasterViewModel(
    private val familyId: String,
    private val liveStreamRepository: LiveStreamRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(BroadcasterUiState())
    val state: StateFlow<BroadcasterUiState> = _state.asStateFlow()

    fun onTitleChange(value: String) {
        if (_state.value.status == BroadcastStatus.Live) return
        _state.value = _state.value.copy(title = value)
    }

    fun requestStart(onReady: (LiveStream) -> Unit) {
        if (_state.value.status != BroadcastStatus.Idle) return
        _state.value = _state.value.copy(status = BroadcastStatus.Preparing, error = null)
        viewModelScope.launch {
            liveStreamRepository.startStream(familyId, _state.value.title).fold(
                onSuccess = { stream ->
                    _state.value = _state.value.copy(
                        status = BroadcastStatus.Live,
                        stream = stream,
                        startedAt = System.currentTimeMillis(),
                    )
                    onReady(stream)
                },
                onFailure = {
                    _state.value = _state.value.copy(
                        status = BroadcastStatus.Idle,
                        error = it.userFriendlyMessage("Couldn't start the stream."),
                    )
                },
            )
        }
    }

    fun requestStop(onStopped: () -> Unit) {
        val stream = _state.value.stream ?: run { onStopped(); return }
        _state.value = _state.value.copy(status = BroadcastStatus.Stopping)
        viewModelScope.launch {
            liveStreamRepository.endStream(stream.id)
            _state.value = BroadcasterUiState()
            onStopped()
        }
    }

    fun reportBroadcasterError(message: String) {
        _state.value = _state.value.copy(error = message)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        val stream = _state.value.stream
        if (stream != null) {
            viewModelScope.launch { liveStreamRepository.endStream(stream.id) }
        }
    }

    class Factory(
        private val familyId: String,
        private val repo: LiveStreamRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            BroadcasterViewModel(familyId, repo) as T
    }
}
