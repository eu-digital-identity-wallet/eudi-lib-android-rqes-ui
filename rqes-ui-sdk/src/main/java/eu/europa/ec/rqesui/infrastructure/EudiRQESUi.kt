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
import eu.europa.ec.rqesui.infrastructure.config.data.QTSPData
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

    //TODO initialize-cache this when State.Initial is called.
    internal lateinit var file: DocumentData

    //TODO cache this when User selects a QTSP.
    internal lateinit var qtsp: QTSPData

    fun setup(application: Application, config: EudiRQESUiConfig) {
        _eudiRQESUiConfig = config
        setupKoin(application)
    }

    /**
     * Launches the SDK with the provided document [Uri].
     *
     * This function initializes the SDK with the given document [Uri].
     *
     * @param context The application [Context].
     * @param documentUri The [Uri] of the document to be loaded.
     */
    fun initiate(
        context: Context,
        documentUri: Uri,
    ) {
        val documentData = DocumentData(
            documentName = documentUri.getFileName(context),
            uri = documentUri
        )

        this.file = documentData

        resume(context)
    }

    fun resume(
        context: Context,
        /*TODO how do we know which Screen to open here? Probably will need another argument/flag here*/
    ) {

        val newState: State = calculateNextState()

        setState(newState)

        (context as? Activity)?.startActivity(
            Intent(context, EudiRQESContainer::class.java)
                .putExtra(SDK_STATE, newState)
        )
    }

    private fun calculateNextState(): State {
        return when (getState()) {
            is State.None -> {
                State.Initial(
                    file = this.file.uri
                )
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
}