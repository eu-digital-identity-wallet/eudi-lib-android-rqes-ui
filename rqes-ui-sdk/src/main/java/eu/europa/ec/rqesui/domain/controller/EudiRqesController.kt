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

package eu.europa.ec.rqesui.domain.controller

import android.net.Uri
import eu.europa.ec.eudi.rqes.AuthorizationCode
import eu.europa.ec.eudi.rqes.CSCClientConfig
import eu.europa.ec.eudi.rqes.OAuth2Client
import eu.europa.ec.eudi.rqes.SigningAlgorithmOID
import eu.europa.ec.eudi.rqes.core.RQESService
import eu.europa.ec.eudi.rqes.core.RQESService.Authorized
import eu.europa.ec.eudi.rqes.core.SignedDocuments
import eu.europa.ec.eudi.rqes.core.UnsignedDocument
import eu.europa.ec.eudi.rqes.core.UnsignedDocuments
import eu.europa.ec.rqesui.domain.entities.error.EudiRQESUiError
import eu.europa.ec.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.rqesui.domain.extension.toUri
import eu.europa.ec.rqesui.domain.helper.FileHelper.saveBase64DecodedPdfToShareableUri
import eu.europa.ec.rqesui.domain.helper.FileHelper.uriToFile
import eu.europa.ec.rqesui.domain.util.safeLet
import eu.europa.ec.rqesui.infrastructure.EudiRQESUi
import eu.europa.ec.rqesui.infrastructure.config.data.CertificateData
import eu.europa.ec.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.rqesui.infrastructure.config.data.QtspData
import eu.europa.ec.rqesui.infrastructure.config.data.toCertificatesData
import eu.europa.ec.rqesui.infrastructure.provider.ResourceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.URL

internal interface EudiRqesController {
    fun getSelectedFile(): EudiRqesGetSelectedFilePartialState

    fun getQtsps(): EudiRqesGetQtspsPartialState

    fun updateQtspUserSelection(qtspData: QtspData): EudiRqesSetSelectedQtspPartialState

    suspend fun getAuthorizationServiceUrl(reqRQESService: RQESService): EudiRqesGetServiceAuthorizationUrlPartialState

    fun getSelectedQtsp(): EudiRqesGetSelectedQtspPartialState

    suspend fun authorizeService(): EudiRqesAuthorizeServicePartialState

    fun setAuthorizedService(authorizedService: Authorized)

    fun getAuthorizedService(): Authorized?

    suspend fun getAvailableCertificates(authorizedService: Authorized): EudiRqesGetCertificatesPartialState

    fun updateCertificateUserSelection(certificate: CertificateData)

    suspend fun getCredentialAuthorizationUrl(
        authorizedService: Authorized,
        certificateData: CertificateData,
    ): EudiRqesGetCredentialAuthorizationUrlPartialState

    suspend fun authorizeCredential(): EudiRqesAuthorizeCredentialPartialState

    suspend fun signDocuments(authorizedCredential: RQESService.CredentialAuthorized): EudiRqesSignDocumentsPartialState

    suspend fun saveSignedDocuments(
        unsignedDocumentName: String,
        signedDocuments: SignedDocuments,
    ): EudiRqesSaveSignedDocumentsPartialState
}

internal class EudiRqesControllerImpl(
    private val eudiRQESUi: EudiRQESUi,
    private val resourceProvider: ResourceProvider,
) : EudiRqesController {

    private val genericErrorMsg
        get() = resourceProvider.genericErrorMessage()

    override fun getSelectedFile(): EudiRqesGetSelectedFilePartialState {
        return runCatching {
            val selectedFile = eudiRQESUi.currentSelection.file
            selectedFile?.let { safeSelectedFile ->
                EudiRqesGetSelectedFilePartialState.Success(file = safeSelectedFile)
            } ?: EudiRqesGetSelectedFilePartialState.Failure(
                error = EudiRQESUiError(
                    message = resourceProvider.getLocalizedString(LocalizableKey.GenericErrorDocumentNotFound)
                )
            )
        }.getOrElse {
            EudiRqesGetSelectedFilePartialState.Failure(
                error = EudiRQESUiError(
                    message = it.localizedMessage ?: genericErrorMsg
                )
            )
        }
    }

    override fun getQtsps(): EudiRqesGetQtspsPartialState {
        return runCatching {
            EudiRqesGetQtspsPartialState.Success(qtsps = eudiRQESUi.getEudiRQESUiConfig().qtsps)
        }.getOrElse {
            EudiRqesGetQtspsPartialState.Failure(
                error = EudiRQESUiError(
                    message = it.localizedMessage ?: genericErrorMsg
                )
            )
        }
    }

    override fun getSelectedQtsp(): EudiRqesGetSelectedQtspPartialState {
        return runCatching {
            val selectedQtsp = eudiRQESUi.currentSelection.qtsp
            selectedQtsp?.let { safeSelectedQtsp ->
                EudiRqesGetSelectedQtspPartialState.Success(qtsp = safeSelectedQtsp)
            } ?: EudiRqesGetSelectedQtspPartialState.Failure(
                error = EudiRQESUiError(
                    message = resourceProvider.getLocalizedString(LocalizableKey.GenericErrorQtspNotFound)
                )
            )
        }.getOrElse {
            EudiRqesGetSelectedQtspPartialState.Failure(
                error = EudiRQESUiError(
                    message = it.localizedMessage ?: genericErrorMsg
                )
            )
        }
    }

    override fun updateQtspUserSelection(qtspData: QtspData): EudiRqesSetSelectedQtspPartialState {
        return runCatching {
            eudiRQESUi.currentSelection = eudiRQESUi.currentSelection.copy(
                qtsp = qtspData
            )

            when (val result = createRqesService(qtspData)) {
                is EudiRqesCreateServicePartialState.Failure -> {
                    EudiRqesSetSelectedQtspPartialState.Failure(error = result.error)
                }

                is EudiRqesCreateServicePartialState.Success -> {
                    EudiRqesSetSelectedQtspPartialState.Success(service = result.service)
                }
            }
        }.getOrElse {
            EudiRqesSetSelectedQtspPartialState.Failure(
                error = EudiRQESUiError(
                    message = it.localizedMessage ?: genericErrorMsg
                )
            )
        }
    }

    override suspend fun getAuthorizationServiceUrl(reqRQESService: RQESService): EudiRqesGetServiceAuthorizationUrlPartialState {
        return withContext(Dispatchers.IO) {
            runCatching {
                val authorizationUrl = reqRQESService.getServiceAuthorizationUrl()
                    .getOrThrow()
                    .value.toString().toUri()
                EudiRqesGetServiceAuthorizationUrlPartialState.Success(authorizationUrl = authorizationUrl)
            }.getOrElse {
                EudiRqesGetServiceAuthorizationUrlPartialState.Failure(
                    error = EudiRQESUiError(
                        message = it.localizedMessage ?: genericErrorMsg
                    )
                )
            }
        }
    }

    override suspend fun authorizeService(): EudiRqesAuthorizeServicePartialState {
        return withContext(Dispatchers.IO) {
            runCatching {
                safeLet(
                    eudiRQESUi.rqesService,
                    eudiRQESUi.currentSelection.authorizationCode
                ) { safeService, safeAuthorizationCode ->
                    val authorizedService = safeService.authorizeService(
                        authorizationCode = AuthorizationCode(safeAuthorizationCode)
                    ).getOrThrow()

                    EudiRqesAuthorizeServicePartialState.Success(authorizedService = authorizedService)
                } ?: EudiRqesAuthorizeServicePartialState.Failure(
                    error = EudiRQESUiError(
                        message = genericErrorMsg
                    )
                )
            }.getOrElse {
                EudiRqesAuthorizeServicePartialState.Failure(
                    error = EudiRQESUiError(
                        message = it.localizedMessage ?: genericErrorMsg
                    )
                )
            }
        }
    }

    override fun setAuthorizedService(authorizedService: Authorized) {
        eudiRQESUi.authorizedService = authorizedService
    }

    override fun getAuthorizedService(): Authorized? {
        return eudiRQESUi.authorizedService
    }

    override suspend fun getAvailableCertificates(authorizedService: Authorized): EudiRqesGetCertificatesPartialState {
        return withContext(Dispatchers.IO) {
            runCatching {
                val certificates = authorizedService.listCredentials()
                    .getOrThrow()
                    .toCertificatesData(
                        createDefaultName = { certificateIndex: Int ->
                            resourceProvider.getLocalizedString(
                                localizableKey = LocalizableKey.Certificate,
                                args = listOf((certificateIndex + 1).toString())
                            )
                        }
                    )
                if (certificates.isNotEmpty()) {
                    EudiRqesGetCertificatesPartialState.Success(certificates = certificates)
                } else {
                    EudiRqesGetCertificatesPartialState.Failure(
                        error = EudiRQESUiError(
                            message = resourceProvider.getLocalizedString(LocalizableKey.GenericErrorCertificatesNotFound)
                        )
                    )
                }
            }.getOrElse {
                EudiRqesGetCertificatesPartialState.Failure(
                    error = EudiRQESUiError(
                        message = it.localizedMessage ?: genericErrorMsg
                    )
                )
            }
        }
    }

    override fun updateCertificateUserSelection(certificate: CertificateData) {
        eudiRQESUi.currentSelection = eudiRQESUi.currentSelection.copy(
            certificate = certificate
        )
    }

    override suspend fun getCredentialAuthorizationUrl(
        authorizedService: Authorized,
        certificateData: CertificateData
    ): EudiRqesGetCredentialAuthorizationUrlPartialState {
        return withContext(Dispatchers.IO) {
            runCatching {
                eudiRQESUi.currentSelection.file?.let { safeSelectedFile ->

                    val fileToBeSigned = uriToFile(
                        context = resourceProvider.provideContext(),
                        uri = safeSelectedFile.uri,
                        fileName = safeSelectedFile.documentName
                    ).getOrThrow()

                    // Prepare the documents to sign
                    val unsignedDocuments = UnsignedDocuments(
                        UnsignedDocument(
                            label = safeSelectedFile.documentName,
                            file = fileToBeSigned,
                        )
                    )

                    val authorizationUrl = authorizedService
                        .getCredentialAuthorizationUrl(
                            credential = certificateData.certificate,
                            documents = unsignedDocuments,
                        ).getOrThrow()
                        .value.toString().toUri()

                    EudiRqesGetCredentialAuthorizationUrlPartialState.Success(authorizationUrl = authorizationUrl)
                } ?: EudiRqesGetCredentialAuthorizationUrlPartialState.Failure(
                    error = EudiRQESUiError(
                        message = resourceProvider.getLocalizedString(
                            LocalizableKey.GenericErrorDocumentNotFound
                        )
                    )
                )
            }.getOrElse {
                EudiRqesGetCredentialAuthorizationUrlPartialState.Failure(
                    error = EudiRQESUiError(
                        message = it.localizedMessage ?: genericErrorMsg
                    )
                )
            }
        }
    }

    override suspend fun authorizeCredential(): EudiRqesAuthorizeCredentialPartialState {
        return withContext(Dispatchers.IO) {
            runCatching {
                safeLet(
                    eudiRQESUi.authorizedService,
                    eudiRQESUi.currentSelection.authorizationCode
                ) { safeAuthorizedService, safeAuthorizationCode ->
                    val authorizedCredential: RQESService.CredentialAuthorized =
                        safeAuthorizedService.authorizeCredential(
                            authorizationCode = AuthorizationCode(safeAuthorizationCode)
                        ).getOrThrow()

                    EudiRqesAuthorizeCredentialPartialState.Success(authorizedCredential = authorizedCredential)
                } ?: EudiRqesAuthorizeCredentialPartialState.Failure(
                    error = EudiRQESUiError(
                        message = genericErrorMsg
                    )
                )
            }.getOrElse {
                EudiRqesAuthorizeCredentialPartialState.Failure(
                    error = EudiRQESUiError(
                        message = it.localizedMessage ?: genericErrorMsg
                    )
                )
            }
        }
    }

    override suspend fun signDocuments(authorizedCredential: RQESService.CredentialAuthorized): EudiRqesSignDocumentsPartialState {
        return withContext(Dispatchers.IO) {
            runCatching {
                val signedDocuments = authorizedCredential.signDocuments().getOrThrow()
                EudiRqesSignDocumentsPartialState.Success(signedDocuments = signedDocuments)
            }.getOrElse {
                EudiRqesSignDocumentsPartialState.Failure(
                    error = EudiRQESUiError(
                        message = it.localizedMessage ?: genericErrorMsg
                    )
                )
            }
        }
    }

    override suspend fun saveSignedDocuments(
        unsignedDocumentName: String,
        signedDocuments: SignedDocuments,
    ): EudiRqesSaveSignedDocumentsPartialState {
        return withContext(Dispatchers.IO) {
            runCatching {
                val uris = mutableListOf<Uri>()

                signedDocuments.forEachIndexed { index, inputStream ->
                    val uri = saveBase64DecodedPdfToShareableUri(
                        context = resourceProvider.provideContext(),
                        inputStream = inputStream,
                        fileName = "signed_${index}_${unsignedDocumentName}"
                    ).getOrThrow()

                    uris.add(uri)
                }

                if (uris.isNotEmpty()) {
                    EudiRqesSaveSignedDocumentsPartialState.Success(savedDocumentsUri = uris)
                } else {
                    EudiRqesSaveSignedDocumentsPartialState.Failure(
                        error = EudiRQESUiError(message = genericErrorMsg)
                    )
                }
            }.getOrElse {
                EudiRqesSaveSignedDocumentsPartialState.Failure(
                    error = EudiRQESUiError(
                        message = it.localizedMessage ?: genericErrorMsg
                    )
                )
            }
        }
    }

    private fun createRqesService(qtspData: QtspData): EudiRqesCreateServicePartialState {
        return runCatching {
            val service = RQESService(
                serviceEndpointUrl = qtspData.endpoint.toString(),
                config = CSCClientConfig(
                    client = OAuth2Client.Confidential.ClientSecretBasic(
                        clientId = "wallet-client-tester", //TODO remove them later?
                        clientSecret = "somesecrettester2"//TODO remove them later?
                    ),
                    authFlowRedirectionURI = URI("rQES://oauth/callback"),
                    scaBaseURL = URL(qtspData.scaUrl),
                ),
                signingAlgorithm = SigningAlgorithmOID.RSA,
            )

            eudiRQESUi.rqesService = service

            EudiRqesCreateServicePartialState.Success(service = service)
        }.getOrElse {
            EudiRqesCreateServicePartialState.Failure(
                error = EudiRQESUiError(
                    message = it.localizedMessage ?: genericErrorMsg
                )
            )
        }
    }
}

internal sealed class EudiRqesGetSelectedFilePartialState {
    data class Success(val file: DocumentData) : EudiRqesGetSelectedFilePartialState()
    data class Failure(val error: EudiRQESUiError) : EudiRqesGetSelectedFilePartialState()
}

internal sealed class EudiRqesGetQtspsPartialState {
    data class Success(val qtsps: List<QtspData>) : EudiRqesGetQtspsPartialState()
    data class Failure(val error: EudiRQESUiError) : EudiRqesGetQtspsPartialState()
}

internal sealed class EudiRqesSetSelectedQtspPartialState {
    data class Success(val service: RQESService) : EudiRqesSetSelectedQtspPartialState()
    data class Failure(val error: EudiRQESUiError) : EudiRqesSetSelectedQtspPartialState()
}

internal sealed class EudiRqesGetServiceAuthorizationUrlPartialState {
    data class Success(val authorizationUrl: Uri) : EudiRqesGetServiceAuthorizationUrlPartialState()
    data class Failure(
        val error: EudiRQESUiError
    ) : EudiRqesGetServiceAuthorizationUrlPartialState()
}

internal sealed class EudiRqesGetSelectedQtspPartialState {
    data class Success(val qtsp: QtspData) : EudiRqesGetSelectedQtspPartialState()
    data class Failure(val error: EudiRQESUiError) : EudiRqesGetSelectedQtspPartialState()
}

internal sealed class EudiRqesAuthorizeServicePartialState {
    data class Success(val authorizedService: Authorized) : EudiRqesAuthorizeServicePartialState()
    data class Failure(val error: EudiRQESUiError) : EudiRqesAuthorizeServicePartialState()
}

internal sealed class EudiRqesCreateServicePartialState {
    data class Success(val service: RQESService) : EudiRqesCreateServicePartialState()
    data class Failure(val error: EudiRQESUiError) : EudiRqesCreateServicePartialState()
}

internal sealed class EudiRqesGetCertificatesPartialState {
    data class Success(
        val certificates: List<CertificateData>,
    ) : EudiRqesGetCertificatesPartialState()

    data class Failure(val error: EudiRQESUiError) : EudiRqesGetCertificatesPartialState()
}

internal sealed class EudiRqesGetCredentialAuthorizationUrlPartialState {
    data class Success(
        val authorizationUrl: Uri,
    ) : EudiRqesGetCredentialAuthorizationUrlPartialState()

    data class Failure(
        val error: EudiRQESUiError
    ) : EudiRqesGetCredentialAuthorizationUrlPartialState()
}

internal sealed class EudiRqesAuthorizeCredentialPartialState {
    data class Success(
        val authorizedCredential: RQESService.CredentialAuthorized,
    ) : EudiRqesAuthorizeCredentialPartialState()

    data class Failure(val error: EudiRQESUiError) : EudiRqesAuthorizeCredentialPartialState()
}

internal sealed class EudiRqesSignDocumentsPartialState {
    data class Success(val signedDocuments: SignedDocuments) : EudiRqesSignDocumentsPartialState()
    data class Failure(val error: EudiRQESUiError) : EudiRqesSignDocumentsPartialState()
}

internal sealed class EudiRqesSaveSignedDocumentsPartialState {
    data class Success(val savedDocumentsUri: List<Uri>) : EudiRqesSaveSignedDocumentsPartialState()
    data class Failure(val error: EudiRQESUiError) : EudiRqesSaveSignedDocumentsPartialState()
}