package android.ai.feedpluse.feature.sources.presentation

import android.ai.feedpluse.core.util.Resource
import android.ai.feedpluse.feature.sources.domain.model.SourceData
import android.ai.feedpluse.feature.sources.domain.repository.SourceRepository
import android.ai.feedpluse.feature.sources.domain.usecase.GetSourcesUseCase
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
class SourceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val sampleSources = listOf(
        SourceData(
            id = "abc-news",
            name = "ABC News",
            description = "Trusted breaking news"
        )
    )

    @Test
    fun `init dispatches LoadSources and updates UiState to Success`() = runTest {
        val fakeRepo = object : SourceRepository {
            override suspend fun getSources(): Resource = Resource.Success(sampleSources)
        }
        val viewModel = SourceViewModel(GetSourcesUseCase(fakeRepo))

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
        assertNull(state.errorMessage)
        assertEquals(sampleSources, state.sources)
    }

    @Test
    fun `RetrySources recovers from errorMessage to sources list`() = runTest {
        var shouldFail = true
        val fakeRepo = object : SourceRepository {
            override suspend fun getSources(): Resource {
                return if (shouldFail) {
                    Resource.Error("Server error")
                } else {
                    Resource.Success(sampleSources)
                }
            }
        }
        val viewModel = SourceViewModel(GetSourcesUseCase(fakeRepo))

        advanceUntilIdle()
        assertEquals("Server error", viewModel.uiState.value.errorMessage)

        shouldFail = false
        viewModel.onIntent(SourceIntent.RetrySources)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
        assertEquals(sampleSources, state.sources)
    }

    @Test
    fun `RefreshSources failure keeps cached sources and emits ShowError effect`() = runTest {
        var shouldFail = false
        val fakeRepo = object : SourceRepository {
            override suspend fun getSources(): Resource {
                return if (shouldFail) {
                    Resource.Error("Refresh failed")
                } else {
                    Resource.Success(sampleSources)
                }
            }
        }
        val viewModel = SourceViewModel(GetSourcesUseCase(fakeRepo))
        advanceUntilIdle()

        shouldFail = true
        viewModel.onIntent(SourceIntent.RefreshSources)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertNull(state.errorMessage)
        assertEquals(sampleSources, state.sources)

        val effect = viewModel.effect.first()
        assertTrue(effect is SourceEffect.ShowError)
        assertEquals("Refresh failed", (effect as SourceEffect.ShowError).message)
    }
}
