/*
 * Copyright (c) 2025 European Commission
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.europa.ec.eudi.rqesui.presentation.ui.component.content

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.europa.ec.eudi.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.eudi.rqesui.infrastructure.provider.ResourceProvider
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.PreviewTheme
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.ThemeModePreviews
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.SIZE_MEDIUM
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.WrapPrimaryButton
import org.koin.compose.koinInject

internal data class ContentErrorConfig(
    val errorTitle: String,
    val errorSubTitle: String,
    val onCancel: () -> Unit,
    val onRetry: (() -> Unit)? = null
)

@Composable
internal fun ContentError(
    config: ContentErrorConfig,
    paddingValues: PaddingValues,
    resourceProvider: ResourceProvider = koinInject(),
    subtitleStyle: TextStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurface
    ),
    subTitleMaxLines: Int = Int.MAX_VALUE
) {

    Column(
        Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = config.errorSubTitle,
            style = subtitleStyle,
            maxLines = subTitleMaxLines,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.weight(1f))

        config.onRetry?.let { callback ->
            WrapPrimaryButton(
                onClick = {
                    callback()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = resourceProvider.getLocalizedString(LocalizableKey.GenericErrorButtonRetry)
                )
            }
        }
    }
}

/**
 * Preview composable of [ContentError].
 */
@ThemeModePreviews
@Composable
private fun PreviewContentErrorScreen() {
    PreviewTheme {
        ContentError(
            config = ContentErrorConfig(
                errorTitle = "Error Title",
                errorSubTitle = "Error Message",
                onCancel = {}
            ),
            paddingValues = PaddingValues(SIZE_MEDIUM.dp)
        )
    }
}