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

import androidx.lifecycle.viewModelScope
import eu.europa.ec.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.rqesui.domain.interactor.SuccessInteractor
import eu.europa.ec.rqesui.domain.interactor.SuccessInteractorDoAllPartialState
import eu.europa.ec.rqesui.domain.interactor.SuccessInteractorGetSelectedFileAndQtspPartialState
import eu.europa.ec.rqesui.domain.serializer.UiSerializer
import eu.europa.ec.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.rqesui.infrastructure.config.data.QtspData
import eu.europa.ec.rqesui.infrastructure.provider.ResourceProvider
import eu.europa.ec.rqesui.presentation.architecture.MviViewModel
import eu.europa.ec.rqesui.presentation.architecture.ViewEvent
import eu.europa.ec.rqesui.presentation.architecture.ViewSideEffect
import eu.europa.ec.rqesui.presentation.architecture.ViewState
import eu.europa.ec.rqesui.presentation.entities.SelectionItemUi
import eu.europa.ec.rqesui.presentation.entities.config.ViewDocumentUiConfig
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
    val headline: String? = null,
    val subtitle: String? = null,
    val bottomBarButtonText: String,
) : ViewState

internal sealed class Event : ViewEvent {
    data object Init : Event()
    data class DoAll(
        val originalDocumentName: String,
        val qtspName: String,
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

            is Event.DoAll -> {
                doAll(event, event.originalDocumentName, event.qtspName)
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
        when (val getSelectFileAndQtspResponse = successInteractor.getSelectedFileAndQtsp()) {
            is SuccessInteractorGetSelectedFileAndQtspPartialState.Failure -> {
                setState {
                    copy(
                        error = ContentErrorConfig(
                            onRetry = {
                                setEvent(Event.DismissError)
                                setEvent(event)
                            },
                            errorSubTitle = getSelectFileAndQtspResponse.error.message,
                            onCancel = {
                                setEvent(Event.DismissError)
                                setEffect { Effect.Navigation.Finish }
                            }
                        )
                    )
                }
            }

            is SuccessInteractorGetSelectedFileAndQtspPartialState.Success -> {
                setState {
                    copy(
                        error = null,
                    )
                }
                setEffect {
                    Effect.OnSelectedFileAndQtspGot(
                        selectedFile = getSelectFileAndQtspResponse.selectedFile,
                        selectedQtsp = getSelectFileAndQtspResponse.selectedQtsp,
                    )
                }
            }
        }
    }

    private fun doAll(event: Event, originalDocumentName: String, qtspName: String) {
        setState { copy(isLoading = true) }

        viewModelScope.launch {
            when (val response = successInteractor.doAll(originalDocumentName)) {
                is SuccessInteractorDoAllPartialState.Failure -> {
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
                            selectionItem = null,
                            headline = null,
                            subtitle = null,
                            isBottomBarButtonEnabled = false,
                            isLoading = false,
                        )
                    }
                }

                is SuccessInteractorDoAllPartialState.Success -> {
                    val selectionItem = SelectionItemUi(
                        documentData = response.savedDocument,
                        subtitle = resourceProvider.getLocalizedString(
                            LocalizableKey.SignedBy,
                            listOf(qtspName)
                        ),
                        action = resourceProvider.getLocalizedString(LocalizableKey.View),
                    )

                    setState {
                        copy(
                            selectionItem = selectionItem,
                            headline = resourceProvider.getLocalizedString(LocalizableKey.Success),
                            subtitle = resourceProvider.getLocalizedString(LocalizableKey.SuccessfullySignedDocument),
                            isBottomBarButtonEnabled = true,
                            isLoading = false,
                        )
                    }
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