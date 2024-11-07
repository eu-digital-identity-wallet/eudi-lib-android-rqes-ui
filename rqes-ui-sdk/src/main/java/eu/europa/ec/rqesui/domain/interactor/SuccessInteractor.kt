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

import eu.europa.ec.rqesui.domain.controller.EudiRqesController
import eu.europa.ec.rqesui.domain.controller.EudiRqesGetSelectedFilePartialState
import eu.europa.ec.rqesui.domain.controller.EudiRqesGetSelectedQtspPartialState
import eu.europa.ec.rqesui.infrastructure.provider.ResourceProvider

internal interface SuccessInteractor {
    fun getSelectedFile(): EudiRqesGetSelectedFilePartialState

    fun getSelectedQtsp(): EudiRqesGetSelectedQtspPartialState
}

internal class SuccessInteractorImpl(
    private val resourceProvider: ResourceProvider,
    private val eudiRqesController: EudiRqesController,
    //TODO change this when integration with Core is ready.
    private val rqesCoreController: Any? = null,
) : SuccessInteractor {

    private val genericErrorMsg
        get() = resourceProvider.genericErrorMessage()

    override fun getSelectedFile(): EudiRqesGetSelectedFilePartialState {
        return eudiRqesController.getSelectedFile()
    }

    override fun getSelectedQtsp(): EudiRqesGetSelectedQtspPartialState {
        return eudiRqesController.getSelectedQtsp()
    }
}