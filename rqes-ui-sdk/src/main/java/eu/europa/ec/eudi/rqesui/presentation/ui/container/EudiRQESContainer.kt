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

package eu.europa.ec.eudi.rqesui.presentation.ui.container

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
import eu.europa.ec.eudi.rqesui.domain.entities.error.EudiRQESUiError
import eu.europa.ec.eudi.rqesui.domain.serializer.UiSerializer
import eu.europa.ec.eudi.rqesui.domain.util.Constants.SDK_STATE
import eu.europa.ec.eudi.rqesui.infrastructure.EudiRQESUi
import eu.europa.ec.eudi.rqesui.presentation.entities.config.OptionsSelectionScreenState
import eu.europa.ec.eudi.rqesui.presentation.entities.config.OptionsSelectionUiConfig
import eu.europa.ec.eudi.rqesui.presentation.navigation.RouterHost
import eu.europa.ec.eudi.rqesui.presentation.navigation.Screen
import eu.europa.ec.eudi.rqesui.presentation.navigation.SdkScreens
import eu.europa.ec.eudi.rqesui.presentation.navigation.helper.generateComposableArguments
import eu.europa.ec.eudi.rqesui.presentation.navigation.helper.generateComposableNavigationLink
import eu.europa.ec.eudi.rqesui.presentation.router.sdkGraph
import org.koin.android.ext.android.inject
import org.koin.core.annotation.KoinExperimentalAPI

internal class EudiRQESContainer : ComponentActivity() {

    private val routerHost: RouterHost by inject()
    private val uiSerializer: UiSerializer by inject()

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
                color = MaterialTheme.colorScheme.surface
            ) {
                val startingRoute = getStartingRoute(intent)
                routerHost.StartFlow(startDestination = startingRoute) {
                    builder(it)
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    @Throws(EudiRQESUiError::class)
    private fun getStartingRoute(intent: Intent): String {
        val state = intent.getParcelableExtra<EudiRQESUi.State>(SDK_STATE)

        val (screen, arguments) = buildScreenAndArgumentsFromState(state)

        return generateComposableNavigationLink(
            screen = screen,
            arguments = arguments
        )
    }

    /**
     * Builds the screen and its associated arguments based on the provided state.
     *
     * @param state The current state of the UI, which determines the screen to navigate to and the arguments needed.
     * @return A pair containing the target screen and its arguments as a string.
     */
    private fun buildScreenAndArgumentsFromState(state: EudiRQESUi.State?): Pair<Screen, String> {
        return when (state) {
            is EudiRQESUi.State.None, null -> throw EudiRQESUiError(
                title = "State Error",
                message = "EUDIRQESUI-SDK: Missing state"
            )

            is EudiRQESUi.State.Initial -> SdkScreens.OptionsSelection to prepareOptionsSelectionScreenArguments(
                OptionsSelectionScreenState.QtspSelection
            )

            is EudiRQESUi.State.Certificate -> SdkScreens.OptionsSelection to prepareOptionsSelectionScreenArguments(
                OptionsSelectionScreenState.CertificateSelection
            )

            is EudiRQESUi.State.Success -> SdkScreens.Success to ""
        }
    }

    /**
     * Prepares serialized arguments for the Options Selection screen.
     *
     * @param optionsSelectionState The specific state for the Options Selection screen (e.g., QtspSelection or CertificateSelection).
     * @return A string containing the serialized arguments for the screen.
     */
    private fun prepareOptionsSelectionScreenArguments(
        optionsSelectionState: OptionsSelectionScreenState
    ): String {
        val screenArguments = generateComposableArguments(
            arguments = mapOf(
                OptionsSelectionUiConfig.serializedKeyName to uiSerializer.toBase64(
                    model = OptionsSelectionUiConfig(
                        optionsSelectionScreenState = optionsSelectionState
                    ),
                    parser = OptionsSelectionUiConfig.Parser
                )
            )
        )
        return screenArguments
    }

}