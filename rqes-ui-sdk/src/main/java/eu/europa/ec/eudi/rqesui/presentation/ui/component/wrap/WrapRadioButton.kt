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

package eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.PreviewTheme
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.ThemeModePreviews

@Composable
internal fun WrapRadioButton(
    isSelected: Boolean,
    isEnabled: Boolean = true,
    colors: RadioButtonColors = RadioButtonDefaults.colors(
        selectedColor = MaterialTheme.colorScheme.primary,
        unselectedColor = MaterialTheme.colorScheme.primary
    ),
    onClick: (() -> Unit)? = null,
) {
    RadioButton(
        selected = isSelected,
        enabled = isEnabled,
        onClick = onClick,
        colors = colors
    )
}

@ThemeModePreviews
@Composable
private fun WrapRadioButtonSelectedEnabledPreview() {
    PreviewTheme {
        WrapRadioButton(
            isSelected = true,
            isEnabled = true,
        )
    }
}

@ThemeModePreviews
@Composable
private fun WrapRadioButtonSelectedDisabledPreview() {
    PreviewTheme {
        WrapRadioButton(
            isSelected = true,
            isEnabled = false,
        )
    }
}

@ThemeModePreviews
@Composable
private fun WrapRadioButtonUnselectedEnabledPreview() {
    PreviewTheme {
        WrapRadioButton(
            isSelected = false,
            isEnabled = true,
        )
    }
}

@ThemeModePreviews
@Composable
private fun WrapRadioButtonUnselectedDisabledPreview() {
    PreviewTheme {
        WrapRadioButton(
            isSelected = false,
            isEnabled = false,
        )
    }
}