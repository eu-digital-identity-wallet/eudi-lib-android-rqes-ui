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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.europa.ec.rqesui.infrastructure.theme.textDisabledDark
import eu.europa.ec.rqesui.infrastructure.theme.textPrimaryDark
import eu.europa.ec.rqesui.presentation.ui.component.utils.SIZE_100
import eu.europa.ec.rqesui.presentation.ui.component.utils.SPACING_MEDIUM


private val buttonsShape: RoundedCornerShape = RoundedCornerShape(SIZE_100.dp)

private val buttonsContentHorizontalPadding: PaddingValues =
    PaddingValues(horizontal = SPACING_MEDIUM.dp)

@Composable
fun WrapPrimaryButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    val textColor = if (isSystemInDarkTheme()) {
        Color.White
    } else {
        MaterialTheme.colorScheme.background
    }

    Button(
        modifier = modifier,
        enabled = enabled,
        onClick = onClick,
        shape = buttonsShape,
        colors = ButtonDefaults.textButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = textColor,
            disabledContainerColor = MaterialTheme.colorScheme.secondary,
            disabledContentColor = MaterialTheme.colorScheme.textDisabledDark,
        ),
        contentPadding = buttonsContentHorizontalPadding,
        content = content
    )
}

@Composable
fun WrapSecondaryButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        modifier = modifier,
        enabled = enabled,
        onClick = onClick,
        shape = buttonsShape,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.textPrimaryDark,
            disabledContainerColor = MaterialTheme.colorScheme.background,
            disabledContentColor = MaterialTheme.colorScheme.textDisabledDark,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary,
        ),
        contentPadding = buttonsContentHorizontalPadding,
        content = content
    )
}

@Preview(showBackground = true)
@Composable
fun WrapPrimaryButtonPreview() {
    WrapPrimaryButton(
        modifier = Modifier.padding(16.dp),
        enabled = true,
        onClick = { }
    ) {
        Text("Primary Button")
    }
}