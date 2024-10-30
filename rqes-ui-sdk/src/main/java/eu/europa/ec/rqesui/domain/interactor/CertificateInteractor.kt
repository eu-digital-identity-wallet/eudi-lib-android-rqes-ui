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

import eu.europa.ec.rqesui.domain.extension.safeAsync
import eu.europa.ec.rqesui.infrastructure.config.data.CertificateData
import eu.europa.ec.rqesui.infrastructure.provider.ResourceProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.URI

internal sealed class CertificatePartialState {
    data class Success(
        val qtspCertificatesList: List<CertificateData>
    ) : CertificatePartialState()

    data class Failure(val error: String) : CertificatePartialState()
}

internal interface CertificateInteractor {
    fun qtspCertificates(qtspCertificateEndpoint: URI): Flow<CertificatePartialState>
    fun signDocument(documentUri: URI)
}

internal class CertificateInteractorImpl(
    private val resourceProvider: ResourceProvider,
    private val rqesCoreController: Any? = null
) : CertificateInteractor {

    private val genericErrorMsg
        get() = resourceProvider.genericErrorMessage()

    override fun qtspCertificates(qtspCertificateEndpoint: URI): Flow<CertificatePartialState> =
        flow {
            emit(
                CertificatePartialState.Success(
                    qtspCertificatesList =
                    listOf(
                        CertificateData(name = "Certificate 1", certificateURI = URI("uri")),
                        CertificateData(name = "Certificate 2", certificateURI = URI("uri"))
                    )

                )
            )
        }.safeAsync {
            CertificatePartialState.Failure(
                error = it.localizedMessage ?: genericErrorMsg
            )
        }

    override fun signDocument(documentUri: URI) {
        // TODO implement sign document functionality
    }
}