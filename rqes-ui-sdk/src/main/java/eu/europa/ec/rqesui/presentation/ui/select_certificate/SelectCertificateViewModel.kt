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

package eu.europa.ec.rqesui.presentation.ui.select_certificate

import androidx.lifecycle.viewModelScope
import eu.europa.ec.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.rqesui.domain.extension.toUri
import eu.europa.ec.rqesui.domain.interactor.SelectCertificateInteractor
import eu.europa.ec.rqesui.domain.interactor.SelectCertificatePartialState
import eu.europa.ec.rqesui.infrastructure.config.data.CertificateData
import eu.europa.ec.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.rqesui.infrastructure.provider.ResourceProvider
import eu.europa.ec.rqesui.infrastructure.theme.values.ThemeColors
import eu.europa.ec.rqesui.presentation.architecture.MviViewModel
import eu.europa.ec.rqesui.presentation.architecture.ViewEvent
import eu.europa.ec.rqesui.presentation.architecture.ViewSideEffect
import eu.europa.ec.rqesui.presentation.architecture.ViewState
import eu.europa.ec.rqesui.presentation.entities.SelectionItemUi
import eu.europa.ec.rqesui.presentation.ui.component.AppIcons
import eu.europa.ec.rqesui.presentation.ui.component.content.ContentErrorConfig
import eu.europa.ec.rqesui.presentation.ui.component.wrap.BottomSheetTextData
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

internal data class State(
    val isLoading: Boolean = false,
    val error: ContentErrorConfig? = null,
    val isBottomSheetOpen: Boolean = false,

    val title: String,
    val subtitle: String,
    val certificatesSectionTitle: String,
    val bottomBarButtonText: String,
    val selectionItem: SelectionItemUi,
    val certificates: List<CertificateData> = emptyList(),
    val selectedCertificateIndex: Int = 0,

    val sheetTextData: BottomSheetTextData,
) : ViewState

internal sealed class Event : ViewEvent {
    data object Init : Event()
    data object Pop : Event()
    data object DismissError : Event()

    data class CertificateIndexSelected(val index: Int) : Event()
    data object BottomBarButtonPressed : Event()
    data class ViewDocument(val documentData: DocumentData) : Event()

    sealed class BottomSheet : Event() {

        data class UpdateBottomSheetState(val isOpen: Boolean) : BottomSheet()

        sealed class CancelSignProcess : BottomSheet() {
            data object PrimaryButtonPressed : CancelSignProcess()
            data object SecondaryButtonPressed : CancelSignProcess()
        }
    }
}

internal sealed class Effect : ViewSideEffect {
    sealed class Navigation : Effect() {
        data class SwitchScreen(val screenRoute: String) : Navigation()
        data object Finish : Navigation()
    }

    data object ShowBottomSheet : Effect()
    data object CloseBottomSheet : Effect()
}

@KoinViewModel
internal class SelectCertificateViewModel(
    private val selectCertificateInteractor: SelectCertificateInteractor,
    private val resourceProvider: ResourceProvider,
) : MviViewModel<Event, State, Effect>() {

    override fun setInitialState(): State {
        return State(
            title = resourceProvider.getLocalizedString(LocalizableKey.SignDocument),
            subtitle = resourceProvider.getLocalizedString(LocalizableKey.SelectCertificateTitle),
            certificatesSectionTitle = resourceProvider.getLocalizedString(LocalizableKey.SelectCertificateSubtitle),
            selectionItem = getSelectionItem(),
            bottomBarButtonText = resourceProvider.getLocalizedString(LocalizableKey.Sign),
            sheetTextData = getConfirmCancellationTextData(),
        )
    }

    override fun handleEvents(event: Event) {
        when (event) {
            is Event.Init -> {
                fetchQTSPCertificates(event)
            }

            is Event.Pop -> {
                showBottomSheet()
            }

            is Event.DismissError -> {
                setState {
                    copy(
                        error = null
                    )
                }
            }

            is Event.BottomBarButtonPressed -> {
                viewModelScope.launch {
                    //TODO proceed to sign document
                    // selectCertificateInteractor.certificateSelected==SingDocument
                    setEffect {
                        Effect.Navigation.Finish
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

            is Event.CertificateIndexSelected -> {
                setState {
                    copy(
                        selectedCertificateIndex = event.index
                    )
                }
            }

            is Event.ViewDocument -> {
                // TODO view document in pdf screen
            }
        }
    }

    private fun fetchQTSPCertificates(event: Event) {
        setState {
            copy(
                isLoading = true,
                error = null,
            )
        }

        viewModelScope.launch {
            selectCertificateInteractor.qtspCertificates(
                //TODO Change this once integration with Core is done.
                qtspCertificateEndpoint = "https://qtsp.endpoint".toUri()
            ).collect { response ->
                when (response) {
                    is SelectCertificatePartialState.Success -> {
                        setState {
                            copy(
                                certificates = response.qtspCertificatesList,
                                error = null,
                                isLoading = false,
                            )
                        }
                    }

                    is SelectCertificatePartialState.Failure -> {
                        setState {
                            copy(
                                certificates = emptyList(),
                                error = ContentErrorConfig(
                                    onRetry = { setEvent(event) },
                                    errorSubTitle = response.error,
                                    onCancel = {
                                        setEvent(Event.DismissError)
                                        setEffect { Effect.Navigation.Finish }
                                    }
                                ),
                                isLoading = false,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getSelectionItem(): SelectionItemUi {
        return SelectionItemUi(
            documentData = selectCertificateInteractor.getDocumentData(),
            subtitle = resourceProvider.getLocalizedString(
                LocalizableKey.SignedBy,
                listOf(selectCertificateInteractor.getQtspData().qtspName)
            ),
            iconData = AppIcons.Verified,
            iconTint = ThemeColors.success
        )
    }

    private fun getConfirmCancellationTextData(): BottomSheetTextData {
        return BottomSheetTextData(
            title = resourceProvider.getLocalizedString(LocalizableKey.CancelSignProcessTitle),
            message = resourceProvider.getLocalizedString(LocalizableKey.CancelSignProcessSubtitle),
            positiveButtonText = resourceProvider.getLocalizedString(LocalizableKey.CancelSignProcessPrimaryText),
            negativeButtonText = resourceProvider.getLocalizedString(LocalizableKey.CancelSignProcessSecondaryText),
        )
    }

    private fun showBottomSheet() {
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