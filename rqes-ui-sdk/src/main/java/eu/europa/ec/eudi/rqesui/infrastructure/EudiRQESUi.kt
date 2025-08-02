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

package eu.europa.ec.eudi.rqesui.infrastructure

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import eu.europa.ec.eudi.rqes.core.RQESService
import eu.europa.ec.eudi.rqes.core.documentRetrieval.ResolutionOutcome
import eu.europa.ec.eudi.rqesui.domain.di.base.EudiRQESUIModule
import eu.europa.ec.eudi.rqesui.domain.entities.error.EudiRQESUiError
import eu.europa.ec.eudi.rqesui.domain.extension.decode
import eu.europa.ec.eudi.rqesui.domain.extension.getFileName
import eu.europa.ec.eudi.rqesui.domain.util.Constants.SDK_STATE
import eu.europa.ec.eudi.rqesui.infrastructure.config.EudiRQESUiConfig
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.QtspData
import eu.europa.ec.eudi.rqesui.presentation.ui.container.EudiRQESContainer
import kotlinx.parcelize.Parcelize
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.KoinApplication
import org.koin.core.context.GlobalContext.startKoin
import org.koin.ksp.generated.module

@JvmInline
value class RemoteUri(val uri: Uri)

@JvmInline
value class DocumentUri(val uri: Uri)

object EudiRQESUi {

    private const val SDK_NOT_INITIALIZED_TITLE =
        "EudiRQESUi Error"

    private const val SDK_NOT_INITIALIZED_MESSAGE =
        "Before calling resume, SDK must be initialized firstly. Call EudiRQESUi.launchSDK()"

    private lateinit var _eudiRQESUiConfig: EudiRQESUiConfig
    private lateinit var sessionData: SessionData

    private var state: State = State.None
    private var rqesService: RQESService? = null
    private var authorizedService: RQESService.Authorized? = null
    private var remoteResolutionOutcome: ResolutionOutcome? = null

    fun setup(
        application: Application,
        config: EudiRQESUiConfig,
        koinApplication: KoinApplication? = null
    ) {
        _eudiRQESUiConfig = config
        setupKoin(application, koinApplication)
    }

    /**
     * Starts the SDK with the provided remote [RemoteUri].
     *
     * This function initializes the SDK with the given remote [RemoteUri].
     * If the remote [RemoteUri] is invalid throws [EudiRQESUiError].
     *
     * @param context The application [Context].
     * @param remoteUri The remote url [RemoteUri] for document retrieval.
     * @throws EudiRQESUiError If the remote [RemoteUri] is invalid.
     */
    @Throws(EudiRQESUiError::class)
    fun initiate(
        context: Context,
        remoteUri: RemoteUri,
    ) {
        initializeSDK(
            context = context,
            remoteUri = remoteUri.uri.decode().getOrThrow()
        )
    }

    /**
     * Starts the SDK with the provided document [DocumentUri].
     *
     * This function initializes the SDK with the given document [DocumentUri].
     * If the filename cannot be determined throws [EudiRQESUiError].
     *
     * @param context The application [Context].
     * @param documentUri The [DocumentUri] of the document to be loaded.
     * @throws EudiRQESUiError If the filename cannot be extracted from the [DocumentUri].
     */
    @Throws(EudiRQESUiError::class)
    fun initiate(
        context: Context,
        documentUri: DocumentUri,
    ) {
        initializeSDK(
            context = context,
            documentUri = documentUri.uri
        )
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
        if (!::sessionData.isInitialized) {
            throw EudiRQESUiError(
                title = SDK_NOT_INITIALIZED_TITLE,
                message = SDK_NOT_INITIALIZED_MESSAGE
            )
        }
        sessionData = sessionData.copy(
            authorizationCode = authorizationCode
        )
        setState(calculateNextState())
        launchSDK(context)
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
            throw EudiRQESUiError(
                title = SDK_NOT_INITIALIZED_TITLE,
                message = SDK_NOT_INITIALIZED_MESSAGE
            )
        }
        return _eudiRQESUiConfig
    }

    internal fun setRqesService(rqesService: RQESService) {
        this.rqesService = rqesService
    }

    internal fun getRqesService(): RQESService? {
        return rqesService
    }

    internal fun setAuthorizedService(authorizedService: RQESService.Authorized) {
        this.authorizedService = authorizedService
    }

    internal fun getAuthorizedService(): RQESService.Authorized? {
        return authorizedService
    }

    internal fun setRemoteResolutionOutcome(resolutionOutcome: ResolutionOutcome) {
        this.remoteResolutionOutcome = resolutionOutcome
    }

    internal fun getRemoteResolutionOutcome(): ResolutionOutcome? {
        return remoteResolutionOutcome
    }

    internal fun setSessionData(sessionData: SessionData) {
        this.sessionData = sessionData
    }

    internal fun getSessionData(): SessionData {
        return sessionData
    }

    /**
     * Initializes the SDK with the provided context, document URI, and remote URL.
     *
     * This function sets up the necessary session data, including the document file information (name and URI)
     * if a document URI is provided, the remote URI if provided, and initializes other session-related fields.
     * It also resets the RQES service and authorized service instances and sets the initial state of the SDK.
     * Finally, it triggers the launch of the SDK's core functionality.
     *
     * @param context The application context.
     * @param documentUri Optional URI of the document to be processed.  If provided, the document's filename is extracted.
     * @param remoteUri Optional URI representing a remote location for the document or related data.
     * @throws EudiRQESUiError If an error occurs while extracting the document filename from the provided URI.
     */
    @Throws(EudiRQESUiError::class)
    private fun initializeSDK(
        context: Context,
        documentUri: Uri? = null,
        remoteUri: Uri? = null
    ) {
        val documentData = documentUri?.let {
            DocumentData(
                documentName = it.getFileName(context).getOrThrow(),
                uri = it
            )
        }

        sessionData = SessionData(
            file = documentData,
            remoteUrl = remoteUri,
            qtsp = null,
            authorizationCode = null,
        )

        rqesService = null
        authorizedService = null
        remoteResolutionOutcome = null

        setState(
            State.Initial(
                file = documentUri,
            )
        )

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
            val intent = Intent(context, EudiRQESContainer::class.java).apply {
                putExtra(SDK_STATE, getState())
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } else {
            throw EudiRQESUiError(
                title = "Context Error",
                message = "Context passed is not an Activity."
            )
        }
    }

    /**
     * Calculates the next state in the flow based on the current state.
     *
     * This function uses the current state and the selected file or remote uri to determine the next state.
     * If a file is selected or a remote uri is provided, it transitions through the states: None -> Initial -> Certificate -> Success.
     * If no file is selected or remote uri is not provided, it throws an [EudiRQESUiError] indicating that the SDK is not initialized.
     *
     * @return The next state in the flow.
     * @throws EudiRQESUiError If the SDK is not initialized (no file selected or remote uri provided).
     */
    private fun calculateNextState(): State {

        val fileUri = sessionData.file?.uri
        val remoteUri = sessionData.remoteUrl

        if (fileUri == null && remoteUri == null) {
            throw EudiRQESUiError(
                title = SDK_NOT_INITIALIZED_TITLE,
                message = SDK_NOT_INITIALIZED_MESSAGE
            )
        }

        return when (getState()) {
            is State.None -> {
                State.Initial(
                    file = fileUri,
                    remoteUri = remoteUri
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
    }

    private fun setState(state: State) {
        this.state = state
    }

    private fun getState(): State = this.state

    private fun setupKoin(application: Application, koinApplication: KoinApplication?) {
        koinApplication?.modules(EudiRQESUIModule().module) ?: startKoin {
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
        data class Initial(
            val file: Uri? = null,
            val remoteUri: Uri? = null
        ) : State()

        data object Certificate : State()
        data object Success : State()
    }

    internal data class SessionData(
        val file: DocumentData?,
        val remoteUrl: Uri?,
        val qtsp: QtspData?,
        val authorizationCode: String?,
    )
}