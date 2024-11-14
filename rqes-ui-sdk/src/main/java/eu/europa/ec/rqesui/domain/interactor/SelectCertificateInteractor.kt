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

import eu.europa.ec.eudi.rqes.core.RQESService.Authorized
import eu.europa.ec.rqesui.domain.controller.EudiRqesAuthorizeServicePartialState
import eu.europa.ec.rqesui.domain.controller.EudiRqesController
import eu.europa.ec.rqesui.domain.controller.EudiRqesCoreController
import eu.europa.ec.rqesui.domain.controller.EudiRqesGetCertificatesPartialState
import eu.europa.ec.rqesui.domain.controller.EudiRqesGetCredentialAuthorizationUrlPartialState
import eu.europa.ec.rqesui.domain.controller.EudiRqesGetSelectedFilePartialState
import eu.europa.ec.rqesui.infrastructure.config.data.CertificateData
import eu.europa.ec.rqesui.infrastructure.provider.ResourceProvider

internal interface SelectCertificateInteractor {
    suspend fun getCertificates(authorizedService: Authorized): EudiRqesGetCertificatesPartialState

    fun getSelectedFile(): EudiRqesGetSelectedFilePartialState

    fun updateCertificateUserSelection(certificate: CertificateData)

    suspend fun authorizeService(): EudiRqesAuthorizeServicePartialState

    fun setAuthorizedService(authorizedService: Authorized)

    fun getAuthorizedService(): Authorized?

    suspend fun getCredentialAuthorizationUrl(
        authorizedService: Authorized,
        certificateData: CertificateData,
    ): EudiRqesGetCredentialAuthorizationUrlPartialState
}

internal class SelectCertificateInteractorImpl(
    private val resourceProvider: ResourceProvider,
    private val eudiRqesController: EudiRqesController,
    private val eudiRqesCoreController: EudiRqesCoreController,
) : SelectCertificateInteractor {

    private val genericErrorMsg
        get() = resourceProvider.genericErrorMessage()

    override suspend fun getCertificates(authorizedService: Authorized): EudiRqesGetCertificatesPartialState {
        return eudiRqesController.getAvailableCertificates(authorizedService)
    }

    override fun getSelectedFile(): EudiRqesGetSelectedFilePartialState {
        return eudiRqesController.getSelectedFile()
    }

    override fun updateCertificateUserSelection(certificate: CertificateData) {
        eudiRqesController.updateCertificateUserSelection(certificate)
    }

    override suspend fun authorizeService(): EudiRqesAuthorizeServicePartialState {
        return eudiRqesController.authorizeService()
    }

    override fun setAuthorizedService(authorizedService: Authorized) {
        eudiRqesController.setAuthorizedService(authorizedService)
    }

    override fun getAuthorizedService(): Authorized? {
        return eudiRqesController.getAuthorizedService()
    }

    override suspend fun getCredentialAuthorizationUrl(
        authorizedService: Authorized,
        certificateData: CertificateData,
    ): EudiRqesGetCredentialAuthorizationUrlPartialState {
        return eudiRqesController.getCredentialAuthorizationUrl(authorizedService, certificateData)
    }
}