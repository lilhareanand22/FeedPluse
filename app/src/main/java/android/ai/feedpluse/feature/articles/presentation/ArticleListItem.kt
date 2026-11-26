package android.ai.feedpluse.feature.articles.presentation

import android.ai.feedpluse.feature.articles.domain.model.ArticleData
import android.ai.feedpluse.ui.theme.FeedPluseTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * Card item displaying a single news article summary.
 */
@Composable
fun ArticleListItem(
    article: ArticleData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            if (!article.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = article.imageUrl,
                    contentDescription = article.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!article.sourceName.isNullOrBlank() || !article.date.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = article.sourceName.orEmpty(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = article.date.orEmpty().take(10),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!article.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = article.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleListItemPreview() {
    FeedPluseTheme {
        ArticleListItem(
            article = ArticleData(
                sourceId = "bbc-news",
                sourceName = "BBC News",
                author = null,
                title = "US announces new tariffs on host of countries including China",
                description = "The new levies would hit many of America's biggest trading partners, including China and the EU.",
                url = "https://www.bbc.com/news/articles/c893r9ke5p97o",
                imageUrl = null,
                date = "2026-11-26T01:00:06Z",
                content = null
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
