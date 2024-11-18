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

package eu.europa.ec.eudi.rqesui.presentation.ui.select_qtsp

import android.net.Uri
import androidx.lifecycle.viewModelScope
import eu.europa.ec.eudi.rqes.core.RQESService
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetQtspsPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetSelectedFilePartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetServiceAuthorizationUrlPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesSetSelectedQtspPartialState
import eu.europa.ec.eudi.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.eudi.rqesui.domain.interactor.SelectQtspInteractor
import eu.europa.ec.eudi.rqesui.domain.serializer.UiSerializer
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.QtspData
import eu.europa.ec.eudi.rqesui.infrastructure.provider.ResourceProvider
import eu.europa.ec.eudi.rqesui.presentation.architecture.MviViewModel
import eu.europa.ec.eudi.rqesui.presentation.architecture.ViewEvent
import eu.europa.ec.eudi.rqesui.presentation.architecture.ViewSideEffect
import eu.europa.ec.eudi.rqesui.presentation.architecture.ViewState
import eu.europa.ec.eudi.rqesui.presentation.entities.ModalOptionUi
import eu.europa.ec.eudi.rqesui.presentation.entities.SelectionItemUi
import eu.europa.ec.eudi.rqesui.presentation.entities.config.ViewDocumentUiConfig
import eu.europa.ec.eudi.rqesui.presentation.navigation.SdkScreens
import eu.europa.ec.eudi.rqesui.presentation.navigation.helper.generateComposableArguments
import eu.europa.ec.eudi.rqesui.presentation.navigation.helper.generateComposableNavigationLink
import eu.europa.ec.eudi.rqesui.presentation.ui.component.content.ContentErrorConfig
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.BottomSheetTextData
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

internal data class State(
    val isLoading: Boolean = false,
    val selectionItem: SelectionItemUi? = null,
    val error: ContentErrorConfig? = null,
    val isBottomSheetOpen: Boolean = false,

    val title: String,
    val subtitle: String,
    val bottomBarButtonText: String,

    val sheetContent: SelectQtspBottomSheetContent,
) : ViewState

internal sealed class Event : ViewEvent {
    data object Init : Event()
    data object Pop : Event()
    data object Finish : Event()
    data object DismissError : Event()
    data object BottomBarButtonPressed : Event()

    data class ViewDocument(val documentData: DocumentData) : Event()
    data class FetchServiceAuthorizationUrl(val service: RQESService) : Event()

    sealed class BottomSheet : Event() {
        data class UpdateBottomSheetState(val isOpen: Boolean) : BottomSheet()

        sealed class CancelSignProcess : BottomSheet() {
            data object PrimaryButtonPressed : CancelSignProcess()
            data object SecondaryButtonPressed : CancelSignProcess()
        }

        data class QtspSelected(val qtspData: QtspData) : BottomSheet()
    }
}

internal sealed class Effect : ViewSideEffect {
    sealed class Navigation : Effect() {
        data class SwitchScreen(val screenRoute: String) : Navigation()
        data object Finish : Navigation()
    }

    data object ShowBottomSheet : Effect()
    data object CloseBottomSheet : Effect()

    data class OpenUrl(val uri: Uri) : Effect()
    data class OnSelectedQtspUpdated(val service: RQESService) : Effect()
}

internal sealed class SelectQtspBottomSheetContent {
    data class ConfirmCancellation(
        val bottomSheetTextData: BottomSheetTextData,
    ) : SelectQtspBottomSheetContent()

    data class SelectQTSP(
        val bottomSheetTextData: BottomSheetTextData,
        val options: List<ModalOptionUi<Event>>,
    ) : SelectQtspBottomSheetContent()
}

@KoinViewModel
internal class SelectQtspViewModel(
    private val selectQtspInteractor: SelectQtspInteractor,
    private val resourceProvider: ResourceProvider,
    private val uiSerializer: UiSerializer,
) : MviViewModel<Event, State, Effect>() {

    override fun setInitialState(): State = State(
        title = resourceProvider.getLocalizedString(LocalizableKey.SignDocument),
        subtitle = resourceProvider.getLocalizedString(LocalizableKey.ConfirmSelectionTitle),
        bottomBarButtonText = resourceProvider.getLocalizedString(LocalizableKey.Sign),
        sheetContent = SelectQtspBottomSheetContent.ConfirmCancellation(
            bottomSheetTextData = getConfirmCancellationTextData()
        ),
    )

    override fun handleEvents(event: Event) {
        when (event) {
            is Event.Init -> {
                createSelectionItem(event)
            }

            is Event.Pop -> {
                showBottomSheet(
                    sheetContent = SelectQtspBottomSheetContent.ConfirmCancellation(
                        bottomSheetTextData = getConfirmCancellationTextData()
                    )
                )
            }

            is Event.Finish -> {
                setEffect { Effect.Navigation.Finish }
            }

            is Event.DismissError -> {
                setState { copy(error = null) }
            }

            is Event.BottomSheet.UpdateBottomSheetState -> {
                setState {
                    copy(
                        isBottomSheetOpen = event.isOpen
                    )
                }
            }

            is Event.BottomSheet.CancelSignProcess.PrimaryButtonPressed -> {
                hideBottomSheet()
            }

            is Event.BottomSheet.CancelSignProcess.SecondaryButtonPressed -> {
                setEffect {
                    Effect.Navigation.Finish
                }
            }

            is Event.ViewDocument -> {
                navigateToViewDocument(event.documentData)
            }

            is Event.BottomBarButtonPressed -> {
                getQtsps(event)
            }

            is Event.BottomSheet.QtspSelected -> {
                val response = selectQtspInteractor.updateQtspUserSelection(event.qtspData)
                hideBottomSheet()
                when (response) {
                    is EudiRqesSetSelectedQtspPartialState.Failure -> {
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
                                    }
                                )
                            )
                        }
                    }

                    is EudiRqesSetSelectedQtspPartialState.Success -> {
                        setEffect {
                            Effect.OnSelectedQtspUpdated(service = response.service)
                        }
                    }
                }
            }

            is Event.FetchServiceAuthorizationUrl -> {
                fetchServiceAuthorizationUrl(event, event.service)
            }
        }
    }

    private fun createSelectionItem(event: Event) {
        when (val response = selectQtspInteractor.getSelectedFile()) {
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
                        selectionItem = SelectionItemUi(
                            documentData = response.file,
                            action = resourceProvider.getLocalizedString(LocalizableKey.View)
                        )
                    )
                }
            }
        }
    }

    private fun getQtsps(event: Event) {
        when (val response = selectQtspInteractor.getQtsps()) {
            is EudiRqesGetQtspsPartialState.Failure -> {
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

            is EudiRqesGetQtspsPartialState.Success -> {
                val bottomSheetOptions: List<ModalOptionUi<Event>> =
                    response.qtsps.map { qtspData ->
                        ModalOptionUi(
                            title = qtspData.name,
                            icon = null,
                            event = Event.BottomSheet.QtspSelected(qtspData)
                        )
                    }

                showBottomSheet(
                    sheetContent = SelectQtspBottomSheetContent.SelectQTSP(
                        bottomSheetTextData = getSelectQTSPTextData(),
                        options = bottomSheetOptions
                    )
                )
            }
        }
    }

    private fun fetchServiceAuthorizationUrl(event: Event, service: RQESService) {
        setState {
            copy(isLoading = true)
        }

        viewModelScope.launch {
            val response = selectQtspInteractor.getServiceAuthorizationUrl(
                rqesService = service
            )

            when (response) {
                is EudiRqesGetServiceAuthorizationUrlPartialState.Failure -> {
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
                                }
                            ),
                            isLoading = false,
                        )
                    }
                }

                is EudiRqesGetServiceAuthorizationUrlPartialState.Success -> {
                    setState {
                        copy(isLoading = false)
                    }
                    setEffect { Effect.OpenUrl(uri = response.authorizationUrl) }
                    setEffect { Effect.Navigation.Finish }
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
                            isSigned = false,
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

    private fun getConfirmCancellationTextData(): BottomSheetTextData {
        return BottomSheetTextData(
            title = resourceProvider.getLocalizedString(LocalizableKey.CancelSignProcessTitle),
            message = resourceProvider.getLocalizedString(LocalizableKey.CancelSignProcessSubtitle),
            positiveButtonText = resourceProvider.getLocalizedString(LocalizableKey.CancelSignProcessPrimaryText),
            negativeButtonText = resourceProvider.getLocalizedString(LocalizableKey.CancelSignProcessSecondaryText),
        )
    }

    private fun getSelectQTSPTextData(): BottomSheetTextData {
        return BottomSheetTextData(
            title = resourceProvider.getLocalizedString(LocalizableKey.SelectServiceTitle),
            message = resourceProvider.getLocalizedString(LocalizableKey.SelectServiceSubtitle),
        )
    }

    private fun showBottomSheet(sheetContent: SelectQtspBottomSheetContent) {
        setState {
            copy(sheetContent = sheetContent)
        }
        setEffect {
            Effect.ShowBottomSheet
        }
    }

    private fun hideBottomSheet() {
        setEffect {
            Effect.CloseBottomSheet
        }
    }
}