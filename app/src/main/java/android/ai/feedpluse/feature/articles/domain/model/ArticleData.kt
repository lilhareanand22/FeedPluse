package android.ai.feedpluse.feature.articles.domain.model

/**
 * Clean domain model representing a news article.
 */
data class ArticleData(
    val sourceId: String?,
    val sourceName: String?,
    val author: String?,
    val title: String,
    val description: String?,
    val url: String?,
    val imageUrl: String?,
    val date: String?,
    val content: String?
)
