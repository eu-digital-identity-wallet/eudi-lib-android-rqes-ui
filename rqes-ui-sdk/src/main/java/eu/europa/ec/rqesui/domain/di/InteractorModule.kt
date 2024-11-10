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

package eu.europa.ec.rqesui.domain.di

import eu.europa.ec.rqesui.domain.controller.EudiRqesController
import eu.europa.ec.rqesui.domain.controller.EudiRqesCoreController
import eu.europa.ec.rqesui.domain.interactor.SelectCertificateInteractor
import eu.europa.ec.rqesui.domain.interactor.SelectCertificateInteractorImpl
import eu.europa.ec.rqesui.domain.interactor.SelectQtspInteractor
import eu.europa.ec.rqesui.domain.interactor.SelectQtspInteractorImpl
import eu.europa.ec.rqesui.domain.interactor.SuccessInteractor
import eu.europa.ec.rqesui.domain.interactor.SuccessInteractorImpl
import eu.europa.ec.rqesui.infrastructure.provider.ResourceProvider
import org.koin.core.annotation.Factory

@Factory
internal fun provideSelectQtspInteractor(
    resourceProvider: ResourceProvider,
    eudiRqesController: EudiRqesController,
    eudiRqesCoreController: EudiRqesCoreController,
): SelectQtspInteractor = SelectQtspInteractorImpl(
    resourceProvider,
    eudiRqesController,
    eudiRqesCoreController,
)

@Factory
internal fun provideSelectCertificateInteractor(
    resourceProvider: ResourceProvider,
    eudiRqesController: EudiRqesController,
    eudiRqesCoreController: EudiRqesCoreController,
): SelectCertificateInteractor = SelectCertificateInteractorImpl(
    resourceProvider,
    eudiRqesController,
    eudiRqesCoreController,
)

@Factory
internal fun provideSuccessInteractor(
    resourceProvider: ResourceProvider,
    eudiRqesController: EudiRqesController,
    eudiRqesCoreController: EudiRqesCoreController,
): SuccessInteractor = SuccessInteractorImpl(
    resourceProvider,
    eudiRqesController,
    eudiRqesCoreController,
)