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

import android.net.Uri
import eu.europa.ec.rqesui.domain.extension.safeAsync
import eu.europa.ec.rqesui.domain.extension.toUri
import eu.europa.ec.rqesui.infrastructure.EudiRQESUi
import eu.europa.ec.rqesui.infrastructure.config.data.CertificateData
import eu.europa.ec.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.rqesui.infrastructure.config.data.QTSPData
import eu.europa.ec.rqesui.infrastructure.provider.ResourceProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal sealed class SelectCertificatePartialState {
    data class Success(
        val qtspCertificatesList: List<CertificateData>
    ) : SelectCertificatePartialState()

    data class Failure(val error: String) : SelectCertificatePartialState()
}

internal interface SelectCertificateInteractor {
    fun qtspCertificates(qtspCertificateEndpoint: Uri): Flow<SelectCertificatePartialState>
    fun getDocumentData(): DocumentData
    fun getQtspData(): QTSPData
}

internal class SelectCertificateInteractorImpl(
    private val resourceProvider: ResourceProvider,
    //TODO change this when integration with Core is ready.
    private val rqesCoreController: Any? = null,
) : SelectCertificateInteractor {

    private val genericErrorMsg
        get() = resourceProvider.genericErrorMessage()

    override fun qtspCertificates(qtspCertificateEndpoint: Uri): Flow<SelectCertificatePartialState> =
        flow {
            emit(
                SelectCertificatePartialState.Success(
                    qtspCertificatesList = listOf(
                        CertificateData(name = "Certificate 1", certificateURI = "uri1".toUri()),
                        CertificateData(name = "Certificate 2", certificateURI = "uri2".toUri()),
                        CertificateData(name = "Certificate 3", certificateURI = "uri3".toUri()),
                    )
                )
            )
        }.safeAsync {
            SelectCertificatePartialState.Failure(
                error = it.localizedMessage ?: genericErrorMsg
            )
        }

    override fun getDocumentData(): DocumentData {
        return EudiRQESUi.file
    }

    override fun getQtspData(): QTSPData {
        return EudiRQESUi.qtsp
    }
}