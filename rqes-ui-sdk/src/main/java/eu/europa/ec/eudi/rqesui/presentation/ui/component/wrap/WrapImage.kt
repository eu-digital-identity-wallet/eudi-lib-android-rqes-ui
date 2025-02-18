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

package eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import eu.europa.ec.eudi.rqesui.presentation.ui.component.IconData

@Composable
internal fun WrapImage(
    iconData: IconData,
    modifier: Modifier = Modifier,
    colorFilter: ColorFilter? = null,
    contentScale: ContentScale? = null,
) {
    val iconContentDescription = stringResource(id = iconData.contentDescriptionId)

    iconData.resourceId?.let { resId ->
        Image(
            modifier = modifier,
            painter = painterResource(id = resId),
            contentDescription = iconContentDescription,
            colorFilter = colorFilter,
            contentScale = contentScale ?: ContentScale.FillBounds,
        )
    } ?: run {
        iconData.imageVector?.let { imageVector ->
            Image(
                modifier = modifier,
                imageVector = imageVector,
                contentDescription = iconContentDescription,
                colorFilter = colorFilter,
                contentScale = contentScale ?: ContentScale.FillBounds,
            )
        }
    }
}