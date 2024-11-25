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

package eu.europa.ec.eudi.rqesui.presentation.ui.select_certificate

import android.net.Uri
import androidx.lifecycle.viewModelScope
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetCredentialAuthorizationUrlPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetSelectedFilePartialState
import eu.europa.ec.eudi.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.eudi.rqesui.domain.interactor.SelectCertificateInteractor
import eu.europa.ec.eudi.rqesui.domain.interactor.SelectCertificateInteractorAuthorizeServiceAndFetchCertificatesPartialState
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.CertificateData
import eu.europa.ec.eudi.rqesui.infrastructure.provider.ResourceProvider
import eu.europa.ec.eudi.rqesui.presentation.architecture.MviViewModel
import eu.europa.ec.eudi.rqesui.presentation.architecture.ViewEvent
import eu.europa.ec.eudi.rqesui.presentation.architecture.ViewSideEffect
import eu.europa.ec.eudi.rqesui.presentation.architecture.ViewState
import eu.europa.ec.eudi.rqesui.presentation.entities.SelectionItemUi
import eu.europa.ec.eudi.rqesui.presentation.ui.component.content.ContentErrorConfig
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.BottomSheetTextData
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

internal data class State(
    val isLoading: Boolean = false,
    val selectionItem: SelectionItemUi? = null,
    val error: ContentErrorConfig? = null,
    val isBottomSheetOpen: Boolean = false,
    val isBottomBarButtonEnabled: Boolean = false,

    val title: String,
    val subtitle: String,
    val certificatesSectionTitle: String,
    val bottomBarButtonText: String,
    val certificates: List<CertificateData> = emptyList(),
    val selectedCertificateIndex: Int = 0,

    val sheetTextData: BottomSheetTextData,
) : ViewState

internal sealed class Event : ViewEvent {
    data object Init : Event()
    data object AuthorizeServiceAndFetchCertificates : Event()
    data class CertificateSelected(val index: Int) : Event()
    data object BottomBarButtonPressed : Event()

    data object Pop : Event()
    data object DismissError : Event()

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

    data object OnSelectionItemCreated : Effect()
    data class OpenUrl(val uri: Uri) : Effect()
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
            bottomBarButtonText = resourceProvider.getLocalizedString(LocalizableKey.Sign),
            sheetTextData = getConfirmCancellationTextData(),
        )
    }

    override fun handleEvents(event: Event) {
        when (event) {
            is Event.Init -> {
                createSelectionItem(event)
            }

            is Event.AuthorizeServiceAndFetchCertificates -> {
                authorizeServiceAndFetchCertificates(event)
            }

            is Event.CertificateSelected -> {
                setState {
                    copy(
                        selectedCertificateIndex = event.index
                    )
                }
            }

            is Event.BottomBarButtonPressed -> {
                with(viewState.value) {
                    if (certificates.isNotEmpty() && selectedCertificateIndex >= 0) {
                        getCertificateAuthorizationUrl(
                            event,
                            certificates[selectedCertificateIndex],
                        )
                    }
                }
            }

            is Event.Pop -> {
                showBottomSheet()
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
        }
    }

    private fun createSelectionItem(event: Event) {
        when (val response = selectCertificateInteractor.getSelectedFile()) {
            is EudiRqesGetSelectedFilePartialState.Failure -> {
                setState {
                    copy(
                        selectionItem = null,
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
                        )
                    )
                }
            }

            is EudiRqesGetSelectedFilePartialState.Success -> {
                setState {
                    copy(
                        selectionItem = SelectionItemUi(
                            documentData = response.file,
                        )
                    )
                }
                setEffect {
                    Effect.OnSelectionItemCreated
                }
            }
        }
    }

    private fun authorizeServiceAndFetchCertificates(event: Event) {
        setState { copy(isLoading = true) }

        viewModelScope.launch {
            val response = selectCertificateInteractor.authorizeServiceAndFetchCertificates()
            when (response) {
                is SelectCertificateInteractorAuthorizeServiceAndFetchCertificatesPartialState.Failure -> {
                    setState {
                        copy(
                            certificates = emptyList(),
                            isBottomBarButtonEnabled = false,
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

                is SelectCertificateInteractorAuthorizeServiceAndFetchCertificatesPartialState.Success -> {
                    setState {
                        copy(
                            certificates = response.certificates,
                            isBottomBarButtonEnabled = true,
                            isLoading = false,
                        )
                    }
                }
            }
        }
    }

    private fun getCertificateAuthorizationUrl(event: Event, certificate: CertificateData) {
        setState { copy(isLoading = true) }

        viewModelScope.launch {
            val response = selectCertificateInteractor.getCredentialAuthorizationUrl(
                certificate = certificate,
            )
            when (response) {
                is EudiRqesGetCredentialAuthorizationUrlPartialState.Failure -> {
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

                is EudiRqesGetCredentialAuthorizationUrlPartialState.Success -> {
                    setState {
                        copy(
                            isLoading = false,
                        )
                    }
                    setEffect { Effect.OpenUrl(uri = response.authorizationUrl) }
                    setEffect { Effect.Navigation.Finish }
                }
            }
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