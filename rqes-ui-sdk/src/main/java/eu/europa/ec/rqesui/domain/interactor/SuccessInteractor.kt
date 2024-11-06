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

internal interface SuccessInteractor {
    fun getDocumentData(): DocumentData
    fun getQtspData(): QTSPData
}

internal class SuccessInteractorImpl(
    private val resourceProvider: ResourceProvider,
    //TODO change this when integration with Core is ready.
    private val rqesCoreController: Any? = null,
) : SuccessInteractor {

    private val genericErrorMsg
        get() = resourceProvider.genericErrorMessage()

    override fun getDocumentData(): DocumentData {
        return EudiRQESUi.file
    }

    override fun getQtspData(): QTSPData {
        return EudiRQESUi.qtsp
    }
}