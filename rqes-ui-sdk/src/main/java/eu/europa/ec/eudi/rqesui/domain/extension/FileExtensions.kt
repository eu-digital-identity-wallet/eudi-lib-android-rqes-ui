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
import androidx.core.content.FileProvider
import java.io.File

/**
 * Converts a [File] to a shareable [Uri] using a [FileProvider].
 *
 * This function is useful for sharing files with other applications,
 * as it ensures that the receiving application has the necessary permissions
 * to access the file.
 *
 * @param context The [Context] to use for creating the [FileProvider].
 * @return A [Result] containing the shareable [Uri] on success,
 *         or an exception on failure.
 */
internal fun File.toShareableUri(
    context: Context
): Result<Uri> {
    return runCatching {
        return@runCatching FileProvider.getUriForFile(
            context,
            "${context.packageName}.rqes.library.provider",
            this
        )
    }
}