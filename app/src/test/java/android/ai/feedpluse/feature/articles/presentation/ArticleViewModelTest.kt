package android.ai.feedpluse.feature.articles.presentation

import android.ai.feedpluse.core.util.Resource
import android.ai.feedpluse.feature.articles.domain.model.ArticleData
import android.ai.feedpluse.feature.articles.domain.repository.ArticleRepository
import android.ai.feedpluse.feature.articles.domain.usecase.GetArticlesUseCase
import android.ai.feedpluse.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArticleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    @Test
    fun `init fetches articles and emits Loading then Success`() = runTest {
        val sampleArticles = listOf(
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
        val fakeRepo = object : ArticleRepository {
            override suspend fun getArticles(): Resource = Resource.Success(sampleArticles)
        }
        val viewModel = ArticleViewModel(GetArticlesUseCase(fakeRepo))

        assertTrue(viewModel.uiState.value is Resource.Loading)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is Resource.Success)
        assertEquals(sampleArticles, (state as Resource.Success).data)
    }

    @Test
    fun `fetchArticles emits Error on failure and recovers on retry`() = runTest {
        var shouldFail = true
        val fakeRepo = object : ArticleRepository {
            override suspend fun getArticles(): Resource {
                return if (shouldFail) {
                    Resource.Error("Network error")
                } else {
                    Resource.Success(emptyList<ArticleData>())
                }
            }
        }
        val viewModel = ArticleViewModel(GetArticlesUseCase(fakeRepo))

        advanceUntilIdle()
        val errorState = viewModel.uiState.value
        assertTrue(errorState is Resource.Error)
        assertEquals("Network error", (errorState as Resource.Error).message)

        shouldFail = false
        viewModel.fetchArticles()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is Resource.Success)
    }
}
