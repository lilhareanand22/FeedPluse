package android.ai.feedpluse.feature.articles.domain.usecase

import android.ai.feedpluse.core.util.Resource
import android.ai.feedpluse.feature.articles.domain.repository.ArticleRepository
import javax.inject.Inject

/**
 * Use case for retrieving the list of top-headline articles.
 */
class GetArticlesUseCase @Inject constructor(
    private val repository: ArticleRepository
) {
    suspend operator fun invoke(): Resource {
        return repository.getArticles()
    }
}
