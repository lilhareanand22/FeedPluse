package android.ai.feedpluse.feature.articles.presentation

import android.ai.feedpluse.core.util.Resource
import android.ai.feedpluse.feature.articles.domain.usecase.GetArticlesUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel managing the UI state for the Articles screen.
 */
@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val getArticlesUseCase: GetArticlesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<Resource>(Resource.Loading)
    val uiState: StateFlow<Resource> = _uiState.asStateFlow()

    init {
        fetchArticles()
    }

    fun fetchArticles() {
        viewModelScope.launch {
            _uiState.value = Resource.Loading
            _uiState.value = getArticlesUseCase()
        }
    }
}
