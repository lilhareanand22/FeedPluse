package android.ai.feedpluse

import android.ai.feedpluse.feature.articles.presentation.ArticleScreen
import android.ai.feedpluse.ui.theme.FeedPluseTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FeedPluseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ArticleScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}