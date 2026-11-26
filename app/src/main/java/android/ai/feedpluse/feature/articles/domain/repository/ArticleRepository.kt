package android.ai.feedpluse.feature.articles.domain.repository

import android.ai.feedpluse.core.util.Resource

/**
 * Repository contract for fetching news articles.
 */
interface ArticleRepository {
    suspend fun getArticles(): Resource
}
