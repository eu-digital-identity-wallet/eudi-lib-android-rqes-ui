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
import androidx.annotation.RawRes
import eu.europa.ec.eudi.rqesui.domain.entities.error.EudiRQESUiError
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

/**
 * Retrieves an X.509 certificate from a raw resource.
 *
 * @param resId The resource ID of the certificate file (e.g., R.raw.my_certificate).
 * @return A [Result] object containing either the loaded [X509Certificate] on success,
 *         or a [EudiRQESUiError] on failure.  The failure case includes a user-friendly
 *         title ("Certificate Error") and a message detailing the issue, such as "Resource
 *         not found" or a more specific error if available.
 * @throws EudiRQESUiError if an exception occurs during certificate loading. This exception
 *         is also encapsulated in the [Result.failure] case, but is explicitly declared for
 *         clarity.
 */
@Throws(EudiRQESUiError::class)
internal fun Context.getCertificate(@RawRes resId: Int): Result<X509Certificate> = try {
    val certificate = resources.openRawResource(resId).use {
        CertificateFactory.getInstance("X509")
            .generateCertificate(it) as X509Certificate
    }
    Result.success(certificate)
} catch (e: Exception) {
    Result.failure(
        EudiRQESUiError(
            title = "Certificate Error",
            message = e.localizedMessage ?: "Resource $resId not found"
        )
    )
}