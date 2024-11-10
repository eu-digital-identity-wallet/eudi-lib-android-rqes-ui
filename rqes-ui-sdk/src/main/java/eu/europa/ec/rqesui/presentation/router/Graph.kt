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

package eu.europa.ec.rqesui.presentation.router

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import eu.europa.ec.rqesui.presentation.entities.config.ViewDocumentUiConfig
import eu.europa.ec.rqesui.presentation.navigation.SdkScreens
import eu.europa.ec.rqesui.presentation.ui.select_certificate.SelectCertificateScreen
import eu.europa.ec.rqesui.presentation.ui.select_qtsp.SelectQtspScreen
import eu.europa.ec.rqesui.presentation.ui.success.SuccessScreen
import eu.europa.ec.rqesui.presentation.ui.view_document.ViewDocumentScreen
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal fun NavGraphBuilder.sdkGraph(navController: NavController) {
    composable(
        route = SdkScreens.SelectQtsp.screenRoute,
    ) {
        SelectQtspScreen(
            navController = navController,
            viewModel = koinViewModel()
        )
    }

    composable(
        route = SdkScreens.SelectCertificate.screenRoute,
    ) {
        SelectCertificateScreen(
            navController = navController,
            viewModel = koinViewModel()
        )
    }

    composable(
        route = SdkScreens.Success.screenRoute,
    ) {
        SuccessScreen(
            navController = navController,
            viewModel = koinViewModel()
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
            viewModel = getViewModel(
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