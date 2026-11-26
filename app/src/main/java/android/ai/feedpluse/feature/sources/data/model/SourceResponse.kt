package android.ai.feedpluse.feature.sources.data.model

/**
 * Top-level API response wrapper for the top-headlines/sources endpoint.
 */
data class SourceResponse(
    val status: String,
    val sources: List<SourceDto>
)
