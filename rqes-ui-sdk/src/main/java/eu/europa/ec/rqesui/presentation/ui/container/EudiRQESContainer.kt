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

package eu.europa.ec.rqesui.presentation.ui.container

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import eu.europa.ec.rqesui.infrastructure.EudiRQESUi
import eu.europa.ec.rqesui.presentation.navigation.RouterHost
import eu.europa.ec.rqesui.presentation.navigation.SdkScreens
import eu.europa.ec.rqesui.presentation.router.sdkGraph
import eu.europa.ec.rqesui.presentation.utils.Constants.STEP_KEY
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.KoinAndroidContext
import org.koin.core.annotation.KoinExperimentalAPI

internal class EudiRQESContainer : ComponentActivity() {

    private val routerHost: RouterHost by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Content(intent) {
                sdkGraph(it)
            }
        }
    }

    @OptIn(KoinExperimentalAPI::class)
    @Composable
    private fun Content(
        intent: Intent,
        builder: NavGraphBuilder.(NavController) -> Unit
    ) {
        EudiRQESUi.getEudiRQESUiConfig().themeManager.Theme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                KoinAndroidContext {
                    val startingRoute = getStartingRoute(intent)
                    routerHost.StartFlow(startDestination = startingRoute) {
                        builder(it)
                    }
                }
            }
        }
    }

    //TODO should we handle onNewIntent?

    private fun getStartingRoute(intent: Intent): String {
        val step = intent.getParcelableExtra<EudiRQESUi.SignDocumentStep>(STEP_KEY)
        return when (step) {
            is EudiRQESUi.SignDocumentStep.Start -> SdkScreens.SelectQtsp.screenRoute
            is EudiRQESUi.SignDocumentStep.SelectCertificate -> SdkScreens.SelectCertificate.screenRoute
            is EudiRQESUi.SignDocumentStep.Completed -> SdkScreens.Success.screenRoute
            null -> throw IllegalStateException("EUDIRQESUI-SDK: Missing step")
        }
    }
}