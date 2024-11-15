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

import android.content.Context
import eu.europa.ec.rqesui.domain.controller.EudiRqesController
import eu.europa.ec.rqesui.domain.controller.EudiRqesControllerImpl
import eu.europa.ec.rqesui.domain.controller.KeyStorage
import eu.europa.ec.rqesui.domain.controller.KeyStorageImpl
import eu.europa.ec.rqesui.domain.controller.LocalizationController
import eu.europa.ec.rqesui.domain.controller.LocalizationControllerImpl
import eu.europa.ec.rqesui.domain.controller.LogController
import eu.europa.ec.rqesui.domain.controller.LogControllerImpl
import eu.europa.ec.rqesui.domain.controller.PreferencesController
import eu.europa.ec.rqesui.domain.controller.PreferencesControllerImpl
import eu.europa.ec.rqesui.infrastructure.EudiRQESUi
import eu.europa.ec.rqesui.infrastructure.provider.ResourceProvider
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Single

@Single
internal fun providePreferencesController(context: Context): PreferencesController =
    PreferencesControllerImpl(context)

@Single
internal fun provideLocalizationController(): LocalizationController =
    LocalizationControllerImpl(EudiRQESUi.getEudiRQESUiConfig())

@Single
internal fun provideEudiRqesController(resourceProvider: ResourceProvider): EudiRqesController =
    EudiRqesControllerImpl(EudiRQESUi, resourceProvider)

@Factory
internal fun provideLogController(): LogController =
    LogControllerImpl(EudiRQESUi.getEudiRQESUiConfig())

@Factory
internal fun provideKeyStorage(preferencesController: PreferencesController): KeyStorage =
    KeyStorageImpl(preferencesController)