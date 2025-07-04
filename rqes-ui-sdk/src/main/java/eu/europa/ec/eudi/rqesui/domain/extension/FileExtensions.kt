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