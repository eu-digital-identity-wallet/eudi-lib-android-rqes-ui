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

package eu.europa.ec.eudi.rqesui.domain.helper

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

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
}