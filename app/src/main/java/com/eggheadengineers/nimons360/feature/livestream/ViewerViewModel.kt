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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class ViewerUiState(
    val stream: LiveStream? = null,
    val isLoading: Boolean = true,
    val isEnded: Boolean = false,
    val error: String? = null,
)

class ViewerViewModel(
    private val streamId: String,
    private val familyId: String,
    private val liveStreamRepository: LiveStreamRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ViewerUiState())
    val state: StateFlow<ViewerUiState> = _state.asStateFlow()

    init {
        load()
        liveStreamRepository.observeFamilyStreams(familyId)
            .onEach { streams ->
                val current = streams.firstOrNull { it.id == streamId }
                if (current != null) {
                    _state.value = _state.value.copy(stream = current, isLoading = false, isEnded = false)
                } else if (_state.value.stream != null) {
                    _state.value = _state.value.copy(
                        stream = null,
                        isLoading = false,
                        isEnded = true,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun load() {
        viewModelScope.launch {
            liveStreamRepository.listStreams(familyId).fold(
                onSuccess = { streams ->
                    val current = streams.firstOrNull { it.id == streamId }
                    _state.value = _state.value.copy(
                        stream = current,
                        isLoading = false,
                        isEnded = current == null,
                        error = null,
                    )
                },
                onFailure = {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = it.userFriendlyMessage("Couldn't load this live stream."),
                    )
                },
            )
        }
    }

    class Factory(
        private val streamId: String,
        private val familyId: String,
        private val repo: LiveStreamRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            ViewerViewModel(streamId, familyId, repo) as T
    }
}
