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

package eu.europa.ec.eudi.rqesui.presentation.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.europa.ec.eudi.rqesui.domain.extension.toUri
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.presentation.entities.SelectionOptionUi
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.PreviewTheme
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.TextLengthPreviewProvider
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.ThemeModePreviews
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.HSpacer
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.SIZE_SMALL
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.SPACING_LARGE
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.SPACING_MEDIUM
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.WrapCard
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.WrapIcon
import eu.europa.ec.eudi.rqesui.presentation.ui.options_selection.Event

@Composable
internal fun SelectionItem(
    modifier: Modifier = Modifier,
    selectionItemData: SelectionOptionUi<*>,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = Color.Transparent
    ),
    shape: Shape = RoundedCornerShape(SIZE_SMALL.dp),
    verticalPadding: Dp = SPACING_MEDIUM.dp,
    horizontalPadding: Dp = SPACING_LARGE.dp,
    trailingActionAlignment: Alignment.Vertical = Alignment.Top,
    enabled: Boolean = true,
    onClick: (() -> Unit)?,
) {
    WrapCard(
        modifier = modifier,
        onClick = onClick,
        throttleClicks = true,
        shape = shape,
        colors = colors,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = horizontalPadding,
                vertical = verticalPadding
            ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = trailingActionAlignment
        ) {
            selectionItemData.leadingIcon?.let { safeIcon ->
                WrapIcon(
                    modifier = Modifier.padding(end = SPACING_MEDIUM.dp),
                    iconData = safeIcon,
                    customTint = selectionItemData.leadingIconTint
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                selectionItemData.overlineText?.let { safeOverlineText ->
                    Text(
                        text = safeOverlineText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                selectionItemData.mainText?.let { safeMainText ->
                    Text(
                        text = safeMainText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                selectionItemData.subtitle?.let { subtitle ->
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(
                modifier = Modifier.padding(start = SPACING_MEDIUM.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                selectionItemData.actionText?.let { action ->
                    Text(
                        text = action,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                HSpacer.Small()

                selectionItemData.trailingIcon?.let { safeIcon ->
                    WrapIcon(
                        iconData = safeIcon,
                        customTint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private val DummyEventForPreview = Event.ViewDocumentItemPressed(
    documentData = DocumentData(
        documentName = "Document.pdf",
        uri = "mockedUri".toUri()
    )
)

@ThemeModePreviews
@Composable
private fun SelectionItemWithNoSubtitlePreview() {
    PreviewTheme {
        SelectionItem(
            modifier = Modifier.fillMaxWidth(),
            selectionItemData = SelectionOptionUi(
                mainText = "Select document",
                actionText = "VIEW",
                enabled = true,
                event = DummyEventForPreview
            ),
            onClick = {}
        )
    }
}

@ThemeModePreviews
@Composable
private fun SelectionItemWithSubtitlePreview(
    @PreviewParameter(TextLengthPreviewProvider::class) text: String
) {
    PreviewTheme {
        SelectionItem(
            modifier = Modifier.fillMaxWidth(),
            selectionItemData = SelectionOptionUi(
                mainText = text,
                subtitle = text,
                actionText = "VIEW",
                leadingIcon = AppIcons.StepOne,
                trailingIcon = AppIcons.KeyboardArrowRight,
                enabled = true,
                event = DummyEventForPreview
            ),
            onClick = {}
        )
    }
}