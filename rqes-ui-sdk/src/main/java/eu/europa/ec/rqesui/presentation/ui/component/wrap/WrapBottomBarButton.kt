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
import eu.europa.ec.rqesui.infrastructure.theme.values.divider
import eu.europa.ec.rqesui.presentation.ui.component.preview.PreviewTheme
import eu.europa.ec.rqesui.presentation.ui.component.preview.ThemeModePreviews
import eu.europa.ec.rqesui.presentation.ui.component.utils.SPACING_LARGE

private sealed interface StickyBottomBarConfig {
    data object Primary : StickyBottomBarConfig
    data object Secondary : StickyBottomBarConfig
}

@Composable
internal fun WrapBottomBarPrimaryButton(
    buttonText: String,
    enabled: Boolean = true,
    onButtonClick: () -> Unit,
) {
    WrapStickyBottomBar(
        config = StickyBottomBarConfig.Primary,
        buttonText = buttonText,
        enabled = enabled,
        onButtonClick = onButtonClick,
    )
}

@Composable
internal fun WrapBottomBarSecondaryButton(
    buttonText: String,
    enabled: Boolean = true,
    onButtonClick: () -> Unit,
) {
    WrapStickyBottomBar(
        config = StickyBottomBarConfig.Secondary,
        buttonText = buttonText,
        enabled = enabled,
        onButtonClick = onButtonClick,
    )
}

@Composable
private fun WrapStickyBottomBar(
    config: StickyBottomBarConfig,
    buttonText: String,
    enabled: Boolean,
    onButtonClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.divider
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = SPACING_LARGE.dp),
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
            buttonText = "Sign",
            onButtonClick = {},
        )
    }
}