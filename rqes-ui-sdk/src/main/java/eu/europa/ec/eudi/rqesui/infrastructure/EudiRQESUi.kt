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

package eu.europa.ec.eudi.rqesui.infrastructure

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import eu.europa.ec.eudi.rqes.core.RQESService
import eu.europa.ec.eudi.rqesui.domain.di.base.EudiRQESUIModule
import eu.europa.ec.eudi.rqesui.domain.entities.error.EudiRQESUiError
import eu.europa.ec.eudi.rqesui.domain.util.Constants.SDK_STATE
import eu.europa.ec.eudi.rqesui.infrastructure.config.EudiRQESUiConfig
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.QtspData
import eu.europa.ec.eudi.rqesui.presentation.extension.getFileName
import eu.europa.ec.eudi.rqesui.presentation.ui.container.EudiRQESContainer
import kotlinx.parcelize.Parcelize
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.ksp.generated.module

object EudiRQESUi {

    private const val SDK_NOT_INITIALIZED_MESSAGE =
        "Before calling resume, SDK must be initialized firstly. Call EudiRQESUi.launchSDK()"
    private lateinit var _eudiRQESUiConfig: EudiRQESUiConfig
    private var state: State = State.None

    internal lateinit var currentSelection: CurrentSelection

    internal var rqesService: RQESService? = null
    internal var authorizedService: RQESService.Authorized? = null

    fun setup(application: Application, config: EudiRQESUiConfig) {
        _eudiRQESUiConfig = config
        setupKoin(application)
    }

    /**
     * Starts the SDK with the provided document [Uri].
     *
     * This function initializes the SDK with the given document [Uri].
     * If the filename cannot be determined throws [EudiRQESUiError].
     *
     * @param context The application [Context].
     * @param documentUri The [Uri] of the document to be loaded.
     * @throws EudiRQESUiError If the filename cannot be extracted from the [Uri].
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

        currentSelection = CurrentSelection(
            file = documentData,
            qtsp = null,
            authorizationCode = null,
        )

        rqesService = null
        authorizedService = null

        setState(
            State.Initial(
                file = documentUri
            )
        )

        launchSDK(context)
    }

    /**
     * Resumes the SDK flow and automatically calculates the next state.
     *
     * This function should be called after the user has successfully authenticated
     * with their identity provider and you have received an authorization code.
     * The SDK will then use this code to proceed with the request process.
     *
     * **Important:**
     * -  This function must be called with an Activity context. Passing a non-Activity context
     *    will result in an [EudiRQESUiError] being thrown.
     * - Ensure that the SDK has been previously initialized using one of the `start()` methods
     *   before calling this function.
     *
     * @param context The Activity [Context] used to launch the SDK.
     * @param authorizationCode The authorization code obtained after successful authentication
     *                           with the user's identity provider.
     *
     * @throws [EudiRQESUiError] if the SDK has not been initialized or if a non-Activity context
     * is provided.
     */
    @Throws(EudiRQESUiError::class)
    fun resume(
        context: Context,
        authorizationCode: String
    ) {
        if (!::currentSelection.isInitialized) {
            throw EudiRQESUiError(
                message = SDK_NOT_INITIALIZED_MESSAGE
            )
        }
        currentSelection = currentSelection.copy(
            authorizationCode = authorizationCode
        )
        setState(calculateNextState())
        launchSDK(context)
    }

    /**
     * Launches the EudiRQES SDK.
     *
     * This function starts the EudiRQESContainer activity, which hosts the SDK's UI.
     * It passes the current state of the SDK to the activity using an intent extra.
     *
     * @param context The context used to launch the activity. Must be an Activity.
     *
     * @throws EudiRQESUiError If the provided context is not an Activity.
     */
    @Throws(EudiRQESUiError::class)
    private fun launchSDK(context: Context) {
        if (context as? Activity != null) {
            context.startActivity(
                Intent(context, EudiRQESContainer::class.java).putExtra(
                    SDK_STATE,
                    getState()
                )
            )
        } else {
            throw EudiRQESUiError(message = "Context passed is not an Activity.")
        }
    }

    /**
     * Calculates the next state in the flow based on the current state.
     *
     * This function uses the current state and the selected file to determine the next state.
     * If a file is selected, it transitions through the states: None -> Initial -> Certificate -> Success.
     * If no file is selected, it throws an [EudiRQESUiError] indicating that the SDK is not initialized.
     *
     * @return The next state in the flow.
     * @throws EudiRQESUiError If the SDK is not initialized (no file selected).
     */
    private fun calculateNextState(): State {
        currentSelection.file?.let { safeFile ->
            return when (getState()) {
                is State.None -> {
                    State.Initial(
                        file = safeFile.uri
                    )
                }

                is State.Initial -> {
                    State.Certificate
                }

                is State.Certificate -> {
                    State.Success
                }

                is State.Success -> {
                    State.Success
                }
            }
        } ?: throw EudiRQESUiError(message = SDK_NOT_INITIALIZED_MESSAGE)
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
            throw EudiRQESUiError(message = SDK_NOT_INITIALIZED_MESSAGE)
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
    internal sealed class State : Parcelable {
        data object None : State()
        data class Initial(val file: Uri) : State()
        data object Certificate : State()
        data object Success : State()
    }

    internal data class CurrentSelection(
        val file: DocumentData?,
        val qtsp: QtspData?,
        val authorizationCode: String?,
    )
}