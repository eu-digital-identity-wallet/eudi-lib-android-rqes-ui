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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import eu.europa.ec.eudi.rqesui.infrastructure.theme.values.success
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.PreviewTheme
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.TextLengthPreviewProvider
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.ThemeModePreviews
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.TextConfig
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.WrapIcon
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.WrapText

/**
 * Data class representing information about a Relying Party.
 *
 * @property isVerified A boolean indicating whether the Relying Party is verified.
 * @property name The name of the Relying Party.
 */
data class RelyingPartyData(
    val isVerified: Boolean,
    val name: String,
)

@Composable
fun RelyingParty(
    modifier: Modifier = Modifier,
    relyingPartyData: RelyingPartyData,
) {
    val commonTextAlign = TextAlign.Center

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        with(relyingPartyData) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isVerified) {
                    WrapIcon(
                        modifier = Modifier.size(20.dp),
                        iconData = AppIcons.VerifiedBadge,
                        customTint = MaterialTheme.colorScheme.success,
                    )
                }
                WrapText(
                    modifier = Modifier.wrapContentWidth(),
                    text = name,
                    textConfig = TextConfig(
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = commonTextAlign,
                    )
                )
            }

        }
    }
}

@ThemeModePreviews
@Composable
private fun RelyingPartyPreview(
    @PreviewParameter(TextLengthPreviewProvider::class) text: String
) {
    PreviewTheme {
        RelyingParty(
            relyingPartyData = RelyingPartyData(
                isVerified = true,
                name = "Relying Party Name: $text",
            )
        )
    }
}