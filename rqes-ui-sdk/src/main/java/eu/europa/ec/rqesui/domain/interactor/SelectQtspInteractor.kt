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

import eu.europa.ec.rqesui.infrastructure.EudiRQESUi
import eu.europa.ec.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.rqesui.infrastructure.config.data.QTSPData
import eu.europa.ec.rqesui.infrastructure.provider.ResourceProvider
import java.net.URI

internal interface SelectQtspInteractor {
    fun getQTSPList(): List<QTSPData>
    fun getDocumentData(): DocumentData
    fun updateQTSPUserSelection(qtspData: QTSPData)
}

internal class SelectQtspInteractorImpl(
    private val resourceProvider: ResourceProvider,
    //TODO change this when integration with Core is ready.
    private val rqesCoreController: Any? = null,
) : SelectQtspInteractor {

    private val genericErrorMsg
        get() = resourceProvider.genericErrorMessage()

    override fun getQTSPList(): List<QTSPData> {
        return EudiRQESUi.getEudiRQESUiConfig().qtsps
    }

    override fun getDocumentData(): DocumentData {
        //TODO return EudiRQESUi.file
        return DocumentData(
            documentName = "Document name.PDF",
            uri = URI("")
        )
    }

    override fun updateQTSPUserSelection(qtspData: QTSPData) {
        // TODO set selected QTSP to RQES config
    }
}