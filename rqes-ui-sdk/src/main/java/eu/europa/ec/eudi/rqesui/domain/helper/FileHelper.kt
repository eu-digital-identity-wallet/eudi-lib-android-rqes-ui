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

package eu.europa.ec.eudi.rqesui.domain.helper

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Base64

internal object FileHelper {

    /**
     * Converts a content Uri to a File object.
     *
     * This function takes a content Uri, creates a temporary file in the app's cache directory,
     * and copies the content from the Uri to the temporary file.
     *
     * @param context The application context.
     * @param uri The content [Uri] to convert.
     * @param fileName The desired name for the temporary file.
     * @return A [Result] object containing the temporary [File] if successful, or a [Throwable] if an error occurred.
     */
    internal fun uriToFile(context: Context, uri: Uri, fileName: String): Result<File> {
        return runCatching {
            // Define a temporary file in the app's cache directory
            val tempFile = File(context.cacheDir, fileName)
            // Open InputStream from the Uri
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                // Copy the InputStream to the temporary file
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return@runCatching tempFile // Return the temporary file
        }
    }

    /**
     * Saves a Base64 decoded PDF from an InputStream to a file in the app's cache directory
     * and returns a shareable Uri using FileProvider.
     *
     * This function takes an InputStream containing Base64 encoded PDF data, decodes it,
     * and saves it to a file within the app's cache directory under a "signed_pdfs" subdirectory.
     * It then generates a content Uri using FileProvider, making the saved PDF accessible
     * to other apps for sharing purposes.
     *
     * **Note:** Ensure that you have properly configured a FileProvider in your AndroidManifest.xml
     * with the authority "${context.packageName}.provider" to enable sharing the generated Uri.
     *
     * @param context The application context.
     * @param inputStream The InputStream containing the Base64 encoded PDF data.
     * @param fileName The desired name for the saved PDF file (including the .pdf extension).
     *
     * @return A Result object containing either the shareable Uri of the saved PDF file on success,
     *         or a Throwable in case of an error during the process. The Throwable can be an
     *         `IOException` if an error occurs during file I/O operations, or an
     *         `IllegalArgumentException` if the provided InputStream is invalid or the fileName is empty.
     *
     * @throws SecurityException If the FileProvider is not configured correctly or if the app does not
     *         have the necessary permissions to access the file.
     */
    internal fun saveBase64DecodedPdfToShareableUri(
        context: Context,
        inputStream: InputStream,
        fileName: String,
    ): Result<Uri> {
        return runCatching {
            // Define the directory and file for saving the decoded PDF
            val sharedPdfDir = File(context.cacheDir, "signed_pdfs").apply { mkdirs() }
            val pdfFile = File(sharedPdfDir, fileName)

            // Decode the Base64 InputStream and write it to the file
            inputStream.use { input ->
                FileOutputStream(pdfFile).use { output ->
                    val buffer = ByteArray(8192) // 8 KB buffer
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        // Decode only the read bytes and write them to the file
                        val decodedBytes = Base64.getDecoder().decode(buffer.copyOf(bytesRead))
                        output.write(decodedBytes)
                    }
                }
            }

            // Return a shareable Uri using FileProvider
            return@runCatching FileProvider.getUriForFile(
                context,
                "${context.packageName}.rqes.library.provider",
                pdfFile
            )
        }
    }
}