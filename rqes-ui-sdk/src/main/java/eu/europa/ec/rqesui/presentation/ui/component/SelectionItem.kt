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

package eu.europa.ec.rqesui.presentation.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import eu.europa.ec.rqesui.infrastructure.theme.values.primaryVariant
import eu.europa.ec.rqesui.infrastructure.theme.values.success
import eu.europa.ec.rqesui.presentation.entities.SelectionItemUi
import eu.europa.ec.rqesui.presentation.ui.component.preview.PreviewTheme
import eu.europa.ec.rqesui.presentation.ui.component.preview.TextLengthPreviewProvider
import eu.europa.ec.rqesui.presentation.ui.component.preview.ThemeModePreviews
import eu.europa.ec.rqesui.presentation.ui.component.utils.SPACING_LARGE
import eu.europa.ec.rqesui.presentation.ui.component.utils.SPACING_MEDIUM
import eu.europa.ec.rqesui.presentation.ui.component.utils.VSpacer
import eu.europa.ec.rqesui.presentation.ui.component.wrap.WrapCard
import eu.europa.ec.rqesui.presentation.ui.component.wrap.WrapIcon

@Composable
internal fun SelectionItem(
    modifier: Modifier = Modifier,
    data: SelectionItemUi,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryVariant
    ),
    onClick: (() -> Unit)? = null
) {
    WrapCard(
        modifier = modifier,
        onClick = onClick,
        throttleClicks = true,
        colors = colors,
    ) {
        Box(
            modifier = Modifier.wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = SPACING_MEDIUM.dp,
                    vertical = SPACING_LARGE.dp
                ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    data.subTitle?.let {
                        VSpacer.ExtraSmall()

                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                data.action?.let { action ->
                    Text(
                        text = action,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                data.icon?.let { icon ->
                    WrapIcon(
                        modifier = Modifier.padding(start = SPACING_MEDIUM.dp),
                        iconData = icon,
                        customTint = MaterialTheme.colorScheme.success
                    )
                }
            }
        }
    }

}

@ThemeModePreviews
@Composable
private fun SelectionItemPreview(
    @PreviewParameter(TextLengthPreviewProvider::class) text: String
) {
    PreviewTheme {
        SelectionItem(
            modifier = Modifier.fillMaxWidth(),
            data = SelectionItemUi(
                title = "test text",
                subTitle = "test subtitle",
                action = "VIEW",
            ),
            onClick = {}
        )
    }
}