/*
 * Copyright (c) 2023 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work
 * except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific language
 * governing permissions and limitations under the Licence.
 */

package eu.europa.ec.testrqes

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import eu.europa.ec.rqesui.domain.extension.toUri
import eu.europa.ec.rqesui.infrastructure.EudiRQESUi
import eu.europa.ec.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.rqesui.infrastructure.config.data.QTSPData
import eu.europa.ec.testrqes.ui.theme.EudiRQESUiTheme

class TestRQESActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        var documentUri by remember {
            mutableStateOf<Uri?>(null)
        }

        val selectPdfLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->
            documentUri = uri
        }

        Column {
            Button(onClick = {
                showRQESSDK(context)
            }) {
                Text("Show RQES SDK")
            }

            if (documentUri == null) {
                Button(onClick = {
                    selectPdfLauncher.launch(
                        arrayOf("application/pdf")
                    )
                }) {
                    Text(text = "Select PDF document")
                }
            } else {
                Button(
                    onClick = {
                        showRQESSuccessScreen(
                            context = context,
                            documentUri = documentUri
                        )
                    }
                ) {
                    Text("Go to Success screen")
                }
            }

            Button(
                onClick = {
                    documentUri = null
                },
                enabled = documentUri != null
            ) {
                Text(text = "Clear selected PDF")
            }
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
    EudiRQESUi.launchSdk(
        context = context,
        state = EudiRQESUi.State.Initial(
            file = DocumentData(
                documentName = "Document_name.pdf",
                uri = "https://www.example.com".toUri()
            ),
            qtsps = listOf(
                QTSPData(qtspName = "QTSP1 Example", uri = "https://qtsp1.com".toUri()),
                QTSPData(qtspName = "QTSP2 Example", uri = "https://qtsp2.com".toUri()),
                QTSPData(qtspName = "QTSP3 Example", uri = "https://qtsp3.com".toUri()),
            )
        )
    )
}

private fun showRQESSuccessScreen(
    context: Context,
    documentUri: Uri?
) {
    EudiRQESUi.launchSdk(
        context = context,
        state = EudiRQESUi.State.Success(
            file = DocumentData(
                documentName = "Document.pdf",
                uri = documentUri ?: "https://www.example.com".toUri()
            ),
        ),
        documentName = "Document.pdf",
        documentUri = documentUri
    )
}