package android.ai.feedpluse.feature.sources.data.model

import android.ai.feedpluse.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API service for fetching news sources.
 */
interface SourceApi {

    @GET("top-headlines/sources")
    suspend fun getSources(
        @Query("apiKey") apiKey: String = BuildConfig.NEWS_API_KEY
    ): SourceResponse
}
