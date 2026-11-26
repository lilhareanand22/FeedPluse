package android.ai.feedpluse.navigation

import android.ai.feedpluse.feature.articles.presentation.ArticleScreen
import android.ai.feedpluse.feature.sources.presentation.SourceScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

/**
 * Top-level navigation graph connecting Articles and Sources screens.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Articles.route,
        modifier = modifier
    ) {
        composable(route = Screen.Articles.route) {
            ArticleScreen()
        }

        composable(route = Screen.Sources.route) {
            SourceScreen()
        }
    }
}
