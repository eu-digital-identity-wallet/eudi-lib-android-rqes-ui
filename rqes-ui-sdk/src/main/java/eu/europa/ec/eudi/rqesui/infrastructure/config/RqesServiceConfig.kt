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

import eu.europa.ec.eudi.rqes.HashAlgorithmOID
import eu.europa.ec.eudi.rqes.SigningAlgorithmOID
import java.net.URI

/**
 * Configuration data class for the RQES service.
 *
 * This class holds the necessary configuration parameters for interacting with the RQES service,
 * including client credentials, redirection URI, and cryptographic algorithm specifications.
 *
 * @property clientId The client ID used to identify the application with the RQES service.
 * @property clientSecret The client secret used for authentication with the RQES service.
 * @property authFlowRedirectionURI The [URI] to which the user is redirected after authenticating with the RQES service.
 * @property signingAlgorithm The signing algorithm used for generating digital signatures. Defaults to RSA.
 * @property hashAlgorithm The hash algorithm used for generating message digests. Defaults to SHA-256.
 */
data class RqesServiceConfig(
    val clientId: String,
    val clientSecret: String,
    val authFlowRedirectionURI: URI,
    val signingAlgorithm: SigningAlgorithmOID = SigningAlgorithmOID.RSA,
    val hashAlgorithm: HashAlgorithmOID = HashAlgorithmOID.SHA_256,
)