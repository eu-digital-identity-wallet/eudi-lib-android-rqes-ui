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

package eu.europa.ec.eudi.rqesui.presentation.navigation

internal interface NavigatableItem

internal open class Screen(name: String, parameters: String = "") : NavigatableItem {
    val screenRoute: String = name + parameters
    val screenName = name
}

internal sealed class SdkScreens {
    data object Success : Screen(
        name = "SUCCESS"
    )

    data object OptionsSelection : Screen(
        name = "OPTIONS_SELECTION",
        parameters = "?optionsSelectionConfig={optionsSelectionConfig}"
    )

    data object ViewDocument : Screen(
        name = "VIEW_DOCUMENT",
        parameters = "?viewDocumentConfig={viewDocumentConfig}"
    )
}