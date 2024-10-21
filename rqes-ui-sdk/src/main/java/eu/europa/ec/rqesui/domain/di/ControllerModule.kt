package eu.europa.ec.rqesui.domain.di

import android.content.Context
import eu.europa.ec.rqesui.domain.controller.KeyStorage
import eu.europa.ec.rqesui.domain.controller.KeyStorageImpl
import eu.europa.ec.rqesui.domain.controller.LocalizationController
import eu.europa.ec.rqesui.domain.controller.LocalizationControllerImpl
import eu.europa.ec.rqesui.domain.controller.LogController
import eu.europa.ec.rqesui.domain.controller.LogControllerImpl
import eu.europa.ec.rqesui.domain.controller.PreferencesController
import eu.europa.ec.rqesui.domain.controller.PreferencesControllerImpl
import eu.europa.ec.rqesui.infrastructure.EudiRQESUi
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Single

@Single
fun providePrefenencesController(context: Context): PreferencesController =
    PreferencesControllerImpl(context)

@Single
fun provideLocalizationController(): LocalizationController =
    LocalizationControllerImpl(EudiRQESUi.getEudiRQESUiConfig())

@Factory
fun provideLogController(): LogController =
    LogControllerImpl(EudiRQESUi.getEudiRQESUiConfig())

@Factory
fun provideKeyStorage(preferencesController: PreferencesController): KeyStorage =
    KeyStorageImpl(preferencesController)