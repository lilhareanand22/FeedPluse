package android.ai.feedpluse.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Source
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Top-level destinations for the bottom navigation bar.
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Articles : Screen(
        route = "articles",
        title = "Articles",
        icon = Icons.AutoMirrored.Filled.Article
    )

    data object Sources : Screen(
        route = "sources",
        title = "Sources",
        icon = Icons.Default.Source
    )

    companion object {
        val bottomNavItems = listOf(Articles, Sources)
    }
}
