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

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.europa.ec.rqesui.infrastructure.theme.values.success
import eu.europa.ec.rqesui.presentation.ui.component.preview.ThemeModePreviews
import eu.europa.ec.rqesui.presentation.ui.component.utils.SPACING_EXTRA_SMALL
import eu.europa.ec.rqesui.presentation.ui.component.wrap.WrapIcon

@Composable
internal fun TextWithBadge(
    message: String,
    showBadge: Boolean
) {
    val inlineContentMap = mapOf(
        "badgeIconId" to InlineTextContent(
            Placeholder(20.sp, 20.sp, PlaceholderVerticalAlign.TextCenter)
        ) {
            WrapIcon(
                iconData = AppIcons.Verified,
                customTint = MaterialTheme.colorScheme.success
            )
        }
    )

    val textWithBadgeData = TextWithBadgeData(
        textAfterBadge = message,
        showBadge = showBadge
    )

    Text(
        modifier = Modifier
            .offset(x = -SPACING_EXTRA_SMALL.dp)
            .takeIf { showBadge } ?: Modifier,
        text = textWithBadgeData.annotatedString,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        ),
        inlineContent = inlineContentMap
    )
}

internal data class TextWithBadgeData(
    private val textAfterBadge: String? = null,
    private val showBadge: Boolean
) {
    val annotatedString = buildAnnotatedString {
        if (showBadge) {
            append(" ")
            appendInlineContent(id = "badgeIconId")
            append(" ")
        }
        if (!textAfterBadge.isNullOrEmpty()) {
            append(textAfterBadge)
        }
    }
}

@ThemeModePreviews
@Composable
private fun TextWithBadgePreview() {
    TextWithBadge(
        message = "Document_title.PDF",
        showBadge = true
    )
}
