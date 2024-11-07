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

import eu.europa.ec.rqesui.domain.controller.EudiRqesGetSelectedFilePartialState
import eu.europa.ec.rqesui.domain.controller.EudiRqesGetSelectedQtspPartialState
import eu.europa.ec.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.rqesui.domain.interactor.SuccessInteractor
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
import org.koin.android.annotation.KoinViewModel

internal data class State(
    val isLoading: Boolean = false,
    val selectionItem: SelectionItemUi? = null,
    val error: ContentErrorConfig? = null,

    val title: String,
    val headline: String,
    val subtitle: String,
    val bottomBarButtonText: String,
) : ViewState

internal sealed class Event : ViewEvent {
    data object Init : Event()
    data object Pop : Event()
    data object DismissError : Event()
    data class CreateSelectionItem(val qtsp: QtspData) : Event()

    data object BottomBarButtonPressed : Event()

    data class ViewDocument(val documentData: DocumentData) : Event()
}

internal sealed class Effect : ViewSideEffect {
    sealed class Navigation : Effect() {
        data class SwitchScreen(val screenRoute: String) : Navigation()
        data object Finish : Navigation()
    }

    data class SelectedQtspFetched(val qtsp: QtspData) : Effect()
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
            headline = resourceProvider.getLocalizedString(LocalizableKey.Success),
            subtitle = resourceProvider.getLocalizedString(LocalizableKey.SuccessfullySignedDocument),
            bottomBarButtonText = resourceProvider.getLocalizedString(LocalizableKey.SaveAndClose),
        )
    }

    override fun handleEvents(event: Event) {
        when (event) {
            is Event.Init -> {
                getSelectedQtsp(event)
            }

            is Event.Pop -> {
                setEffect { Effect.Navigation.Finish }
            }

            is Event.DismissError -> {
                setState { copy(error = null) }
            }

            is Event.CreateSelectionItem -> {
                createSelectionItem(event, event.qtsp)
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

    private fun getSelectedQtsp(event: Event) {
        when (val response = successInteractor.getSelectedQtsp()) {
            is EudiRqesGetSelectedQtspPartialState.Failure -> {
                setState {
                    copy(
                        error = ContentErrorConfig(
                            onRetry = { setEvent(event) },
                            errorSubTitle = response.error.message,
                            onCancel = {
                                setEvent(Event.DismissError)
                                setEffect { Effect.Navigation.Finish }
                            }
                        )
                    )
                }
            }

            is EudiRqesGetSelectedQtspPartialState.Success -> {
                setState { copy(error = null) }
                setEffect { Effect.SelectedQtspFetched(qtsp = response.qtsp) }
            }
        }
    }

    private fun createSelectionItem(event: Event, qtsp: QtspData) {
        when (val response = successInteractor.getSelectedFile()) {
            is EudiRqesGetSelectedFilePartialState.Failure -> {
                setState {
                    copy(
                        selectionItem = null,
                        error = ContentErrorConfig(
                            onRetry = { setEvent(event) },
                            errorSubTitle = response.error.message,
                            onCancel = {
                                setEvent(Event.DismissError)
                                setEffect { Effect.Navigation.Finish }
                            }
                        )
                    )
                }
            }

            is EudiRqesGetSelectedFilePartialState.Success -> {
                setState {
                    copy(
                        error = null,
                        selectionItem = SelectionItemUi(
                            documentData = response.file,
                            subtitle = resourceProvider.getLocalizedString(
                                LocalizableKey.SignedBy,
                                listOf(qtsp.qtspName)
                            ),
                            action = resourceProvider.getLocalizedString(LocalizableKey.View)
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