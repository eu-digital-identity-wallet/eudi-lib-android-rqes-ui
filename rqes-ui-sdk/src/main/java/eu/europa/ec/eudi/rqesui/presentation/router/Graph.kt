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

package eu.europa.ec.eudi.rqesui.presentation.router

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import eu.europa.ec.eudi.rqesui.presentation.entities.config.OptionsSelectionUiConfig
import eu.europa.ec.eudi.rqesui.presentation.entities.config.ViewDocumentUiConfig
import eu.europa.ec.eudi.rqesui.presentation.navigation.SdkScreens
import eu.europa.ec.eudi.rqesui.presentation.ui.options_selection.OptionsSelectionScreen
import eu.europa.ec.eudi.rqesui.presentation.ui.success.SuccessScreen
import eu.europa.ec.eudi.rqesui.presentation.ui.view_document.ViewDocumentScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal fun NavGraphBuilder.sdkGraph(navController: NavController) {
    composable(
        route = SdkScreens.Success.screenRoute,
    ) {
        SuccessScreen(
            navController = navController,
            viewModel = koinViewModel()
        )
    }

    composable(
        route = SdkScreens.OptionsSelection.screenRoute,
        arguments = listOf(
            navArgument(OptionsSelectionUiConfig.serializedKeyName) {
                type = NavType.StringType
            }
        )
    ) {
        OptionsSelectionScreen(
            navController = navController,
            viewModel = koinViewModel(
                parameters = {
                    parametersOf(
                        it.arguments?.getString(
                            OptionsSelectionUiConfig.serializedKeyName
                        ).orEmpty()
                    )
                }
            )
        )
    }

    composable(
        route = SdkScreens.ViewDocument.screenRoute,
        arguments = listOf(
            navArgument(ViewDocumentUiConfig.serializedKeyName) {
                type = NavType.StringType
            }
        )
    ) {
        ViewDocumentScreen(
            navController = navController,
            viewModel = koinViewModel(
                parameters = {
                    parametersOf(
                        it.arguments?.getString(
                            ViewDocumentUiConfig.serializedKeyName
                        ).orEmpty()
                    )
                }
            )
        )
    }
}