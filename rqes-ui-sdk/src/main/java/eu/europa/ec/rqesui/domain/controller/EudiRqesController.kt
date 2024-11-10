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

import eu.europa.ec.rqesui.R
import eu.europa.ec.rqesui.domain.entities.error.EudiRQESUiError
import eu.europa.ec.rqesui.infrastructure.EudiRQESUi
import eu.europa.ec.rqesui.infrastructure.config.data.CertificateData
import eu.europa.ec.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.rqesui.infrastructure.config.data.QtspData
import eu.europa.ec.rqesui.infrastructure.provider.ResourceProvider

internal sealed class EudiRqesGetQtspsPartialState {
    data class Success(val qtsps: List<QtspData>) : EudiRqesGetQtspsPartialState()
    data class Failure(val error: EudiRQESUiError) : EudiRqesGetQtspsPartialState()
}

internal sealed class EudiRqesGetSelectedQtspPartialState {
    data class Success(val qtsp: QtspData) : EudiRqesGetSelectedQtspPartialState()
    data class Failure(val error: EudiRQESUiError) : EudiRqesGetSelectedQtspPartialState()
}

internal sealed class EudiRqesGetSelectedFilePartialState {
    data class Success(val file: DocumentData) : EudiRqesGetSelectedFilePartialState()
    data class Failure(val error: EudiRQESUiError) : EudiRqesGetSelectedFilePartialState()
}

internal interface EudiRqesController {
    fun getQtsps(): EudiRqesGetQtspsPartialState

    fun getSelectedQtsp(): EudiRqesGetSelectedQtspPartialState

    fun getSelectedFile(): EudiRqesGetSelectedFilePartialState

    fun updateQtspUserSelection(qtspData: QtspData)

    fun updateCertificateUserSelection(certificateData: CertificateData)
}

internal class EudiRqesControllerImpl(
    private val eudiRQESUi: EudiRQESUi,
    private val resourceProvider: ResourceProvider,
) : EudiRqesController {

    private val genericErrorMsg
        get() = resourceProvider.genericErrorMessage()

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
                    message = resourceProvider.getString(R.string.generic_error_qtsp_not_found)
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

    override fun getSelectedFile(): EudiRqesGetSelectedFilePartialState {
        return runCatching {
            val selectedFile = eudiRQESUi.currentSelection.file
            selectedFile?.let { safeSelectedFile ->
                EudiRqesGetSelectedFilePartialState.Success(file = safeSelectedFile)
            } ?: EudiRqesGetSelectedFilePartialState.Failure(
                error = EudiRQESUiError(
                    message = resourceProvider.getString(R.string.generic_error_document_not_found)
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

    override fun updateQtspUserSelection(qtspData: QtspData) {
        eudiRQESUi.currentSelection = eudiRQESUi.currentSelection.copy(
            qtsp = qtspData
        )
    }

    override fun updateCertificateUserSelection(certificateData: CertificateData) {
        eudiRQESUi.currentSelection = eudiRQESUi.currentSelection.copy(
            certificate = EudiRQESUi.TBDByCore(
                value = certificateData.name //TODO change this once integration with Core is ready.
            )
        )
    }
}