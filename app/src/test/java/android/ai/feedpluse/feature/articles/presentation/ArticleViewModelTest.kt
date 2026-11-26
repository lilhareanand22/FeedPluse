package android.ai.feedpluse.feature.articles.presentation

import android.ai.feedpluse.core.util.Resource
import android.ai.feedpluse.feature.articles.domain.model.ArticleData
import android.ai.feedpluse.feature.articles.domain.repository.ArticleRepository
import android.ai.feedpluse.feature.articles.domain.usecase.GetArticlesUseCase
import android.ai.feedpluse.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArticleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val sampleArticles = listOf(
        ArticleData(
            sourceId = "cnn",
            sourceName = "CNN",
            author = null,
            title = "Sample Headline",
            description = "Sample description",
            url = "https://cnn.com/1",
            imageUrl = null,
            date = "2026-11-26T01:00:00Z",
            content = null
        )
    )

    @Test
    fun `init dispatches LoadArticles and updates UiState to Success`() = runTest {
        val fakeRepo = object : ArticleRepository {
            override suspend fun getArticles(): Resource = Resource.Success(sampleArticles)
        }
        val viewModel = ArticleViewModel(GetArticlesUseCase(fakeRepo))

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
        assertNull(state.errorMessage)
        assertEquals(sampleArticles, state.articles)
    }

    @Test
    fun `RetryArticles recovers from errorMessage to articles list`() = runTest {
        var shouldFail = true
        val fakeRepo = object : ArticleRepository {
            override suspend fun getArticles(): Resource {
                return if (shouldFail) {
                    Resource.Error("Network error")
                } else {
                    Resource.Success(sampleArticles)
                }
            }
        }
        val viewModel = ArticleViewModel(GetArticlesUseCase(fakeRepo))

        advanceUntilIdle()
        assertEquals("Network error", viewModel.uiState.value.errorMessage)

        shouldFail = false
        viewModel.onIntent(ArticleIntent.RetryArticles)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
        assertEquals(sampleArticles, state.articles)
    }

    @Test
    fun `RefreshArticles failure keeps cached articles and emits ShowError effect`() = runTest {
        var shouldFail = false
        val fakeRepo = object : ArticleRepository {
            override suspend fun getArticles(): Resource {
                return if (shouldFail) {
                    Resource.Error("Refresh failed")
                } else {
                    Resource.Success(sampleArticles)
                }
            }
        }
        val viewModel = ArticleViewModel(GetArticlesUseCase(fakeRepo))
        advanceUntilIdle()

        shouldFail = true
        viewModel.onIntent(ArticleIntent.RefreshArticles)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertNull(state.errorMessage)
        assertEquals(sampleArticles, state.articles)

        val effect = viewModel.effect.first()
        assertTrue(effect is ArticleEffect.ShowError)
        assertEquals("Refresh failed", (effect as ArticleEffect.ShowError).message)
    }

    @Test
    fun `ArticleClicked emits NavigateToDetail effect`() = runTest {
        val fakeRepo = object : ArticleRepository {
            override suspend fun getArticles(): Resource = Resource.Success(sampleArticles)
        }
        val viewModel = ArticleViewModel(GetArticlesUseCase(fakeRepo))
        advanceUntilIdle()

        val clicked = sampleArticles.first()
        viewModel.onIntent(ArticleIntent.ArticleClicked(clicked))

        val effect = viewModel.effect.first()
        assertTrue(effect is ArticleEffect.NavigateToDetail)
        assertEquals(clicked, (effect as ArticleEffect.NavigateToDetail).article)
    }
}
