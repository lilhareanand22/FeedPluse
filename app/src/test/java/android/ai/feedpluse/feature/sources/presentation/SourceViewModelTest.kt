package android.ai.feedpluse.feature.sources.presentation

import android.ai.feedpluse.core.util.Resource
import android.ai.feedpluse.feature.sources.domain.model.SourceData
import android.ai.feedpluse.feature.sources.domain.repository.SourceRepository
import android.ai.feedpluse.feature.sources.domain.usecase.GetSourcesUseCase
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
class SourceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    @Test
    fun `init fetches sources and emits Loading then Success`() = runTest {
        val sampleSources = listOf(
            SourceData(
                id = "abc-news",
                name = "ABC News",
                description = "Trusted breaking news"
            )
        )
        val fakeRepo = object : SourceRepository {
            override suspend fun getSources(): Resource = Resource.Success(sampleSources)
        }
        val viewModel = SourceViewModel(GetSourcesUseCase(fakeRepo))

        assertTrue(viewModel.uiState.value is Resource.Loading)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is Resource.Success)
        assertEquals(sampleSources, (state as Resource.Success).data)
    }

    @Test
    fun `fetchSources emits Error on failure and recovers on retry`() = runTest {
        var shouldFail = true
        val fakeRepo = object : SourceRepository {
            override suspend fun getSources(): Resource {
                return if (shouldFail) {
                    Resource.Error("Server error")
                } else {
                    Resource.Success(emptyList<SourceData>())
                }
            }
        }
        val viewModel = SourceViewModel(GetSourcesUseCase(fakeRepo))

        advanceUntilIdle()
        val errorState = viewModel.uiState.value
        assertTrue(errorState is Resource.Error)
        assertEquals("Server error", (errorState as Resource.Error).message)

        shouldFail = false
        viewModel.fetchSources()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is Resource.Success)
    }
}
