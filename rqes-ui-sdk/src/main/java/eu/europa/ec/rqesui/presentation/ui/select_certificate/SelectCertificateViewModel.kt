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
import eu.europa.ec.rqesui.domain.interactor.SelectCertificateInteractor
import eu.europa.ec.rqesui.domain.interactor.SelectCertificatePartialState
import eu.europa.ec.rqesui.infrastructure.EudiRQESUi
import eu.europa.ec.rqesui.infrastructure.provider.ResourceProvider
import eu.europa.ec.rqesui.presentation.architecture.MviViewModel
import eu.europa.ec.rqesui.presentation.architecture.ViewEvent
import eu.europa.ec.rqesui.presentation.architecture.ViewSideEffect
import eu.europa.ec.rqesui.presentation.architecture.ViewState
import eu.europa.ec.rqesui.presentation.entities.QTSPCertificateUi
import eu.europa.ec.rqesui.presentation.entities.SelectionItemUi
import eu.europa.ec.rqesui.presentation.entities.toQTSPCertificateUi
import eu.europa.ec.rqesui.presentation.ui.component.AppIcons
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
    val certificatesSectionTitle: String = "",
    val buttonText: String = "",

    val options: List<SelectionItemUi> = emptyList(),

    val certificates: List<QTSPCertificateUi> = emptyList(),
    val selectedCertificateIndex: Int = 0,
) : ViewState

internal sealed class Event : ViewEvent {
    data object Init : Event()
    data object Pop : Event()
    data object Finish : Event()

    data object DismissError : Event()

    data class CertificateSelected(val documentUri: URI) : Event()
    data class CertificateIndexSelected(val index: Int) : Event()
    data class SignDocument(val documentUri: URI) : Event()

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
        data object Finish : Navigation()
        data object Pop : Navigation()
    }

    data object ShowBottomSheet : Effect()
    data object CloseBottomSheet : Effect()
}

@KoinViewModel
internal class SelectCertificateViewModel(
    private val resourceProvider: ResourceProvider,
    private val selectCertificateInteractor: SelectCertificateInteractor
) : MviViewModel<Event, State, Effect>() {

    override fun setInitialState(): State {
        val selectedQTSP = EudiRQESUi.getEudiRQESUiConfig().qtsps.getOrNull(0)
        val subtitleWithQTSP = "Signed by: ${selectedQTSP?.qtspName}"
        // TODO retrieval of selected qtsp

        return State(
            title = resourceProvider.getLocalizedString(LocalizableKey.SignDocument),
            subtitle = resourceProvider.getLocalizedString(LocalizableKey.SelectCertificateTitle),
            certificatesSectionTitle = resourceProvider.getLocalizedString(LocalizableKey.SelectCertificateSubtitle),
            options = listOf(
                SelectionItemUi(
                    title = "Document name.PDF",
                    subTitle = subtitleWithQTSP,
                    icon = AppIcons.Verified
                )
            ),
            buttonText = resourceProvider.getLocalizedString(LocalizableKey.Sign)
        )
    }

    private fun fetchQTSPCertificates() {
        viewModelScope.launch {
            selectCertificateInteractor.qtspCertificates(
                qtspCertificateEndpoint = URI("https://qtsp.endpoint")
            ).collect { response ->
                when (response) {
                    is SelectCertificatePartialState.Success -> {
                        setState {
                            copy(
                                certificates = response.qtspCertificatesList.map {
                                    it.toQTSPCertificateUi()
                                }
                            )
                        }
                    }

                    is SelectCertificatePartialState.Failure -> {
                        // no op
                    }
                }
            }
        }
    }

    override fun handleEvents(event: Event) {
        when (event) {
            is Event.Init -> {
                fetchQTSPCertificates()
            }

            is Event.Pop -> {
                showBottomSheet()
            }

            is Event.Finish -> setEffect {
                Effect.Navigation.Finish
            }

            is Event.DismissError -> {
                // TODO show error
            }

            is Event.SignDocument -> {
                viewModelScope.launch {
                    selectCertificateInteractor.signDocument(
                        documentUri = event.documentUri
                    )
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

            is Event.CertificateSelected -> {
                // TODO set selected certificate
            }

            is Event.CertificateIndexSelected -> {
                setState {
                    copy(
                        selectedCertificateIndex = event.index
                    )
                }
            }
        }
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