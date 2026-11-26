package android.ai.feedpluse.feature.sources.data.model

/**
 * Raw data transfer object representing a news source returned by NewsAPI.
 */
data class SourceDto(
    val id: String?,
    val name: String?,
    val description: String?,
    val url: String?,
    val category: String?,
    val language: String?,
    val country: String?
)
