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
import android.net.Uri
import android.os.Parcelable
import eu.europa.ec.rqesui.domain.di.base.EudiRQESUIModule
import eu.europa.ec.rqesui.domain.entities.error.EudiRQESUiError
import eu.europa.ec.rqesui.domain.util.Constants.SDK_STATE
import eu.europa.ec.rqesui.infrastructure.config.EudiRQESUiConfig
import eu.europa.ec.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.rqesui.infrastructure.config.data.QtspData
import eu.europa.ec.rqesui.presentation.extension.getFileName
import eu.europa.ec.rqesui.presentation.ui.container.EudiRQESContainer
import kotlinx.parcelize.Parcelize
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.ksp.generated.module

object EudiRQESUi {

    private lateinit var _eudiRQESUiConfig: EudiRQESUiConfig
    private var state: State = State.None

    internal lateinit var currentSelection: CurrentSelection

    fun setup(application: Application, config: EudiRQESUiConfig) {
        _eudiRQESUiConfig = config
        setupKoin(application)
    }

    /**
     * Launches the SDK with the provided document [Uri].
     *
     * This function initializes the SDK with the given document [Uri].
     * If the filename cannot be determined throws [EudiRQESUiError].
     *
     * @param context The application [Context].
     * @param documentUri The [Uri] of the document to be loaded.
     */
    @Throws(EudiRQESUiError::class)
    fun initiate(
        context: Context,
        documentUri: Uri,
    ) {
        val documentData = DocumentData(
            documentName = documentUri.getFileName(context).getOrThrow(),
            uri = documentUri
        )

        this.currentSelection = CurrentSelection(
            file = documentData,
            qtsp = null,
            certificate = null
        )

        resume(
            context = context,
            nextState = State.Initial(
                file = documentUri
            )
        )
    }

    /**
     * Resumes the SDK and auto calculates the next state internally.
     *
     * If the context passed is not an Activity, throws [EudiRQESUiError].
     *
     * @param context The application [Context].
     */
    @Throws(EudiRQESUiError::class)
    fun resume(
        context: Context,
        nextState: State? = null,
    ) {

        val newState: State = nextState ?: calculateNextState()

        setState(newState)

        if (context as? Activity != null) {
            context.startActivity(
                Intent(context, EudiRQESContainer::class.java).putExtra(
                    SDK_STATE,
                    newState
                )
            )
        } else {
            throw EudiRQESUiError("Context passed is not an Activity.")
        }
    }

    private fun calculateNextState(): State {
        return when (getState()) {
            is State.None -> {
                this.currentSelection.file?.let { safeFile ->
                    State.Initial(
                        file = safeFile.uri
                    )
                } ?: State.None
            }

            is State.Initial -> {
                State.Certificate(
                    tBDByCore = TBDByCore("some_tbd_value")
                )
            }

            is State.Certificate -> {
                State.Success
            }

            is State.Success -> {
                State.Success
            }
        }
    }

    /**
     * Retrieves the configuration for the EudiRQESUi.
     *
     * This function throws an [EudiRQESUiError] if the EudiRQESUi has not been initialized
     * by calling [EudiRQESUi.setup] prior to invoking this function.
     *
     * @return The [EudiRQESUiConfig] instance containing the configuration.
     * @throws EudiRQESUiError If the EudiRQESUi has not been initialized.
     */
    @Throws(EudiRQESUiError::class)
    internal fun getEudiRQESUiConfig(): EudiRQESUiConfig {
        if (!::_eudiRQESUiConfig.isInitialized) {
            throw EudiRQESUiError("EudiRQESUi must be initialized first. Please call EudiRQESUi.setup()")
        }
        return _eudiRQESUiConfig
    }

    private fun setState(state: State) {
        this.state = state
    }

    private fun getState(): State = this.state

    private fun setupKoin(application: Application) {
        startKoin {
            androidContext(application)
            if (getEudiRQESUiConfig().printLogs) {
                androidLogger()
            }
            modules(EudiRQESUIModule().module)
        }
    }

    @Parcelize
    sealed class State : Parcelable {
        data object None : State()
        data class Initial(val file: Uri) : State()
        data class Certificate(val tBDByCore: TBDByCore) : State()
        data object Success : State()
    }

    //TODO delete and adjust accordingly when integration with Core is done.
    @Parcelize
    data class TBDByCore(val value: String) : Parcelable

    internal data class CurrentSelection(
        val file: DocumentData?,
        val qtsp: QtspData?,
        val certificate: TBDByCore?,
    )
}