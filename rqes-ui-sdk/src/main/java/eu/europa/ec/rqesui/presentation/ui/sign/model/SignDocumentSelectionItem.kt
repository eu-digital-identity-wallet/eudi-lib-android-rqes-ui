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

package eu.europa.ec.rqesui.presentation.ui.sign.model

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import eu.europa.ec.rqesui.uilogic.component.preview.PreviewTheme
import eu.europa.ec.rqesui.uilogic.component.preview.TextLengthPreviewProvider
import eu.europa.ec.rqesui.uilogic.component.preview.ThemeModePreviews
import eu.europa.ec.rqesui.uilogic.component.utils.SPACING_MEDIUM
import eu.europa.ec.rqesui.uilogic.component.wrap.WrapCard

data class SignDocumentSelectionData(
    val text: String,
    val label: String
)

@Composable
fun SignDocumentSelectionItem(
    modifier: Modifier = Modifier,
    data: SignDocumentSelectionData,
    enabled: Boolean = true,
    onClick: (() -> Unit)
) {
    WrapCard(
        modifier = modifier,
        enabled = enabled,
        onClick = onClick,
        throttleClicks = true,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        )
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxHeight()) {
            Row(
                modifier = Modifier.padding(SPACING_MEDIUM.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    modifier = Modifier.weight(1f),
                    text = "Document file name - created at 2024-01-01.pdf",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = data.label,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }

}

@ThemeModePreviews
@Composable
private fun SignDocumentButtonEnabledPreview(
    @PreviewParameter(TextLengthPreviewProvider::class) text: String
) {
    PreviewTheme {
        SignDocumentSelectionItem(
            modifier = Modifier.fillMaxWidth(),
            data = SignDocumentSelectionData(
                text = "test text",
                label = "VIEW"
            ),
            enabled = true,
            onClick = {}
        )
    }
}

@ThemeModePreviews
@Composable
private fun SignDocumentButtonDisabledPreview(
    @PreviewParameter(TextLengthPreviewProvider::class) text: String
) {
    PreviewTheme {
        SignDocumentSelectionItem(
            modifier = Modifier.fillMaxWidth(),
            data = SignDocumentSelectionData(
                text = "test text",
                label = "VIEW"
            ),
            enabled = false,
            onClick = {}
        )
    }
}
