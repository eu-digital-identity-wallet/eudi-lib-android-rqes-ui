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

package eu.europa.ec.rqesui.presentation.navigation

internal interface NavigatableItem

internal open class Screen(name: String, parameters: String = "") : NavigatableItem {
    val screenRoute: String = name + parameters
    val screenName = name
}

internal sealed class SdkScreens {
    data object SelectQtsp : Screen(
        name = "SELECT_QTSP"
    )

    data object SelectCertificate : Screen(
        name = "SELECT_CERTIFICATE"
    )

    data object Success : Screen(
        name = "SUCCESS"
    )

    data object ViewDocument : Screen(
        name = "VIEW_DOCUMENT",
        parameters = "?viewDocumentConfig={viewDocumentConfig}"
    )
}

//TODO should probably delete this
internal sealed class ModuleRoute(val route: String) : NavigatableItem {
    data object SdkModule : ModuleRoute("SDK_MODULE")
}