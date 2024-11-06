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

package eu.europa.ec.rqesui.presentation.extension

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import eu.europa.ec.rqesui.presentation.ui.container.EudiRQESContainer
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

/**
 * Retrieves the file name from a given [Uri].
 *
 * This function attempts to extract the file name from the provided [Uri] using the content resolver.
 * If successful, it returns the file name.
 * If the file name cannot be determined from the [Uri], it returns the provided `fallbackName`.
 * If an exception occurs during the process, it also returns the `fallbackName`.
 *
 * @param context The [Context] used to access the content resolver.
 * @param fallbackName The name to return if the file name cannot be determined or an error occurs. Defaults to "Empty File Name".
 * @return The file name extracted from the [Uri], or the `fallbackName` if an error occurs or the name cannot be determined.
 */
internal fun Uri.getFileName(
    context: Context,
    fallbackName: String = "Empty File Name"
): String {
    return try {
        val fileName = context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        }
        //TODO what should be returned if fileName is null?
        //return fileName.orEmpty()
        fileName ?: fallbackName
    } catch (e: Exception) {
        fallbackName
    }
}