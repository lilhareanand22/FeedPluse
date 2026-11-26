package android.ai.feedpluse.feature.sources.presentation

import android.ai.feedpluse.core.util.Resource
import android.ai.feedpluse.feature.sources.domain.usecase.GetSourcesUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel managing the UI state for the Sources screen.
 */
@HiltViewModel
class SourceViewModel @Inject constructor(
    private val getSourcesUseCase: GetSourcesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<Resource>(Resource.Loading)
    val uiState: StateFlow<Resource> = _uiState.asStateFlow()

    init {
        fetchSources()
    }

    fun fetchSources() {
        viewModelScope.launch {
            _uiState.value = Resource.Loading
            _uiState.value = getSourcesUseCase()
        }
    }
}
