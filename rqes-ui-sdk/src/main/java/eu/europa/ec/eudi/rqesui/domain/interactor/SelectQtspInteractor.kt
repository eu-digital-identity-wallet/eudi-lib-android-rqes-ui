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

package eu.europa.ec.eudi.rqesui.domain.interactor

import eu.europa.ec.eudi.rqes.core.RQESService
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesController
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetQtspsPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetSelectedFilePartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetServiceAuthorizationUrlPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesSetSelectedQtspPartialState
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.QtspData

internal interface SelectQtspInteractor {
    fun getQtsps(): EudiRqesGetQtspsPartialState

    fun getSelectedFile(): EudiRqesGetSelectedFilePartialState

    fun updateQtspUserSelection(qtspData: QtspData): EudiRqesSetSelectedQtspPartialState

    suspend fun getServiceAuthorizationUrl(rqesService: RQESService): EudiRqesGetServiceAuthorizationUrlPartialState
}

internal class SelectQtspInteractorImpl(
    private val eudiRqesController: EudiRqesController,
) : SelectQtspInteractor {

    override fun getQtsps(): EudiRqesGetQtspsPartialState {
        return eudiRqesController.getQtsps()
    }

    override fun getSelectedFile(): EudiRqesGetSelectedFilePartialState {
        return eudiRqesController.getSelectedFile()
    }

    override fun updateQtspUserSelection(qtspData: QtspData): EudiRqesSetSelectedQtspPartialState {
        return eudiRqesController.setSelectedQtsp(qtspData)
    }

    override suspend fun getServiceAuthorizationUrl(rqesService: RQESService): EudiRqesGetServiceAuthorizationUrlPartialState {
        return eudiRqesController.getServiceAuthorizationUrl(rqesService)
    }
}