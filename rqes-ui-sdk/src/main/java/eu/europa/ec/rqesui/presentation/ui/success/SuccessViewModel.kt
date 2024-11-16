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

package eu.europa.ec.rqesui.presentation.ui.success

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import eu.europa.ec.eudi.rqes.core.RQESService
import eu.europa.ec.eudi.rqes.core.SignedDocuments
import eu.europa.ec.rqesui.domain.controller.EudiRqesAuthorizeCredentialPartialState
import eu.europa.ec.rqesui.domain.controller.EudiRqesGetSelectedFilePartialState
import eu.europa.ec.rqesui.domain.controller.EudiRqesGetSelectedQtspPartialState
import eu.europa.ec.rqesui.domain.controller.EudiRqesSaveSignedDocumentsPartialState
import eu.europa.ec.rqesui.domain.controller.EudiRqesSignDocumentsPartialState
import eu.europa.ec.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.rqesui.domain.interactor.SuccessInteractor
import eu.europa.ec.rqesui.domain.serializer.UiSerializer
import eu.europa.ec.rqesui.domain.util.safeLet
import eu.europa.ec.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.rqesui.infrastructure.config.data.QtspData
import eu.europa.ec.rqesui.infrastructure.provider.ResourceProvider
import eu.europa.ec.rqesui.presentation.architecture.MviViewModel
import eu.europa.ec.rqesui.presentation.architecture.ViewEvent
import eu.europa.ec.rqesui.presentation.architecture.ViewSideEffect
import eu.europa.ec.rqesui.presentation.architecture.ViewState
import eu.europa.ec.rqesui.presentation.entities.SelectionItemUi
import eu.europa.ec.rqesui.presentation.entities.config.ViewDocumentUiConfig
import eu.europa.ec.rqesui.presentation.extension.getFileName
import eu.europa.ec.rqesui.presentation.navigation.SdkScreens
import eu.europa.ec.rqesui.presentation.navigation.helper.generateComposableArguments
import eu.europa.ec.rqesui.presentation.navigation.helper.generateComposableNavigationLink
import eu.europa.ec.rqesui.presentation.ui.component.content.ContentErrorConfig
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

internal data class State(
    val isLoading: Boolean = false,
    val selectionItem: SelectionItemUi? = null,
    val error: ContentErrorConfig? = null,
    val isBottomBarButtonEnabled: Boolean = false,

    val title: String,
    val headline: String = "",
    val subtitle: String = "",
    val bottomBarButtonText: String,

    val selectedUnsignedFile: DocumentData? = null,
    val selectedQtsp: QtspData? = null,
) : ViewState

internal sealed class Event : ViewEvent {
    data object Init : Event()
    data object AuthorizeCredential : Event()
    data class SignDocuments(val authorizedCredential: RQESService.CredentialAuthorized) : Event()
    data class SaveSignedDocuments(val signedDocuments: SignedDocuments) : Event()

    data class CreateUiItems(
        val signedDocumentsUris: List<Uri>,
        val context: Context,
    ) : Event()

    data object Pop : Event()
    data object DismissError : Event()

    data object BottomBarButtonPressed : Event()

    data class ViewDocument(val documentData: DocumentData) : Event()
}

internal sealed class Effect : ViewSideEffect {
    sealed class Navigation : Effect() {
        data class SwitchScreen(val screenRoute: String) : Navigation()
        data object Finish : Navigation()
    }

    data class OnSelectedFileAndQtspGot(
        val selectedFile: DocumentData,
        val selectedQtsp: QtspData,
    ) : Effect()

    data class OnCredentialAuthorized(
        val authorizedCredential: RQESService.CredentialAuthorized,
    ) : Effect()

    data class OnDocumentsSigned(val signedDocuments: SignedDocuments) : Effect()
    data class OnDocumentsSaved(val signedDocumentsUris: List<Uri>) : Effect()
}

@KoinViewModel
internal class SuccessViewModel(
    private val successInteractor: SuccessInteractor,
    private val resourceProvider: ResourceProvider,
    private val uiSerializer: UiSerializer,
) : MviViewModel<Event, State, Effect>() {

    override fun setInitialState(): State {
        return State(
            title = resourceProvider.getLocalizedString(LocalizableKey.SignDocument),
            bottomBarButtonText = resourceProvider.getLocalizedString(LocalizableKey.SaveAndClose),
        )
    }

    override fun handleEvents(event: Event) {
        when (event) {
            is Event.Init -> {
                getSelectedFileAndQtsp(event)
            }

            is Event.AuthorizeCredential -> {
                authorizeCredential(event)
            }

            is Event.SignDocuments -> {
                signDocuments(event, event.authorizedCredential)
            }

            is Event.SaveSignedDocuments -> {
                saveSignedDocuments(event, event.signedDocuments)
            }

            is Event.CreateUiItems -> {
                createUiItems(event, event.signedDocumentsUris, event.context)
            }

            is Event.Pop -> {
                setEffect { Effect.Navigation.Finish }
            }

            is Event.DismissError -> {
                setState { copy(error = null) }
            }

            is Event.BottomBarButtonPressed -> {
                setEffect {
                    Effect.Navigation.Finish
                }
            }

            is Event.ViewDocument -> {
                navigateToViewDocument(event.documentData)
            }
        }
    }

    private fun getSelectedFileAndQtsp(event: Event) {
        val getSelectFileResponse = successInteractor.getSelectedFile()
        val getSelectQtspResponse = successInteractor.getSelectedQtsp()

        if (
            getSelectFileResponse is EudiRqesGetSelectedFilePartialState.Success &&
            getSelectQtspResponse is EudiRqesGetSelectedQtspPartialState.Success
        ) {
            setState {
                copy(
                    selectedUnsignedFile = getSelectFileResponse.file,
                    selectedQtsp = getSelectQtspResponse.qtsp,
                    error = null,
                )
            }
            setEffect {
                Effect.OnSelectedFileAndQtspGot(
                    selectedFile = getSelectFileResponse.file,
                    selectedQtsp = getSelectQtspResponse.qtsp,
                )
            }
        } else {
            setState {
                copy(
                    selectedQtsp = null,
                    error = ContentErrorConfig(
                        onRetry = {
                            setEvent(Event.DismissError)
                            setEvent(event)
                        },
                        errorSubTitle = resourceProvider.genericErrorMessage(),
                        onCancel = {
                            setEvent(Event.DismissError)
                            setEffect { Effect.Navigation.Finish }
                        }
                    )
                )
            }
        }
    }

    private fun authorizeCredential(event: Event) {
        setState { copy(isLoading = true) }

        viewModelScope.launch {
            when (val response = successInteractor.authorizeCredential()) {
                is EudiRqesAuthorizeCredentialPartialState.Failure -> {
                    setState {
                        copy(
                            error = ContentErrorConfig(
                                onRetry = {
                                    setEvent(Event.DismissError)
                                    setEvent(event)
                                },
                                errorSubTitle = response.error.message,
                                onCancel = {
                                    setEvent(Event.DismissError)
                                    setEffect { Effect.Navigation.Finish }
                                }
                            ),
                            isLoading = false,
                        )
                    }
                }

                is EudiRqesAuthorizeCredentialPartialState.Success -> {
                    setState { copy(isLoading = false) }
                    setEffect { Effect.OnCredentialAuthorized(response.authorizedCredential) }
                }
            }
        }
    }

    private fun signDocuments(
        event: Event,
        authorizedCredential: RQESService.CredentialAuthorized,
    ) {
        setState { copy(isLoading = true) }

        viewModelScope.launch {
            val response = successInteractor.signDocuments(
                authorizedCredential = authorizedCredential
            )

            when (response) {
                is EudiRqesSignDocumentsPartialState.Failure -> {
                    setState {
                        copy(
                            error = ContentErrorConfig(
                                onRetry = {
                                    setEvent(Event.DismissError)
                                    setEvent(event)
                                },
                                errorSubTitle = response.error.message,
                                onCancel = {
                                    setEvent(Event.DismissError)
                                    setEffect { Effect.Navigation.Finish }
                                }
                            ),
                            isLoading = false,
                        )
                    }
                }

                is EudiRqesSignDocumentsPartialState.Success -> {
                    setState { copy(isLoading = false) }
                    setEffect { Effect.OnDocumentsSigned(response.signedDocuments) }
                }
            }
        }
    }

    private fun saveSignedDocuments(
        event: Event,
        signedDocuments: SignedDocuments,
    ) {
        setState { copy(isLoading = true) }

        val originalDocumentName = viewState.value
            .selectedUnsignedFile?.documentName
            .orEmpty()

        viewModelScope.launch {
            val response = successInteractor.saveSignedDocuments(
                originalDocumentName = originalDocumentName,
                signedDocuments = signedDocuments,
            )

            when (response) {
                is EudiRqesSaveSignedDocumentsPartialState.Failure -> {
                    setState {
                        copy(
                            error = ContentErrorConfig(
                                onRetry = {
                                    setEvent(Event.DismissError)
                                    setEvent(event)
                                },
                                errorSubTitle = response.error.message,
                                onCancel = {
                                    setEvent(Event.DismissError)
                                    setEffect { Effect.Navigation.Finish }
                                }
                            ),
                            isLoading = false,
                        )
                    }
                }

                is EudiRqesSaveSignedDocumentsPartialState.Success -> {
                    setState { copy(isLoading = false) }
                    setEffect { Effect.OnDocumentsSaved(response.savedDocumentsUri) }
                }
            }
        }
    }

    private fun createUiItems(
        event: Event,
        signedDocumentsUris: List<Uri>,
        context: Context,
    ) {
        with(viewState.value) {
            safeLet(
                signedDocumentsUris.firstOrNull(),
                selectedQtsp
            ) { safeSignedDocumentUri, safeSelectedQtsp ->
                val documentName = safeSignedDocumentUri.getFileName(context)
                    .getOrNull().orEmpty()

                val selectionItem = SelectionItemUi(
                    documentData = DocumentData(
                        documentName = documentName,
                        uri = safeSignedDocumentUri
                    ),
                    subtitle = resourceProvider.getLocalizedString(
                        LocalizableKey.SignedBy,
                        listOf(safeSelectedQtsp.name)
                    ),
                    action = resourceProvider.getLocalizedString(LocalizableKey.View),
                )

                setState {
                    copy(
                        selectionItem = selectionItem,
                        headline = resourceProvider.getLocalizedString(LocalizableKey.Success),
                        subtitle = resourceProvider.getLocalizedString(LocalizableKey.SuccessfullySignedDocument),
                        isBottomBarButtonEnabled = true,
                    )
                }
            } ?: run {
                setState {
                    copy(
                        selectionItem = null,
                        error = ContentErrorConfig(
                            onRetry = {
                                setEvent(Event.DismissError)
                                setEvent(event)
                            },
                            errorSubTitle = resourceProvider.genericErrorMessage(),
                            onCancel = {
                                setEvent(Event.DismissError)
                                setEffect { Effect.Navigation.Finish }
                            }
                        )
                    )
                }
            }
        }
    }

    private fun navigateToViewDocument(documentData: DocumentData) {
        val screenRoute = generateComposableNavigationLink(
            screen = SdkScreens.ViewDocument,
            arguments = generateComposableArguments(
                arguments = mapOf(
                    ViewDocumentUiConfig.serializedKeyName to uiSerializer.toBase64(
                        model = ViewDocumentUiConfig(
                            isSigned = true,
                            documentData = documentData
                        ),
                        parser = ViewDocumentUiConfig.Parser
                    )
                )
            )
        )
        setEffect {
            Effect.Navigation.SwitchScreen(screenRoute = screenRoute)
        }
    }
}