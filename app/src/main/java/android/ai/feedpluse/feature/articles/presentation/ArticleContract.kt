package android.ai.feedpluse.feature.articles.presentation

import android.ai.feedpluse.feature.articles.domain.model.ArticleData

/**
 * Immutable UI state for the Articles screen.
 */
data class ArticleUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val articles: List<ArticleData> = emptyList(),
    val errorMessage: String? = null
)

/**
 * User actions / intents dispatched from the Articles screen.
 */
sealed interface ArticleIntent {
    data object LoadArticles : ArticleIntent
    data object RefreshArticles : ArticleIntent
    data object RetryArticles : ArticleIntent
    data class ArticleClicked(val article: ArticleData) : ArticleIntent
}

/**
 * One-time side effects emitted by [ArticleViewModel].
 */
sealed interface ArticleEffect {
    data class NavigateToDetail(val article: ArticleData) : ArticleEffect
    data class ShowError(val message: String) : ArticleEffect
}
