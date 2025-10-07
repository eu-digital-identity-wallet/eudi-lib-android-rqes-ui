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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.europa.ec.eudi.rqesui.infrastructure.theme.values.divider
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.PreviewTheme
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.ThemeModePreviews
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.SPACING_LARGE

@Composable
internal fun WrapStickyBottomBar(
    config: ButtonConfig,
    buttonText: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.divider
        )

        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            WrapButton(
                modifier = Modifier.fillMaxWidth(),
                buttonConfig = config,
                content = {
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            )
        }
    }
}

@ThemeModePreviews
@Composable
private fun WrapBottomBarPrimaryButtonPreview() {
    PreviewTheme {
        WrapStickyBottomBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SPACING_LARGE.dp),
            buttonText = "Sign",
            config = ButtonConfig(
                type = ButtonType.PRIMARY,
                enabled = true,
                onClick = {}
            )
        )
    }
}

@ThemeModePreviews
@Composable
private fun WrapBottomBarSecondaryButtonPreview() {
    PreviewTheme {
        WrapStickyBottomBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SPACING_LARGE.dp),
            buttonText = "Sign",
            config = ButtonConfig(
                type = ButtonType.SECONDARY,
                enabled = true,
                onClick = {}
            )
        )
    }
}