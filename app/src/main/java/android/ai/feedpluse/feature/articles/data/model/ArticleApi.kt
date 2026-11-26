package android.ai.feedpluse.feature.articles.data.model

import android.ai.feedpluse.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API service for fetching news articles.
 */
interface ArticleApi {

    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us",
        @Query("apiKey") apiKey: String = BuildConfig.NEWS_API_KEY
    ): ArticleResponse
}
