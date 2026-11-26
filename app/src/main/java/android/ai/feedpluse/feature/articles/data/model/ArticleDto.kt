package android.ai.feedpluse.feature.articles.data.model

import android.ai.feedpluse.feature.articles.domain.model.ArticleData
import com.google.gson.annotations.SerializedName

/**
 * Raw data transfer object representing an article returned by NewsAPI.
 */
data class ArticleDto(
    val source: SourceDto?,
    val author: String?,
    val title: String?,
    val description: String?,
    val url: String?,
    @SerializedName("urlToImage") val imageUrl: String?,
    @SerializedName("publishedAt") val date: String?,
    val content: String?
) {
    data class SourceDto(
        val id: String?,
        val name: String?
    )
}

/**
 * Maps a raw [ArticleDto] from the network layer to a domain [ArticleData].
 */
fun ArticleDto.toArticleData(): ArticleData = ArticleData(
    sourceId = source?.id,
    sourceName = source?.name,
    author = author,
    title = title ?: "No Title",
    description = description,
    url = url,
    imageUrl = imageUrl,
    date = date,
    content = content
)
