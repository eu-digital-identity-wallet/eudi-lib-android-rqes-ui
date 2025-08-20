/*
 * Copyright (c) 2025 European Commission
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.europa.ec.eudi.testrqes.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import eu.europa.ec.eudi.rqesui.domain.extension.toUriOrEmpty
import eu.europa.ec.eudi.rqesui.infrastructure.DocumentUri
import eu.europa.ec.eudi.rqesui.infrastructure.EudiRQESUi
import eu.europa.ec.eudi.rqesui.infrastructure.RemoteUri
import eu.europa.ec.eudi.testrqes.theme.EudiRQESUiTheme

class TestRQESActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EudiRQESUiTheme {
                checkIntent(intent)
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Content(innerPadding)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        checkIntent(intent)
    }

    private fun checkIntent(intent: Intent) {
        val code = intent.data?.getQueryParameter("code")
        if (code != null) {
            EudiRQESUi.resume(
                context = this,
                authorizationCode = code,
            )
        }
    }
}

@Composable
private fun Content(padding: PaddingValues) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        var documentUri by remember {
            mutableStateOf<Uri?>(null)
        }

        var remoteUri by remember {
            mutableStateOf("")
        }

        val selectPdfLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->
            documentUri = uri
        }

        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = "Local File Flow",
                style = LocalTextStyle.current.copy(
                    fontSize = LocalTextStyle.current.fontSize * 1.5f,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(5.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                documentUri?.let {
                    Button(
                        onClick = {
                            startSdk(
                                context = context,
                                documentUri = it
                            )
                        }
                    ) {
                        Text("Start SDK")
                    }
                } ?: run {
                    Button(
                        onClick = {
                            selectPdfLauncher.launch(
                                arrayOf("application/pdf")
                            )
                        }
                    ) {
                        Text(text = "Select PDF document")
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

            HorizontalDivider(
                thickness = 2.dp,
                modifier = Modifier.padding(vertical = 24.dp)
            )

            Text(
                text = "Document Retrieval Flow",
                style = LocalTextStyle.current.copy(
                    fontSize = LocalTextStyle.current.fontSize * 1.5f,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(5.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = remoteUri,
                onValueChange = { remoteUri = it },
                label = { Text("Remote URL") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Uri
                )
            )

            Spacer(modifier = Modifier.height(5.dp))

            Button(
                onClick = {
                    startSdk(
                        context = context,
                        remoteUri = remoteUri
                    )
                },
                enabled = remoteUri.isNotEmpty()
            ) {
                Text("Start SDK")
            }
        }
    }
}

private fun startSdk(
    context: Context,
    documentUri: Uri? = null,
    remoteUri: String? = null
) {
    if (documentUri != null) {
        EudiRQESUi.initiate(
            context = context,
            documentUri = DocumentUri(documentUri),
        )
    } else if (remoteUri != null) {
        EudiRQESUi.initiate(
            context = context,
            remoteUri = RemoteUri(remoteUri.toUriOrEmpty()),
        )
    }
}