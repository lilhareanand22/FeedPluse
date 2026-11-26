package android.ai.feedpluse.feature.sources.presentation

import android.ai.feedpluse.core.util.Resource
import android.ai.feedpluse.feature.sources.domain.model.SourceData
import android.ai.feedpluse.feature.sources.domain.usecase.GetSourcesUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MVI ViewModel managing [SourceUiState], [SourceIntent], and [SourceEffect] for the Sources screen.
 */
@HiltViewModel
class SourceViewModel @Inject constructor(
    private val getSourcesUseCase: GetSourcesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SourceUiState())
    val uiState: StateFlow<SourceUiState> = _uiState.asStateFlow()

    private val _effect = Channel<SourceEffect>(Channel.BUFFERED)
    val effect: Flow<SourceEffect> = _effect.receiveAsFlow()

    init {
        onIntent(SourceIntent.LoadSources)
    }

    fun onIntent(intent: SourceIntent) {
        when (intent) {
            SourceIntent.LoadSources,
            SourceIntent.RetrySources -> loadSources(isRefresh = false)
            SourceIntent.RefreshSources -> loadSources(isRefresh = true)
        }
    }

    private fun loadSources(isRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !isRefresh,
                    isRefreshing = isRefresh,
                    errorMessage = null
                )
            }

            when (val result = getSourcesUseCase()) {
                is Resource.Success -> {
                    @Suppress("UNCHECKED_CAST")
                    val sources = (result.data as? List<SourceData>).orEmpty()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            sources = sources,
                            errorMessage = null
                        )
                    }
                }

                is Resource.Error -> {
                    val hasCachedSources = _uiState.value.sources.isNotEmpty()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = if (isRefresh && hasCachedSources) null else result.message
                        )
                    }
                    if (isRefresh && hasCachedSources) {
                        _effect.send(SourceEffect.ShowError(result.message))
                    }
                }

                is Resource.Loading -> Unit
            }
        }
    }
}
