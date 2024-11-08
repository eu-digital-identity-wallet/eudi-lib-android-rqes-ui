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
import eu.europa.ec.rqesui.domain.controller.EudiRqesCoreController
import eu.europa.ec.rqesui.domain.controller.EudiRqesGetQtspsPartialState
import eu.europa.ec.rqesui.domain.controller.EudiRqesGetSelectedFilePartialState
import eu.europa.ec.rqesui.infrastructure.config.data.QtspData
import eu.europa.ec.rqesui.infrastructure.provider.ResourceProvider

internal interface SelectQtspInteractor {
    fun getQtsps(): EudiRqesGetQtspsPartialState

    fun getSelectedFile(): EudiRqesGetSelectedFilePartialState

    fun updateQtspUserSelection(qtspData: QtspData)
}

internal class SelectQtspInteractorImpl(
    private val resourceProvider: ResourceProvider,
    private val eudiRqesController: EudiRqesController,
    private val eudiRqesCoreController: EudiRqesCoreController,
) : SelectQtspInteractor {

    private val genericErrorMsg
        get() = resourceProvider.genericErrorMessage()

    override fun getQtsps(): EudiRqesGetQtspsPartialState {
        return eudiRqesController.getQtsps()
    }

    override fun getSelectedFile(): EudiRqesGetSelectedFilePartialState {
        return eudiRqesController.getSelectedFile()
    }

    override fun updateQtspUserSelection(qtspData: QtspData) {
        eudiRqesController.updateQtspUserSelection(qtspData)
    }
}