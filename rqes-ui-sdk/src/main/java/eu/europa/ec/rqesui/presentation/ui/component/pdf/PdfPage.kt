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

package eu.europa.ec.rqesui.presentation.ui.component.pdf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.dp
import eu.europa.ec.rqesui.infrastructure.theme.values.devider
import eu.europa.ec.rqesui.presentation.ui.component.ZoomableImage
import eu.europa.ec.rqesui.presentation.ui.component.preview.ThemeModePreviews
import eu.europa.ec.rqesui.presentation.ui.component.utils.SPACING_EXTRA_SMALL
import eu.europa.ec.rqesui.presentation.ui.component.utils.SPACING_LARGE

@Composable
internal fun PdfPage(
    modifier: Modifier,
    imageBitmap: ImageBitmap,
    listState: LazyListState,
    pagePaths: SnapshotStateList<String>,
    index: Int
) {
    Column(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ZoomableImage(
            BitmapPainter(imageBitmap),
            backgroundColor = Color.White,
            scrollState = listState
        )

        Row(
            modifier
                .padding(horizontal = SPACING_LARGE.dp)
                .background(
                    MaterialTheme.colorScheme.devider,
                ),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Page ${index + 1} /${pagePaths.size}",
                modifier = Modifier.padding(SPACING_EXTRA_SMALL.dp),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@ThemeModePreviews
@Composable
private fun PDFPagePreview() {
    PdfPage(
        modifier = Modifier,
        imageBitmap = ImageBitmap(100, 200),
        listState = LazyListState(),
        pagePaths = remember { mutableStateListOf("1", "2") },
        index = 1
    )
}
