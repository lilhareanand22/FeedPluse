package android.ai.feedpluse.feature.sources.domain.repository

import android.ai.feedpluse.core.util.Resource

/**
 * Repository contract for fetching news sources.
 */
interface SourceRepository {
    suspend fun getSources(): Resource
}
