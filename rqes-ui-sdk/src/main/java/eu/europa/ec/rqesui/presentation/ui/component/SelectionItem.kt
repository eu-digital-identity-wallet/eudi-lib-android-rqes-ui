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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import eu.europa.ec.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.rqesui.infrastructure.theme.values.ThemeColors
import eu.europa.ec.rqesui.presentation.entities.SelectionItemUi
import eu.europa.ec.rqesui.presentation.ui.component.preview.PreviewTheme
import eu.europa.ec.rqesui.presentation.ui.component.preview.TextLengthPreviewProvider
import eu.europa.ec.rqesui.presentation.ui.component.preview.ThemeModePreviews
import eu.europa.ec.rqesui.presentation.ui.component.utils.SIZE_SMALL
import eu.europa.ec.rqesui.presentation.ui.component.utils.SPACING_LARGE
import eu.europa.ec.rqesui.presentation.ui.component.utils.SPACING_MEDIUM
import eu.europa.ec.rqesui.presentation.ui.component.wrap.WrapCard
import java.net.URI

@Composable
internal fun SelectionItem(
    modifier: Modifier = Modifier,
    data: SelectionItemUi,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = ThemeColors.primaryVariant //TODO find a good color for DarkMode primaryVariant
    ),
    shape: Shape = RoundedCornerShape(SIZE_SMALL.dp),
    onClick: (() -> Unit)
) {
    WrapCard(
        modifier = modifier,
        onClick = onClick,
        throttleClicks = true,
        shape = shape,
        colors = colors,
    ) {
        Row(
            modifier = modifier.padding(
                horizontal = SPACING_MEDIUM.dp,
                vertical = SPACING_LARGE.dp
            ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = data.documentData.documentName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = data.action,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
                documentData = DocumentData(
                    documentName = text,
                    uri = URI("test")
                ),
                action = "VIEW",
            ),
            onClick = {}
        )
    }
}