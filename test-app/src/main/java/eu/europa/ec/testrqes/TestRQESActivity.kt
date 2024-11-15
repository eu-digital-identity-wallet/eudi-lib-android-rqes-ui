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
import android.content.Intent
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import eu.europa.ec.rqesui.infrastructure.EudiRQESUi
import eu.europa.ec.testrqes.ui.theme.EudiRQESUiTheme

class TestRQESActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EudiRQESUiTheme {
                checkIntent(intent)
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Content(innerPadding)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        println("New intent: ${intent.data}")

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
            documentUri?.let {
                var sdkHasStarted by rememberSaveable {
                    mutableStateOf(false)
                }

                if (sdkHasStarted) {
                    Button(
                        onClick = {
                            resumeSdk(
                                context = context,
                            )
                        }
                    ) {
                        Text("Resume SDK")
                    }
                } else {
                    Button(
                        onClick = {
                            startSdk(
                                context = context,
                                documentUri = it
                            )
                            sdkHasStarted = true
                        }
                    ) {
                        Text("Start SDK")
                    }
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
    }
}

private fun startSdk(
    context: Context,
    documentUri: Uri
) {
    EudiRQESUi.initiate(
        context = context,
        documentUri = documentUri,
    )
}

private fun resumeSdk(
    context: Context,
) {
    EudiRQESUi.resume(
        context = context,
        authorizationCode = "", //TODO("Add authorization code")
    )
}