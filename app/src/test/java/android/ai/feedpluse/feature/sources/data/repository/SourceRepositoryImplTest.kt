package android.ai.feedpluse.feature.sources.data.repository

import android.ai.feedpluse.core.util.Resource
import android.ai.feedpluse.feature.sources.data.model.SourceApi
import android.ai.feedpluse.feature.sources.data.model.SourceDto
import android.ai.feedpluse.feature.sources.data.model.SourceResponse
import android.ai.feedpluse.feature.sources.domain.model.SourceData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class SourceRepositoryImplTest {

    @Test
    fun `getSources returns Resource Success with mapped SourceData`() = runTest {
        val fakeApi = object : SourceApi {
            override suspend fun getSources(apiKey: String): SourceResponse {
                return SourceResponse(
                    status = "ok",
                    sources = listOf(
                        SourceDto(
                            id = "abc-news",
                            name = "ABC News",
                            description = "Breaking news",
                            url = "https://abcnews.go.com",
                            category = "general",
                            language = "en",
                            country = "us"
                        ),
                        SourceDto(
                            id = null,
                            name = null,
                            description = null,
                            url = null,
                            category = null,
                            language = null,
                            country = null
                        )
                    )
                )
            }
        }
        val repository = SourceRepositoryImpl(fakeApi)

        val result = repository.getSources()

        assertTrue(result is Resource.Success)
        @Suppress("UNCHECKED_CAST")
        val sources = (result as Resource.Success).data as List<SourceData>
        assertEquals(2, sources.size)
        assertEquals("abc-news", sources[0].id)
        assertEquals("ABC News", sources[0].name)
        assertEquals("Breaking news", sources[0].description)
        assertEquals("Unknown Source", sources[1].name)
    }

    @Test
    fun `getSources returns Resource Error on IOException`() = runTest {
        val fakeApi = object : SourceApi {
            override suspend fun getSources(apiKey: String): SourceResponse {
                throw IOException("Timeout")
            }
        }
        val repository = SourceRepositoryImpl(fakeApi)

        val result = repository.getSources()

        assertTrue(result is Resource.Error)
        assertEquals(
            "No internet connection. Please check your network and try again.",
            (result as Resource.Error).message
        )
    }
}
