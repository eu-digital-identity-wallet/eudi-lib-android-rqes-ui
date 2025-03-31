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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import eu.europa.ec.eudi.rqesui.domain.extension.toUriOrEmpty
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.presentation.architecture.ViewEvent
import eu.europa.ec.eudi.rqesui.presentation.entities.SelectionOptionUi
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.PreviewTheme
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.TextLengthPreviewProvider
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.ThemeModePreviews
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.HSpacer
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.SIZE_SMALL
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.SPACING_EXTRA_SMALL
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.SPACING_MEDIUM
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.VSpacer
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.WrapCard
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.WrapIcon
import eu.europa.ec.eudi.rqesui.presentation.ui.options_selection.Event

@Composable
internal fun <T : ViewEvent> SelectionItem(
    modifier: Modifier = Modifier,
    selectionItemData: SelectionOptionUi<T>,
    showDividerAbove: Boolean,
    onClick: ((T) -> Unit),
) {
    WrapCard(
        modifier = modifier,
        onClick = { onClick(selectionItemData.event) },
        throttleClicks = true,
        shape = RoundedCornerShape(SIZE_SMALL.dp),
        enabled = selectionItemData.enabled
    ) {
        if (showDividerAbove) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            VSpacer.Medium()
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = SPACING_MEDIUM.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            selectionItemData.leadingIcon?.let { safeIcon ->
                WrapIcon(
                    iconData = safeIcon,
                    customTint = selectionItemData.leadingIconTint
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = SPACING_MEDIUM.dp)
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                selectionItemData.actionText?.let { action ->
                    Text(
                        modifier = Modifier.padding(vertical = SPACING_EXTRA_SMALL.dp),
                        text = action,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                HSpacer.Small()

                selectionItemData.trailingIcon?.let { safeIcon ->
                    WrapIcon(
                        iconData = safeIcon,
                        customTint = selectionItemData.trailingIconTint
                            ?: MaterialTheme.colorScheme.primary
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
    val dummyEventForPreview = Event.ViewDocumentItemPressed(
        documentData = DocumentData(
            documentName = "Document.pdf",
            uri = "mockedUri".toUriOrEmpty()
        )
    )

    PreviewTheme {
        SelectionItem(
            modifier = Modifier.fillMaxWidth(),
            selectionItemData = SelectionOptionUi(
                overlineText = "Document",
                mainText = "File_to_be_signed.pdf",
                subtitle = text,
                actionText = "VIEW",
                enabled = true,
                event = dummyEventForPreview,
                leadingIcon = AppIcons.StepOne,
                trailingIcon = AppIcons.KeyboardArrowRight,
            ),
            showDividerAbove = false,
            onClick = {}
        )
    }
}