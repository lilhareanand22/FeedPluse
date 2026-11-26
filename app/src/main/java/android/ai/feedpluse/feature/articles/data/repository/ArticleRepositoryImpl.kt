package android.ai.feedpluse.feature.articles.data.repository

import android.ai.feedpluse.core.util.Resource
import android.ai.feedpluse.feature.articles.data.model.ArticleApi
import android.ai.feedpluse.feature.articles.data.model.toArticleData
import android.ai.feedpluse.feature.articles.domain.repository.ArticleRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

/**
 * Concrete implementation of [ArticleRepository] backed by [ArticleApi].
 */
class ArticleRepositoryImpl @Inject constructor(
    private val articleApi: ArticleApi
) : ArticleRepository {

    override suspend fun getArticles(): Resource {
        return try {
            val response = articleApi.getTopHeadlines()
            Resource.Success(response.articles.map { it.toArticleData() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }
}
