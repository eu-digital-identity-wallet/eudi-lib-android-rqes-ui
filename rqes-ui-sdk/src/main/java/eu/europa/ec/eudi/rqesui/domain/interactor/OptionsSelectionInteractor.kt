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

import eu.europa.ec.eudi.rqes.core.RQESService
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesAuthorizeServicePartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetCertificatesPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetCredentialAuthorizationUrlPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetQtspsPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetSelectedFilePartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetSelectedQtspPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetServiceAuthorizationUrlPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesSetSelectedQtspPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.RqesController
import eu.europa.ec.eudi.rqesui.domain.entities.error.EudiRQESUiError
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.CertificateData
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.QtspData
import eu.europa.ec.eudi.rqesui.infrastructure.provider.ResourceProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal sealed class OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState {
    data class Success(
        val certificates: List<CertificateData>,
    ) : OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState()

    data class Failure(
        val error: EudiRQESUiError
    ) : OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState()
}

internal sealed class OptionsSelectionInteractorGetSelectedQtspPartialState {
    data class Success(
        val selectedQtsp: QtspData,
    ) : OptionsSelectionInteractorGetSelectedQtspPartialState()

    data class Failure(
        val error: EudiRQESUiError
    ) : OptionsSelectionInteractorGetSelectedQtspPartialState()
}

internal interface OptionsSelectionInteractor {
    fun getQtsps(): EudiRqesGetQtspsPartialState

    fun getSelectedFile(): EudiRqesGetSelectedFilePartialState

    fun updateQtspUserSelection(qtspData: QtspData): EudiRqesSetSelectedQtspPartialState

    fun getSelectedQtsp(): OptionsSelectionInteractorGetSelectedQtspPartialState

    suspend fun getServiceAuthorizationUrl(rqesService: RQESService): EudiRqesGetServiceAuthorizationUrlPartialState

    suspend fun getCredentialAuthorizationUrl(certificateData: CertificateData): EudiRqesGetCredentialAuthorizationUrlPartialState

    suspend fun authorizeServiceAndFetchCertificates(): OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState
}

internal class OptionsSelectionInteractorImpl(
    private val eudiRqesController: RqesController,
    private val resourceProvider: ResourceProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : OptionsSelectionInteractor {

    private val genericErrorMsg
        get() = resourceProvider.genericErrorMessage()

    override fun getQtsps(): EudiRqesGetQtspsPartialState {
        return eudiRqesController.getQtsps()
    }

    override fun getSelectedFile(): EudiRqesGetSelectedFilePartialState {
        return eudiRqesController.getSelectedFile()
    }

    override fun updateQtspUserSelection(qtspData: QtspData): EudiRqesSetSelectedQtspPartialState {
        return eudiRqesController.setSelectedQtsp(qtspData)
    }

    override suspend fun getServiceAuthorizationUrl(rqesService: RQESService): EudiRqesGetServiceAuthorizationUrlPartialState {
        return eudiRqesController.getServiceAuthorizationUrl(rqesService)
    }

    override suspend fun authorizeServiceAndFetchCertificates(): OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState {
        return withContext(dispatcher) {
            runCatching {
                when (val authorizeServiceResponse = eudiRqesController.authorizeService()) {
                    is EudiRqesAuthorizeServicePartialState.Failure -> {
                        return@runCatching OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState.Failure(
                            error = authorizeServiceResponse.error
                        )
                    }

                    is EudiRqesAuthorizeServicePartialState.Success -> {
                        val getCertificatesResponse = eudiRqesController.getAvailableCertificates(
                            authorizedService = authorizeServiceResponse.authorizedService
                        )
                        when (getCertificatesResponse) {
                            is EudiRqesGetCertificatesPartialState.Failure -> {
                                return@runCatching OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState.Failure(
                                    error = getCertificatesResponse.error
                                )
                            }

                            is EudiRqesGetCertificatesPartialState.Success -> {
                                eudiRqesController.setAuthorizedService(authorizedService = authorizeServiceResponse.authorizedService)
                                return@runCatching OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState.Success(
                                    certificates = getCertificatesResponse.certificates
                                )
                            }
                        }
                    }
                }
            }.getOrElse {
                OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState.Failure(
                    error = EudiRQESUiError(
                        message = it.localizedMessage ?: genericErrorMsg
                    )
                )
            }
        }
    }

    override suspend fun getCredentialAuthorizationUrl(certificateData: CertificateData): EudiRqesGetCredentialAuthorizationUrlPartialState {
        return withContext(dispatcher) {
            runCatching {
                val authorizedService = eudiRqesController.getAuthorizedService()
                authorizedService?.let { safeAuthorizedService ->
                    val getCredentialAuthorizationUrlResponse =
                        eudiRqesController.getCredentialAuthorizationUrl(
                            authorizedService = safeAuthorizedService,
                            certificateData = certificateData,
                        )
                    when (getCredentialAuthorizationUrlResponse) {
                        is EudiRqesGetCredentialAuthorizationUrlPartialState.Failure -> {
                            return@let EudiRqesGetCredentialAuthorizationUrlPartialState.Failure(
                                error = getCredentialAuthorizationUrlResponse.error
                            )
                        }

                        is EudiRqesGetCredentialAuthorizationUrlPartialState.Success -> {
                            return@let EudiRqesGetCredentialAuthorizationUrlPartialState.Success(
                                authorizationUrl = getCredentialAuthorizationUrlResponse.authorizationUrl
                            )
                        }
                    }
                } ?: return@runCatching EudiRqesGetCredentialAuthorizationUrlPartialState.Failure(
                    error = EudiRQESUiError(message = genericErrorMsg)
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

    override fun getSelectedQtsp(): OptionsSelectionInteractorGetSelectedQtspPartialState {
        return runCatching {
            when (val getSelectedQtspResponse = eudiRqesController.getSelectedQtsp()) {
                is EudiRqesGetSelectedQtspPartialState.Failure -> {
                    return@runCatching OptionsSelectionInteractorGetSelectedQtspPartialState.Failure(
                        error = getSelectedQtspResponse.error
                    )
                }

                is EudiRqesGetSelectedQtspPartialState.Success -> {
                    return@runCatching OptionsSelectionInteractorGetSelectedQtspPartialState.Success(
                        selectedQtsp = getSelectedQtspResponse.qtsp,
                    )
                }
            }
        }.getOrElse {
            OptionsSelectionInteractorGetSelectedQtspPartialState.Failure(
                error = EudiRQESUiError(
                    message = it.localizedMessage ?: genericErrorMsg
                )
            )
        }
    }
}