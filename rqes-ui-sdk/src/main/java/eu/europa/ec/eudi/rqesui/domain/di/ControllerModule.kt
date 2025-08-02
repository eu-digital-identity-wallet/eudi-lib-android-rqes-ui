/*
 * Copyright (c) 2025 European Commission
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.europa.ec.eudi.rqesui.domain.di

import android.content.Context
import eu.europa.ec.eudi.rqesui.domain.controller.LocalizationController
import eu.europa.ec.eudi.rqesui.domain.controller.LocalizationControllerImpl
import eu.europa.ec.eudi.rqesui.domain.controller.LogController
import eu.europa.ec.eudi.rqesui.domain.controller.LogControllerImpl
import eu.europa.ec.eudi.rqesui.domain.controller.PreferencesController
import eu.europa.ec.eudi.rqesui.domain.controller.PreferencesControllerImpl
import eu.europa.ec.eudi.rqesui.domain.controller.RqesController
import eu.europa.ec.eudi.rqesui.domain.controller.RqesControllerImpl
import eu.europa.ec.eudi.rqesui.infrastructure.EudiRQESUi
import eu.europa.ec.eudi.rqesui.infrastructure.provider.ResourceProvider
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Single

@Single
internal fun providePreferencesController(context: Context): PreferencesController =
    PreferencesControllerImpl(context)

@Single
internal fun provideLocalizationController(): LocalizationController =
    LocalizationControllerImpl(EudiRQESUi.getEudiRQESUiConfig())

@Single
internal fun provideRqesController(resourceProvider: ResourceProvider): RqesController =
    RqesControllerImpl(EudiRQESUi, resourceProvider)

@Factory
internal fun provideLogController(): LogController =
    LogControllerImpl(EudiRQESUi.getEudiRQESUiConfig())