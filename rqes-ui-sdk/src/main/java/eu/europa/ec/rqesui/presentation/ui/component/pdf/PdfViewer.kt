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

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import eu.europa.ec.rqesui.presentation.extension.clearAdd
import eu.europa.ec.rqesui.presentation.extension.loadPdf
import eu.europa.ec.rqesui.presentation.ui.component.utils.SPACING_LARGE
import eu.europa.ec.rqesui.presentation.ui.component.utils.SPACING_MEDIUM

@Composable
internal fun PdfViewer(
    modifier: Modifier = Modifier,
    arrangement: Arrangement.HorizontalOrVertical = Arrangement.spacedBy(SPACING_MEDIUM.dp),
    documentUri: Uri,
    onLoadingListener: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val pagePaths = remember {
        mutableStateListOf<String>()
    }
    val pdfStream = remember(documentUri) {
        context.contentResolver.openInputStream(documentUri)
    }

    Container(
        modifier = modifier,
        arrangement = arrangement,
        pagePaths = pagePaths
    )

    LaunchedEffect(pdfStream) {
        pdfStream?.let {
            val paths = context.loadPdf(
                inputStream = pdfStream,
                loadingListener = { isLoading ->
                    onLoadingListener(isLoading)
                }
            )
            pagePaths.clearAdd(paths)
        }
    }
}

@Composable
private fun Container(
    modifier: Modifier,
    arrangement: Arrangement.HorizontalOrVertical,
    pagePaths: SnapshotStateList<String>
) {
    val listState = rememberLazyListState()

    LazyColumn(
        verticalArrangement = arrangement,
        contentPadding = PaddingValues(vertical = SPACING_LARGE.dp),
        state = listState
    ) {
        items(pagePaths.size) { index ->

            val path = pagePaths[index]
            var imageBitmap by remember {
                mutableStateOf<ImageBitmap?>(null)
            }

            LaunchedEffect(path) {
                imageBitmap = BitmapFactory.decodeFile(path).asImageBitmap()
            }

            imageBitmap?.let { bitmap ->
                PdfPage(
                    modifier = modifier,
                    imageBitmap = bitmap,
                    listState = listState,
                    pagePaths = pagePaths,
                    index = index
                )
            }
        }
    }
}