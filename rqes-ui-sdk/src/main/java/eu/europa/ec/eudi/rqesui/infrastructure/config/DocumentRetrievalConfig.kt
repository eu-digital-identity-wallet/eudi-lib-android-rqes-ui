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

package eu.europa.ec.eudi.rqesui.infrastructure.config

import android.content.Context
import eu.europa.ec.eudi.documentretrieval.X509CertificateTrust
import eu.europa.ec.eudi.rqesui.domain.extension.getCertificate
import eu.europa.ec.eudi.rqes.core.documentRetrieval.X509CertificateTrust as X509CertificateTrustMethod

/**
 * Sealed interface representing different configurations for document retrieval, specifically related to X.509 certificate trust.
 *
 * This interface allows specifying how to trust certificates when retrieving documents, offering options for using a predefined
 * implementation, loading certificates from resources, or disabling validation entirely.
 */
sealed interface DocumentRetrievalConfig {

    val impl: X509CertificateTrust?

    data class X509CertificateImpl(
        override val impl: X509CertificateTrust
    ) : DocumentRetrievalConfig

    data class X509Certificates(
        val context: Context,
        val certificates: List<Int>,
        val shouldLog: Boolean = false
    ) : DocumentRetrievalConfig {
        override val impl: X509CertificateTrust
            get() = X509CertificateTrustMethod(
                trustedCertificates = certificates.mapNotNull {
                    context.getCertificate(it).getOrNull()
                },
                logException = if (shouldLog) {
                    { th: Throwable -> th.printStackTrace() }
                } else {
                    null
                }
            )
    }

    data object NoValidation : DocumentRetrievalConfig {
        override val impl: X509CertificateTrust?
            get() = null
    }
}