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

package eu.europa.ec.eudi.rqesui.domain.extension

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toUri
import eu.europa.ec.eudi.rqesui.domain.entities.error.EudiRQESUiError

/**
 * Retrieves the file name from a given [Uri].
 *
 * This function attempts to extract the file name from the provided [Uri] using the content resolver.
 * If successful, it returns the file name.
 * If the file name cannot be determined from the [Uri], it will throw [EudiRQESUiError].
 *
 * @param context The [Context] used to access the content resolver.
 * @return The file name extracted from the [Uri], if an error occurs or the name cannot be determined throws [EudiRQESUiError].
 */
@Throws(EudiRQESUiError::class)
internal fun Uri.getFileName(context: Context): Result<String> {
    val fileName = context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        cursor.getString(nameIndex)
    }
    return if (fileName.isNullOrEmpty()) {
        Result.failure(
            EudiRQESUiError(
                title = "File Error",
                message = "Unable to determine file name"
            )
        )
    } else {
        Result.success(fileName)
    }
}

/**
 * Decodes a URI string and returns a Result containing the decoded URI.
 *
 * This function attempts to decode a given URI string. If successful, it returns a Result
 * containing the decoded URI. If an error occurs during decoding (e.g., due to an invalid
 * URI format), it returns a Result containing an EudiRQESUiError.
 *
 * @return A Result containing the decoded Uri on success, or a Result containing an
 *         EudiRQESUiError on failure.
 * @throws EudiRQESUiError if an error occurs during URI decoding.  Note that while the function
 *         returns a `Result` to handle errors gracefully, it is still annotated with `@Throws`
 *         to indicate the potential exception for interoperability with Java.
 */
@Throws(EudiRQESUiError::class)
internal fun Uri.decode(): Result<Uri> = try {
    val decoded = Uri.decode(this.toString()).toUri()
    Result.success(decoded)
} catch (e: Exception) {
    Result.failure(
        EudiRQESUiError(
            title = "Uri Decoder",
            message = e.localizedMessage ?: "Unable to decode uri"
        )
    )
}