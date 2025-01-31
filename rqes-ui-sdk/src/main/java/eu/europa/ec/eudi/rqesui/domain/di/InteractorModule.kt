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

package eu.europa.ec.eudi.rqesui.domain.di

import eu.europa.ec.eudi.rqesui.domain.controller.RqesController
import eu.europa.ec.eudi.rqesui.domain.interactor.OptionsSelectionInteractor
import eu.europa.ec.eudi.rqesui.domain.interactor.OptionsSelectionInteractorImpl
import eu.europa.ec.eudi.rqesui.domain.interactor.SuccessInteractor
import eu.europa.ec.eudi.rqesui.domain.interactor.SuccessInteractorImpl
import eu.europa.ec.eudi.rqesui.infrastructure.provider.ResourceProvider
import org.koin.core.annotation.Factory

@Factory
internal fun provideOptionsSelectionInteractor(
    resourceProvider: ResourceProvider,
    eudiRqesController: RqesController,
): OptionsSelectionInteractor = OptionsSelectionInteractorImpl(
    eudiRqesController,
    resourceProvider
)

@Factory
internal fun provideSuccessInteractor(
    resourceProvider: ResourceProvider,
    eudiRqesController: RqesController,
): SuccessInteractor = SuccessInteractorImpl(
    resourceProvider,
    eudiRqesController,
)