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

package eu.europa.ec.rqesui.infrastructure

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import eu.europa.ec.rqesui.domain.di.base.EudiRQESUIModule
import eu.europa.ec.rqesui.infrastructure.EudiRQESUi.State.Companion.fromSignDocumentStep
import eu.europa.ec.rqesui.infrastructure.config.EudiRQESUiConfig
import eu.europa.ec.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.rqesui.infrastructure.config.data.QTSPData
import eu.europa.ec.rqesui.presentation.ui.container.EudiRQESContainer
import eu.europa.ec.rqesui.presentation.utils.Constants.STEP_KEY
import kotlinx.parcelize.Parcelize
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.ksp.generated.module

object EudiRQESUi {

    private lateinit var _eudiRQESUiConfig: EudiRQESUiConfig
    private var state: State = State.None

    fun setup(application: Application, config: EudiRQESUiConfig) {
        _eudiRQESUiConfig = config
        setupKoin(application)
    }

    fun launchSdk(
        context: Context,
        step: SignDocumentStep,
    ) {
        val newSdkState = fromSignDocumentStep(step)
        setState(newSdkState)
        resume(context, step)
    }

    fun resume(context: Context, step: SignDocumentStep) {
        (context as? Activity)?.startActivity(
            Intent(context, EudiRQESContainer::class.java)
                .putExtra(STEP_KEY, step)
        )
    }

    @Throws
    internal fun getEudiRQESUiConfig(): EudiRQESUiConfig {
        if (!::_eudiRQESUiConfig.isInitialized) {
            throw IllegalStateException("EudiRQESUi must be initialized first. Please call EudiRQESUi.setup()")
        }
        return _eudiRQESUiConfig
    }

    internal fun setState(state: State) {
        this.state = state
    }

    internal fun getState(): State = this.state

    private fun setupKoin(application: Application) {
        startKoin {
            androidContext(application)
            if (getEudiRQESUiConfig().printLogs) {
                androidLogger()
            }
            modules(EudiRQESUIModule().module)
        }
    }

    sealed class State {
        data object None : State()
        data class Initial(val file: DocumentData, val qtsps: List<QTSPData>) : State()
        data class Certificate(val tBDByCore: TBDByCore) : State()
        data class ViewDocument(val file: DocumentData) : State()
        data object Sign : State()

        companion object {
            fun fromSignDocumentStep(step: SignDocumentStep): State {
                return when (step) {
                    is SignDocumentStep.Start -> Initial(step.file, step.qtsps)
                    is SignDocumentStep.SelectCertificate -> Certificate(step.tBDByCore)
                    is SignDocumentStep.Completed -> Sign
                }
            }
        }
    }

    //TODO delete and adjust accordingly when integration with Core is done.
    @Parcelize
    data class TBDByCore(val value: String) : Parcelable

    @Parcelize
    sealed class SignDocumentStep : Parcelable {
        data class Start(val file: DocumentData, val qtsps: List<QTSPData>) : SignDocumentStep()
        data class SelectCertificate(val tBDByCore: TBDByCore) : SignDocumentStep()
        data object Completed : SignDocumentStep()
    }
}