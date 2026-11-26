package android.ai.feedpluse.feature.sources.data.model

import android.ai.feedpluse.feature.sources.domain.model.SourceData

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

/**
 * Maps a raw [SourceDto] from the network layer to a domain [SourceData].
 */
fun SourceDto.toSourceData(): SourceData = SourceData(
    id = id.orEmpty(),
    name = name ?: "Unknown Source",
    description = description.orEmpty()
)
