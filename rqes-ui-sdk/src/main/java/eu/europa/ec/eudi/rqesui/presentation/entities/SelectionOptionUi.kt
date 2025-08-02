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