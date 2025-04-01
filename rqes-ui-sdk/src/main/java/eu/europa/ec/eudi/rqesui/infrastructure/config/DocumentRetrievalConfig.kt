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

package eu.europa.ec.eudi.rqesui.infrastructure.config

import android.content.Context
import eu.europa.ec.eudi.documentretrieval.X509CertificateTrust
import eu.europa.ec.eudi.rqesui.domain.extension.getCertificate
import eu.europa.ec.eudi.rqes.core.documentRetrieval.X509CertificateTrust as X509CertificateTrustMethod

/**
 * Configuration for document retrieval, specifically focusing on X.509 certificate validation.
 *
 * This interface allows for different strategies of trusting certificates when retrieving documents,
 * ranging from providing an explicit trust implementation to relying on a list of certificate resources
 * within an Android Context, or disabling validation entirely.  It also includes a flag to indicate
 * whether document integrity should be checked via hashing.
 */
sealed interface DocumentRetrievalConfig {

    val impl: X509CertificateTrust?
    val checkHash: Boolean

    data class X509CertificateImpl(
        override val impl: X509CertificateTrust,
        override val checkHash: Boolean = true
    ) : DocumentRetrievalConfig

    data class X509Certificates(
        val context: Context,
        val certificates: List<Int>,
        override val checkHash: Boolean = true,
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
        override val checkHash: Boolean
            get() = false
    }
}