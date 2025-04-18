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

package eu.europa.ec.eudi.rqesui.presentation.entities

import androidx.compose.ui.graphics.Color
import eu.europa.ec.eudi.rqesui.presentation.architecture.ViewEvent
import eu.europa.ec.eudi.rqesui.presentation.ui.component.IconData

internal data class SelectionOptionUi<T : ViewEvent>(
    val overlineText: String? = null,
    val mainText: String? = null,
    val subtitle: String? = null,
    val actionText: String? = null,
    val leadingIcon: IconData? = null,
    val leadingIconTint: Color? = null,
    val trailingIcon: IconData? = null,
    val trailingIconTint: Color? = null,
    val enabled: Boolean,
    val event: T,
)