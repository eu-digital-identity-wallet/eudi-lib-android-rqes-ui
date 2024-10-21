package eu.europa.ec.rqesui.presentation.ui.container

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import eu.europa.ec.rqesui.infrastructure.theme.EudiRQESUiTheme
import org.koin.androidx.compose.KoinAndroidContext
import org.koin.core.annotation.KoinExperimentalAPI

class EudiRQESContainer : ComponentActivity() {

    @OptIn(KoinExperimentalAPI::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EudiRQESUiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    KoinAndroidContext {
                        Content(innerPadding)
                    }
                }
            }
        }
    }

    @Composable
    private fun Content(padding: PaddingValues) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "EudiRQES SDK ENVIRONMENT")
        }
    }
}