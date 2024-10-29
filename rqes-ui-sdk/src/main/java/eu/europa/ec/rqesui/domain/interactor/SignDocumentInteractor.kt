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
import eu.europa.ec.rqesui.infrastructure.EudiRQESUi
import eu.europa.ec.rqesui.infrastructure.config.data.QTSPData
import eu.europa.ec.rqesui.infrastructure.provider.ResourceProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.URI

internal sealed class SignDocumentPartialState {
    data class Success(
        val signedDocumentUri: URI?
    ) : SignDocumentPartialState()

    data class SigningFailure(val error: String) : SignDocumentPartialState()
}

internal interface SignDocumentInteractor {

    fun getQTSPList(): List<QTSPData>
    fun getDocumentName(): String
    fun signPdfDocument(documentURI: URI): Flow<SignDocumentPartialState>
    fun updateQTSPUserSelection(qtspData: QTSPData)
}

internal class SignDocumentInteractorImpl(
    private val resourceProvider: ResourceProvider,
    private val rqesCoreController: Any? = null,
) : SignDocumentInteractor {

    private val genericErrorMsg
        get() = resourceProvider.genericErrorMessage()

    override fun getQTSPList(): List<QTSPData> {
        return EudiRQESUi.getEudiRQESUiConfig().qtsps
    }

    override fun getDocumentName(): String {
        return when (val sdkState = EudiRQESUi.getState()) {
            is EudiRQESUi.State.Initial -> {
                sdkState.file.documentName
            }

            else -> ""
        }
    }

    override fun signPdfDocument(documentURI: URI): Flow<SignDocumentPartialState> =
        flow {
            emit(SignDocumentPartialState.Success(signedDocumentUri = null))
        }.safeAsync {
            SignDocumentPartialState.SigningFailure(
                error = it.localizedMessage ?: genericErrorMsg
            )
        }

    override fun updateQTSPUserSelection(qtspData: QTSPData) {
        TODO("Not yet implemented")
    }
}