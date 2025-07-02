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
import java.io.File
import java.io.FileOutputStream

internal object FileHelper {

    /**
     * Returns a [File] representing the downloads cache directory.
     *
     * This function retrieves the cache directory for the application's context and creates a subdirectory named "downloads" within it if it doesn't already exist.
     *
     * @param context The application context.
     * @return A [File] object representing the downloads cache directory.
     */
    internal fun getDownloadsDir(context: Context): File =
        File(context.cacheDir, "downloads").apply { mkdirs() }

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
}