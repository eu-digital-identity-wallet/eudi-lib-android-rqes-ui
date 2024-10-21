package eu.europa.ec.testrqes

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import eu.europa.ec.rqesui.infrastructure.EudiRQESUi
import eu.europa.ec.testrqes.ui.theme.EudiRQESUiTheme
import java.net.URI

class TestRQESActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EudiRQESUiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Content(innerPadding)
                }
            }
        }
    }
}

@Composable
private fun Content(padding: PaddingValues) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = { showRQESSDK(context) }) {
            Text("Show RQES SDK")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ContentPreview() {
    EudiRQESUiTheme {
        Content(PaddingValues())
    }
}

private fun showRQESSDK(context: Context) {
    EudiRQESUi.initiate(context, URI.create("https://www.netcompany.com"))
}