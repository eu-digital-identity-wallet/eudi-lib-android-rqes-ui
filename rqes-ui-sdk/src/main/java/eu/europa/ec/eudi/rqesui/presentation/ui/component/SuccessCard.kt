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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import eu.europa.ec.eudi.rqesui.domain.extension.toUriOrEmpty
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.infrastructure.theme.values.success
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.PreviewTheme
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.TextLengthPreviewProvider
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.ThemeModePreviews
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.SIZE_SMALL
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.SPACING_LARGE
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.SPACING_MEDIUM
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.WrapCard
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.WrapIcon

internal data class SuccessCardData(
    val leadingIcon: IconData,
    val documentData: DocumentData,
    val actionText: String,
)

@Composable
internal fun SuccessCard(
    modifier: Modifier = Modifier,
    successCardData: SuccessCardData,
    onClick: (() -> Unit),
) {
    WrapCard(
        modifier = modifier,
        enabled = true,
        onClick = onClick,
        throttleClicks = true,
        shape = RoundedCornerShape(SIZE_SMALL.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = SPACING_LARGE.dp,
                    horizontal = SPACING_MEDIUM.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WrapIcon(
                iconData = successCardData.leadingIcon,
                customTint = MaterialTheme.colorScheme.success
            )

            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = SPACING_MEDIUM.dp),
                text = successCardData.documentData.documentName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = successCardData.actionText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@ThemeModePreviews
@Composable
private fun SuccessCardPreview(
    @PreviewParameter(TextLengthPreviewProvider::class) text: String
) {
    PreviewTheme {
        SuccessCard(
            modifier = Modifier.fillMaxWidth(),
            successCardData = SuccessCardData(
                leadingIcon = AppIcons.Verified,
                documentData = DocumentData(
                    documentName = text,
                    uri = "mockedUri".toUriOrEmpty()
                ),
                actionText = "VIEW",
            ),
            onClick = {}
        )
    }
}