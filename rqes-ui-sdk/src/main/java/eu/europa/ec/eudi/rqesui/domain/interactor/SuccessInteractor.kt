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

package eu.europa.ec.eudi.rqesui.domain.interactor

import android.net.Uri
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesAuthorizeCredentialPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetSelectedFilePartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetSelectedQtspPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesSaveSignedDocumentsPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesSignDocumentsPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.RqesController
import eu.europa.ec.eudi.rqesui.domain.entities.error.EudiRQESUiError
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.QtspData
import eu.europa.ec.eudi.rqesui.infrastructure.provider.ResourceProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal interface SuccessInteractor {
    fun getSelectedFileAndQtsp(): SuccessInteractorGetSelectedFileAndQtspPartialState

    suspend fun signAndSaveDocument(originalDocumentName: String): SuccessInteractorSignAndSaveDocumentPartialState
}

internal class SuccessInteractorImpl(
    private val resourceProvider: ResourceProvider,
    private val eudiRqesController: RqesController,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SuccessInteractor {

    private val genericErrorMsg
        get() = resourceProvider.genericErrorMessage()

    private val genericErrorTitle
        get() = resourceProvider.genericErrorTitle()

    override fun getSelectedFileAndQtsp(): SuccessInteractorGetSelectedFileAndQtspPartialState {
        return runCatching {
            when (val getSelectedFileResponse = eudiRqesController.getSelectedFile()) {
                is EudiRqesGetSelectedFilePartialState.Failure -> {
                    return@runCatching SuccessInteractorGetSelectedFileAndQtspPartialState.Failure(
                        error = getSelectedFileResponse.error
                    )
                }

                is EudiRqesGetSelectedFilePartialState.Success -> {
                    when (val getSelectedQtspResponse = eudiRqesController.getSelectedQtsp()) {
                        is EudiRqesGetSelectedQtspPartialState.Failure -> {
                            return@runCatching SuccessInteractorGetSelectedFileAndQtspPartialState.Failure(
                                error = getSelectedQtspResponse.error
                            )
                        }

                        is EudiRqesGetSelectedQtspPartialState.Success -> {
                            return@runCatching SuccessInteractorGetSelectedFileAndQtspPartialState.Success(
                                selectedFile = getSelectedFileResponse.file,
                                selectedQtsp = getSelectedQtspResponse.qtsp,
                            )
                        }
                    }
                }
            }
        }.getOrElse {
            SuccessInteractorGetSelectedFileAndQtspPartialState.Failure(
                error = EudiRQESUiError(
                    title = genericErrorTitle,
                    message = it.localizedMessage ?: genericErrorMsg
                )
            )
        }
    }

    override suspend fun signAndSaveDocument(originalDocumentName: String): SuccessInteractorSignAndSaveDocumentPartialState {
        return withContext(dispatcher) {
            runCatching {
                when (val authorizeCredentialResponse = eudiRqesController.authorizeCredential()) {
                    is EudiRqesAuthorizeCredentialPartialState.Failure -> {
                        return@runCatching SuccessInteractorSignAndSaveDocumentPartialState.Failure(
                            error = authorizeCredentialResponse.error
                        )
                    }

                    is EudiRqesAuthorizeCredentialPartialState.Success -> {
                        val signDocumentsResponse = eudiRqesController.signDocuments(
                            authorizedCredential = authorizeCredentialResponse.authorizedCredential
                        )

                        when (signDocumentsResponse) {
                            is EudiRqesSignDocumentsPartialState.Failure -> {
                                return@runCatching SuccessInteractorSignAndSaveDocumentPartialState.Failure(
                                    error = signDocumentsResponse.error
                                )
                            }

                            is EudiRqesSignDocumentsPartialState.Success -> {
                                val saveSignedDocumentsResponse =
                                    eudiRqesController.saveSignedDocuments(
                                        originalDocumentName = originalDocumentName,
                                        signedDocuments = signDocumentsResponse.signedDocuments,
                                    )

                                when (saveSignedDocumentsResponse) {
                                    is EudiRqesSaveSignedDocumentsPartialState.Failure -> {
                                        return@runCatching SuccessInteractorSignAndSaveDocumentPartialState.Failure(
                                            error = saveSignedDocumentsResponse.error
                                        )
                                    }

                                    is EudiRqesSaveSignedDocumentsPartialState.Success -> {

                                        val signedDocument = saveSignedDocumentsResponse
                                            .savedDocuments.entries.first()

                                        val savedDocument = DocumentData(
                                            uri = signedDocument.value,
                                            documentName = signedDocument.key,
                                        )

                                        return@runCatching SuccessInteractorSignAndSaveDocumentPartialState.Success(
                                            savedDocument = savedDocument,
                                            isRemote = saveSignedDocumentsResponse.isRemote,
                                            redirectUri = saveSignedDocumentsResponse.redirectUri
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }.getOrElse {
                SuccessInteractorSignAndSaveDocumentPartialState.Failure(
                    error = EudiRQESUiError(
                        title = genericErrorTitle,
                        message = it.localizedMessage ?: genericErrorMsg
                    )
                )
            }
        }
    }
}

internal sealed class SuccessInteractorGetSelectedFileAndQtspPartialState {
    data class Success(
        val selectedFile: DocumentData,
        val selectedQtsp: QtspData,
    ) : SuccessInteractorGetSelectedFileAndQtspPartialState()

    data class Failure(
        val error: EudiRQESUiError
    ) : SuccessInteractorGetSelectedFileAndQtspPartialState()
}

internal sealed class SuccessInteractorSignAndSaveDocumentPartialState {
    data class Success(
        val savedDocument: DocumentData,
        val isRemote: Boolean,
        val redirectUri: Uri?
    ) : SuccessInteractorSignAndSaveDocumentPartialState()

    data class Failure(
        val error: EudiRQESUiError
    ) : SuccessInteractorSignAndSaveDocumentPartialState()
}