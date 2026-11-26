package android.ai.feedpluse.feature.articles.data.repository

import android.ai.feedpluse.core.util.Resource
import android.ai.feedpluse.feature.articles.data.model.ArticleApi
import android.ai.feedpluse.feature.articles.data.model.ArticleDto
import android.ai.feedpluse.feature.articles.data.model.ArticleResponse
import android.ai.feedpluse.feature.articles.domain.model.ArticleData
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ArticleRepositoryImplTest {

    @Test
    fun `getArticles returns Resource Success with mapped ArticleData`() = runTest {
        val fakeApi = object : ArticleApi {
            override suspend fun getTopHeadlines(country: String, apiKey: String): ArticleResponse {
                return ArticleResponse(
                    status = "ok",
                    totalResults = 2,
                    articles = listOf(
                        ArticleDto(
                            source = ArticleDto.SourceDto(id = "bbc-news", name = "BBC News"),
                            author = "Reporter",
                            title = "Headline 1",
                            description = "Description 1",
                            url = "https://example.com/1",
                            imageUrl = "https://example.com/1.jpg",
                            date = "2026-11-26T01:00:06Z",
                            content = "Content 1"
                        ),
                        ArticleDto(
                            source = null,
                            author = null,
                            title = null,
                            description = null,
                            url = null,
                            imageUrl = null,
                            date = null,
                            content = null
                        )
                    )
                )
            }
        }
        val repository = ArticleRepositoryImpl(fakeApi)

        val result = repository.getArticles()

        assertTrue(result is Resource.Success)
        @Suppress("UNCHECKED_CAST")
        val articles = (result as Resource.Success).data as List<ArticleData>
        assertEquals(2, articles.size)
        assertEquals("BBC News", articles[0].sourceName)
        assertEquals("https://example.com/1.jpg", articles[0].imageUrl)
        assertEquals("2026-11-26T01:00:06Z", articles[0].date)
        assertEquals("No Title", articles[1].title)
    }

    @Test
    fun `getArticles returns Resource Error on IOException`() = runTest {
        val fakeApi = object : ArticleApi {
            override suspend fun getTopHeadlines(country: String, apiKey: String): ArticleResponse {
                throw IOException("Network down")
            }
        }
        val repository = ArticleRepositoryImpl(fakeApi)

        val result = repository.getArticles()

        assertTrue(result is Resource.Error)
        assertEquals(
            "No internet connection. Please check your network and try again.",
            (result as Resource.Error).message
        )
    }

    @Test
    fun `getArticles returns Resource Error on 401 HttpException`() = runTest {
        val fakeApi = object : ArticleApi {
            override suspend fun getTopHeadlines(country: String, apiKey: String): ArticleResponse {
                val errorBody = "{}".toResponseBody("application/json".toMediaType())
                throw HttpException(Response.error<ArticleResponse>(401, errorBody))
            }
        }
        val repository = ArticleRepositoryImpl(fakeApi)

        val result = repository.getArticles()

        assertTrue(result is Resource.Error)
        assertEquals("Unauthorized. Please check your API key.", (result as Resource.Error).message)
    }
}
