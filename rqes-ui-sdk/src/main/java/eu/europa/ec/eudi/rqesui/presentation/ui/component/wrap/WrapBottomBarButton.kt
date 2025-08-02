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

private sealed interface StickyBottomBarConfig {
    data object Primary : StickyBottomBarConfig
    data object Secondary : StickyBottomBarConfig
}

@Composable
internal fun WrapBottomBarPrimaryButton(
    stickyBottomContentModifier: Modifier = Modifier,
    buttonText: String,
    enabled: Boolean = true,
    onButtonClick: () -> Unit,
) {
    WrapStickyBottomBar(
        config = StickyBottomBarConfig.Primary,
        stickyBottomContentModifier = stickyBottomContentModifier,
        buttonText = buttonText,
        enabled = enabled,
        onButtonClick = onButtonClick,
    )
}

@Composable
internal fun WrapBottomBarSecondaryButton(
    stickyBottomContentModifier: Modifier = Modifier,
    buttonText: String,
    enabled: Boolean = true,
    onButtonClick: () -> Unit,
) {
    WrapStickyBottomBar(
        config = StickyBottomBarConfig.Secondary,
        stickyBottomContentModifier = stickyBottomContentModifier,
        buttonText = buttonText,
        enabled = enabled,
        onButtonClick = onButtonClick,
    )
}

@Composable
private fun WrapStickyBottomBar(
    config: StickyBottomBarConfig,
    stickyBottomContentModifier: Modifier = Modifier,
    buttonText: String,
    enabled: Boolean,
    onButtonClick: () -> Unit,
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
            modifier = stickyBottomContentModifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            ConfigBasedButton(
                config = config,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                onButtonClick = onButtonClick,
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

@Composable
private fun ConfigBasedButton(
    config: StickyBottomBarConfig,
    modifier: Modifier,
    enabled: Boolean,
    onButtonClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    when (config) {
        is StickyBottomBarConfig.Primary -> {
            WrapPrimaryButton(
                modifier = modifier,
                enabled = enabled,
                onClick = onButtonClick,
            ) {
                content()
            }
        }

        is StickyBottomBarConfig.Secondary -> {
            WrapSecondaryButton(
                modifier = modifier,
                enabled = enabled,
                onClick = onButtonClick,
            ) {
                content()
            }
        }
    }
}

@ThemeModePreviews
@Composable
private fun WrapBottomBarPrimaryButtonPreview() {
    PreviewTheme {
        WrapBottomBarPrimaryButton(
            stickyBottomContentModifier = Modifier
                .fillMaxWidth()
                .padding(SPACING_LARGE.dp),
            buttonText = "Sign",
            onButtonClick = {},
        )
    }
}

@ThemeModePreviews
@Composable
private fun WrapBottomBarSecondaryButtonPreview() {
    PreviewTheme {
        WrapBottomBarSecondaryButton(
            stickyBottomContentModifier = Modifier
                .fillMaxWidth()
                .padding(SPACING_LARGE.dp),
            buttonText = "Sign",
            onButtonClick = {},
        )
    }
}