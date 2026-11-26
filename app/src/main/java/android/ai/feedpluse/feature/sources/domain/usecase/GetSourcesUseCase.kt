package android.ai.feedpluse.feature.sources.domain.usecase

import android.ai.feedpluse.core.util.Resource
import android.ai.feedpluse.feature.sources.domain.repository.SourceRepository
import javax.inject.Inject

/**
 * Use case for retrieving the list of news sources.
 */
class GetSourcesUseCase @Inject constructor(
    private val repository: SourceRepository
) {
    suspend operator fun invoke(): Resource {
        return repository.getSources()
    }
}
