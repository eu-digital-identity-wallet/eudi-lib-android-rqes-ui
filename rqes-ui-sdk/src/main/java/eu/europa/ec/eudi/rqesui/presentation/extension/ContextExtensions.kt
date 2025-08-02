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

package eu.europa.ec.eudi.rqesui.presentation.extension

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import eu.europa.ec.eudi.rqesui.presentation.ui.container.EudiRQESContainer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

internal fun Context.finish() {
    (this as? EudiRQESContainer)?.finish()
}

/**
 * Parses a string url and sends the Action View Intent.
 *
 * @param uri the url to parse.
 */
internal fun Context.openUrl(uri: Uri) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (_: Exception) {
    }
}

/**
 * Opens an intent chooser with the provided intent and title.
 *
 * This function attempts to start an activity using the provided intent, wrapped in an intent chooser.
 * It handles any exceptions that may occur during the process and silently ignores them.
 *
 * @param intent The intent to be launched.
 * @param title The title to be displayed in the intent chooser dialog.
 */
internal fun Context.openIntentChooser(
    intent: Intent,
    title: String,
) {
    try {
        startActivity(Intent.createChooser(intent, title))
    } catch (_: Exception) {
    }
}

internal suspend fun Context.loadPdf(
    inputStream: InputStream,
    loadingListener: (isLoading: Boolean) -> Unit = { },
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): List<String> = withContext(dispatcher) {
    loadingListener(true)

    val outputDir = cacheDir
    val temporaryFile = File.createTempFile("temporary_file", "pdf").apply { deleteOnExit() }

    inputStream.use { input ->
        FileOutputStream(temporaryFile).use { output ->
            input.copyTo(output)
        }
    }

    val pdfPaths = mutableListOf<String>()
    ParcelFileDescriptor.open(temporaryFile, ParcelFileDescriptor.MODE_READ_ONLY)
        .use { parcelFileDescriptor ->
            PdfRenderer(parcelFileDescriptor).use { renderer ->
                for (pageNumber in 0 until renderer.pageCount) {
                    loadingListener(true)

                    val bitmap = Bitmap.createBitmap(
                        PDF_BITMAP_WIDTH,
                        PDF_BITMAP_HEIGHT,
                        Bitmap.Config.ARGB_8888
                    )

                    val pageCanvas = Canvas(bitmap)
                    pageCanvas.drawColor(Color.WHITE)

                    renderer.openPage(pageNumber).use { page ->
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    }

                    val pageFile = File.createTempFile("PDFPage$pageNumber", "png", outputDir)
                        .apply { deleteOnExit() }

                    FileOutputStream(pageFile).use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }

                    pdfPaths.add(pageFile.absolutePath)
                    bitmap.recycle()
                }
            }
        }
    loadingListener(false)

    return@withContext pdfPaths
}

private const val PDF_BITMAP_WIDTH = 1240
private const val PDF_BITMAP_HEIGHT = 1754