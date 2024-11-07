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

package eu.europa.ec.rqesui.presentation.ui.component.wrap

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import eu.europa.ec.rqesui.presentation.ui.component.preview.PreviewTheme
import eu.europa.ec.rqesui.presentation.ui.component.preview.ThemeModePreviews

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