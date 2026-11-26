package android.ai.feedpluse.feature.articles.presentation

import android.ai.feedpluse.core.util.Resource
import android.ai.feedpluse.feature.articles.domain.model.ArticleData
import android.ai.feedpluse.feature.articles.domain.usecase.GetArticlesUseCase
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
 * MVI ViewModel managing [ArticleUiState], [ArticleIntent], and [ArticleEffect] for the Articles screen.
 */
@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val getArticlesUseCase: GetArticlesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleUiState())
    val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ArticleEffect>(Channel.BUFFERED)
    val effect: Flow<ArticleEffect> = _effect.receiveAsFlow()

    init {
        onIntent(ArticleIntent.LoadArticles)
    }

    fun onIntent(intent: ArticleIntent) {
        when (intent) {
            ArticleIntent.LoadArticles,
            ArticleIntent.RetryArticles -> loadArticles(isRefresh = false)
            ArticleIntent.RefreshArticles -> loadArticles(isRefresh = true)
            is ArticleIntent.ArticleClicked -> emitEffect(ArticleEffect.NavigateToDetail(intent.article))
        }
    }

    private fun loadArticles(isRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !isRefresh,
                    isRefreshing = isRefresh,
                    errorMessage = null
                )
            }

            when (val result = getArticlesUseCase()) {
                is Resource.Success -> {
                    @Suppress("UNCHECKED_CAST")
                    val articles = (result.data as? List<ArticleData>).orEmpty()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            articles = articles,
                            errorMessage = null
                        )
                    }
                }

                is Resource.Error -> {
                    val hasCachedArticles = _uiState.value.articles.isNotEmpty()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = if (isRefresh && hasCachedArticles) null else result.message
                        )
                    }
                    if (isRefresh && hasCachedArticles) {
                        emitEffect(ArticleEffect.ShowError(result.message))
                    }
                }

                is Resource.Loading -> Unit
            }
        }
    }

    private fun emitEffect(effect: ArticleEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
