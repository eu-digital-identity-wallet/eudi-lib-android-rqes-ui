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

package eu.europa.ec.rqesui.presentation.ui.sign

import androidx.lifecycle.viewModelScope
import eu.europa.ec.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.rqesui.domain.interactor.SignDocumentInteractor
import eu.europa.ec.rqesui.domain.interactor.SignDocumentPartialState
import eu.europa.ec.rqesui.infrastructure.config.data.QTSPData
import eu.europa.ec.rqesui.infrastructure.provider.ResourceProvider
import eu.europa.ec.rqesui.presentation.architecture.MviViewModel
import eu.europa.ec.rqesui.presentation.architecture.ViewEvent
import eu.europa.ec.rqesui.presentation.architecture.ViewSideEffect
import eu.europa.ec.rqesui.presentation.architecture.ViewState
import eu.europa.ec.rqesui.presentation.entities.ModalOptionUi
import eu.europa.ec.rqesui.presentation.entities.SelectionItemUi
import eu.europa.ec.rqesui.presentation.ui.component.content.ContentErrorConfig
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.net.URI

internal data class State(
    val documentUri: URI? = null,

    val isLoading: Boolean = false,
    val error: ContentErrorConfig? = null,
    val isInitialised: Boolean = false,
    val isBottomSheetOpen: Boolean = false,

    val title: String = "",
    val subtitle: String = "",

    val options: List<SelectionItemUi> = emptyList(),
    val sheetContent: SignDocumentBottomSheetContent = SignDocumentBottomSheetContent.ConfirmCancellation,
) : ViewState

internal sealed class Event : ViewEvent {
    data object Init : Event()
    data object Pop : Event()
    data object Finish : Event()

    data object DismissError : Event()

    data class SignDocument(val documentUri: URI) : Event()
    data class SignDocumentButtonPressed(val documentUri: URI) : Event()
    data class ViewDocument(val documentUri: URI) : Event()

    sealed class BottomSheet : Event() {
        data class UpdateBottomSheetState(val isOpen: Boolean) : BottomSheet()

        sealed class CancelSignProcess : BottomSheet() {
            data object PrimaryButtonPressed : CancelSignProcess()
            data object SecondaryButtonPressed : CancelSignProcess()
        }

        sealed class QTSPOptions : BottomSheet() {
            data class QTSPForSigningSelected(
                val qtspData: QTSPData
            ) : QTSPOptions()
        }
    }
}

internal sealed class Effect : ViewSideEffect {
    sealed class Navigation : Effect() {
        data object Finish : Navigation()
    }

    data object ShowBottomSheet : Effect()
    data object CloseBottomSheet : Effect()
}

internal sealed class SignDocumentBottomSheetContent {
    data object ConfirmCancellation : SignDocumentBottomSheetContent()

    data class SelectQTSP(
        val options: List<ModalOptionUi<Event>>
    ) : SignDocumentBottomSheetContent()
}

@KoinViewModel
internal class SignDocumentViewModel(
    private val resourceProvider: ResourceProvider,
    private val signDocumentInteractor: SignDocumentInteractor
) : MviViewModel<Event, State, Effect>() {

    override fun setInitialState(): State = State(
        title = resourceProvider.getLocalizedString(LocalizableKey.SignDocument),
        subtitle = resourceProvider.getLocalizedString(LocalizableKey.ConfirmSelectionTitle),
        options = listOf(
            SelectionItemUi(
                title = signDocumentInteractor.getDocumentName(),
                action = "VIEW"
            )
        )
    )

    override fun handleEvents(event: Event) {
        when (event) {
            is Event.Init -> {
                // TODO
            }

            is Event.Pop -> {
                showBottomSheet(sheetContent = SignDocumentBottomSheetContent.ConfirmCancellation)
            }

            is Event.Finish -> setEffect { Effect.Navigation.Finish }

            is Event.DismissError -> {
                // TODO display error message on screen
            }

            is Event.SignDocument -> {
                viewModelScope.launch {
                    signDocumentInteractor.signPdfDocument(event.documentUri).collect { response ->
                        when (response) {
                            is SignDocumentPartialState.Success -> {
                                setState {
                                    copy(isLoading = false)
                                }
                            }

                            is SignDocumentPartialState.SigningFailure -> setState {
                                copy(isLoading = false)
                            }
                        }
                    }
                }
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
                // TODO view document in pdf screen
            }

            is Event.SignDocumentButtonPressed -> {
                val bottomSheetOptions: List<ModalOptionUi<Event>> =
                    signDocumentInteractor.getQTSPList().map { qtspData ->
                        ModalOptionUi(
                            title = qtspData.qtspName,
                            icon = null,
                            event = Event.BottomSheet.QTSPOptions.QTSPForSigningSelected(qtspData)
                        )
                    }

                showBottomSheet(
                    sheetContent = SignDocumentBottomSheetContent.SelectQTSP(
                        options = bottomSheetOptions
                    )
                )
            }

            is Event.BottomSheet.QTSPOptions.QTSPForSigningSelected -> {
                hideBottomSheet()
                signDocumentInteractor.updateQTSPUserSelection(qtspData = event.qtspData)
                // TODO redirect to Certificate Selection screen
            }
        }
    }

    private fun showBottomSheet(sheetContent: SignDocumentBottomSheetContent) {
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