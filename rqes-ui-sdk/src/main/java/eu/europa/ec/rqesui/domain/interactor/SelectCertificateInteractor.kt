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

package eu.europa.ec.rqesui.domain.interactor

import eu.europa.ec.rqesui.domain.controller.EudiRqesAuthorizeServicePartialState
import eu.europa.ec.rqesui.domain.controller.EudiRqesController
import eu.europa.ec.rqesui.domain.controller.EudiRqesGetCertificatesPartialState
import eu.europa.ec.rqesui.domain.controller.EudiRqesGetCredentialAuthorizationUrlPartialState
import eu.europa.ec.rqesui.domain.controller.EudiRqesGetSelectedFilePartialState
import eu.europa.ec.rqesui.domain.entities.error.EudiRQESUiError
import eu.europa.ec.rqesui.infrastructure.config.data.CertificateData
import eu.europa.ec.rqesui.infrastructure.provider.ResourceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal interface SelectCertificateInteractor {
    fun getSelectedFile(): EudiRqesGetSelectedFilePartialState

    suspend fun authorizeServiceAndFetchCertificates(): SelectCertificateInteractorAuthorizeServiceAndFetchCertificatesPartialState

    suspend fun getCredentialAuthorizationUrl(certificate: CertificateData): EudiRqesGetCredentialAuthorizationUrlPartialState
}

internal class SelectCertificateInteractorImpl(
    private val resourceProvider: ResourceProvider,
    private val eudiRqesController: EudiRqesController,
) : SelectCertificateInteractor {

    private val genericErrorMsg
        get() = resourceProvider.genericErrorMessage()

    override fun getSelectedFile(): EudiRqesGetSelectedFilePartialState {
        return eudiRqesController.getSelectedFile()
    }

    override suspend fun authorizeServiceAndFetchCertificates(): SelectCertificateInteractorAuthorizeServiceAndFetchCertificatesPartialState {
        return withContext(Dispatchers.IO) {
            runCatching {
                when (val authorizeServiceResponse = eudiRqesController.authorizeService()) {
                    is EudiRqesAuthorizeServicePartialState.Failure -> {
                        return@runCatching SelectCertificateInteractorAuthorizeServiceAndFetchCertificatesPartialState.Failure(
                            error = authorizeServiceResponse.error
                        )
                    }

                    is EudiRqesAuthorizeServicePartialState.Success -> {
                        val getCertificatesResponse = eudiRqesController.getAvailableCertificates(
                            authorizedService = authorizeServiceResponse.authorizedService
                        )
                        when (getCertificatesResponse) {
                            is EudiRqesGetCertificatesPartialState.Failure -> {
                                return@runCatching SelectCertificateInteractorAuthorizeServiceAndFetchCertificatesPartialState.Failure(
                                    error = getCertificatesResponse.error
                                )
                            }

                            is EudiRqesGetCertificatesPartialState.Success -> {
                                eudiRqesController.setAuthorizedService(authorizedService = authorizeServiceResponse.authorizedService)
                                return@runCatching SelectCertificateInteractorAuthorizeServiceAndFetchCertificatesPartialState.Success(
                                    certificates = getCertificatesResponse.certificates
                                )
                            }
                        }
                    }
                }
            }.getOrElse {
                SelectCertificateInteractorAuthorizeServiceAndFetchCertificatesPartialState.Failure(
                    error = EudiRQESUiError(
                        message = it.localizedMessage ?: genericErrorMsg
                    )
                )
            }
        }
    }

    override suspend fun getCredentialAuthorizationUrl(certificate: CertificateData): EudiRqesGetCredentialAuthorizationUrlPartialState {
        return withContext(Dispatchers.IO) {
            runCatching {
                val authorizedService = eudiRqesController.getAuthorizedService()
                authorizedService?.let { safeAuthorizedService ->
                    val getCredentialAuthorizationUrlResponse =
                        eudiRqesController.getCredentialAuthorizationUrl(
                            authorizedService = safeAuthorizedService,
                            certificateData = certificate,
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
}

internal sealed class SelectCertificateInteractorAuthorizeServiceAndFetchCertificatesPartialState {
    data class Success(
        val certificates: List<CertificateData>,
    ) : SelectCertificateInteractorAuthorizeServiceAndFetchCertificatesPartialState()

    data class Failure(
        val error: EudiRQESUiError
    ) : SelectCertificateInteractorAuthorizeServiceAndFetchCertificatesPartialState()
}