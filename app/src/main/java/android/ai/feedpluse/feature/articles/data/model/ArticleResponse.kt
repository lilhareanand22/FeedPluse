package android.ai.feedpluse.feature.articles.data.model

/**
 * Top-level API response wrapper for the top-headlines endpoint.
 */
data class ArticleResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<ArticleDto>
)
