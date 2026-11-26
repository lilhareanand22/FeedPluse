package android.ai.feedpluse.feature.sources.presentation

import android.ai.feedpluse.feature.sources.domain.model.SourceData

/**
 * Immutable UI state for the Sources screen.
 */
data class SourceUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val sources: List<SourceData> = emptyList(),
    val errorMessage: String? = null
)

/**
 * User actions / intents dispatched from the Sources screen.
 */
sealed interface SourceIntent {
    data object LoadSources : SourceIntent
    data object RefreshSources : SourceIntent
    data object RetrySources : SourceIntent
}

/**
 * One-time side effects emitted by [SourceViewModel].
 */
sealed interface SourceEffect {
    data class ShowError(val message: String) : SourceEffect
}
