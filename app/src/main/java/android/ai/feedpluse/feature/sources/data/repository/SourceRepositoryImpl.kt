package android.ai.feedpluse.feature.sources.data.repository

import android.ai.feedpluse.core.util.Resource
import android.ai.feedpluse.core.util.safeApiCall
import android.ai.feedpluse.feature.sources.data.model.SourceApi
import android.ai.feedpluse.feature.sources.data.model.toSourceData
import android.ai.feedpluse.feature.sources.domain.repository.SourceRepository
import javax.inject.Inject

/**
 * Concrete implementation of [SourceRepository] backed by [SourceApi].
 */
class SourceRepositoryImpl @Inject constructor(
    private val sourceApi: SourceApi
) : SourceRepository {

    override suspend fun getSources(): Resource = safeApiCall {
        sourceApi.getSources().sources.map { it.toSourceData() }
    }
}
