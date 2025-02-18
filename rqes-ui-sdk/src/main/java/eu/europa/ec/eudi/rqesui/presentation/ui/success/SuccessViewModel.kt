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

package eu.europa.ec.eudi.rqesui.presentation.ui.success

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.viewModelScope
import eu.europa.ec.eudi.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.eudi.rqesui.domain.interactor.SuccessInteractor
import eu.europa.ec.eudi.rqesui.domain.interactor.SuccessInteractorGetSelectedFileAndQtspPartialState
import eu.europa.ec.eudi.rqesui.domain.interactor.SuccessInteractorSignAndSaveDocumentPartialState
import eu.europa.ec.eudi.rqesui.domain.serializer.UiSerializer
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.QtspData
import eu.europa.ec.eudi.rqesui.infrastructure.provider.ResourceProvider
import eu.europa.ec.eudi.rqesui.presentation.architecture.MviViewModel
import eu.europa.ec.eudi.rqesui.presentation.architecture.ViewEvent
import eu.europa.ec.eudi.rqesui.presentation.architecture.ViewSideEffect
import eu.europa.ec.eudi.rqesui.presentation.architecture.ViewState
import eu.europa.ec.eudi.rqesui.presentation.entities.config.ViewDocumentUiConfig
import eu.europa.ec.eudi.rqesui.presentation.navigation.SdkScreens
import eu.europa.ec.eudi.rqesui.presentation.navigation.helper.generateComposableArguments
import eu.europa.ec.eudi.rqesui.presentation.navigation.helper.generateComposableNavigationLink
import eu.europa.ec.eudi.rqesui.presentation.ui.component.AppIconAndTextData
import eu.europa.ec.eudi.rqesui.presentation.ui.component.AppIcons
import eu.europa.ec.eudi.rqesui.presentation.ui.component.RelyingPartyData
import eu.europa.ec.eudi.rqesui.presentation.ui.component.SuccessCardData
import eu.europa.ec.eudi.rqesui.presentation.ui.component.content.ContentErrorConfig
import eu.europa.ec.eudi.rqesui.presentation.ui.component.content.ContentHeaderConfig
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.BottomSheetTextData
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

internal data class State(
    val isLoading: Boolean = false,
    val headerConfig: ContentHeaderConfig = ContentHeaderConfig(
        appIconAndTextData = AppIconAndTextData(),
        description = null,
    ),
    val successCardData: SuccessCardData? = null,
    val error: ContentErrorConfig? = null,
    val isBottomSheetOpen: Boolean = false,
    val isBottomBarButtonEnabled: Boolean = false,
    val bottomBarButtonText: String,
    val sheetContent: SuccessBottomSheetContent,
) : ViewState

internal sealed class Event : ViewEvent {
    data object Initialize : Event()
    data class SignAndSaveDocument(
        val originalDocumentName: String,
        val qtspName: String,
    ) : Event()

    data object Pop : Event()
    data object DismissError : Event()

    data class ViewDocumentItemPressed(val documentData: DocumentData) : Event()
    data object BottomBarButtonPressed : Event()

    sealed class BottomSheet : Event() {
        data class UpdateBottomSheetState(val isOpen: Boolean) : BottomSheet()

        sealed class ShareDocument : BottomSheet() {
            data class PrimaryButtonPressed(
                val documentUri: Uri,
            ) : ShareDocument()

            data object SecondaryButtonPressed : ShareDocument()
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

    data class OnSelectedFileAndQtspGot(
        val selectedFile: DocumentData,
        val selectedQtsp: QtspData,
    ) : Effect()

    data class SharePdf(
        val intent: Intent,
        val chooserTitle: String,
    ) : Effect()
}

internal sealed class SuccessBottomSheetContent {
    data class ShareDocument(
        val bottomSheetTextData: BottomSheetTextData,
    ) : SuccessBottomSheetContent()
}

@KoinViewModel
internal class SuccessViewModel(
    private val successInteractor: SuccessInteractor,
    private val resourceProvider: ResourceProvider,
    private val uiSerializer: UiSerializer,
) : MviViewModel<Event, State, Effect>() {

    override fun setInitialState(): State {
        return State(
            bottomBarButtonText = resourceProvider.getLocalizedString(LocalizableKey.Close),
            sheetContent = SuccessBottomSheetContent.ShareDocument(bottomSheetTextData = getShareDocumentTextData()),
        )
    }

    override fun handleEvents(event: Event) {
        when (event) {
            is Event.Initialize -> {
                getSelectedFileAndQtsp(event)
            }

            is Event.SignAndSaveDocument -> {
                signAndSaveDocument(event, event.originalDocumentName, event.qtspName)
            }

            is Event.Pop -> {
                setEffect { Effect.Navigation.Finish }
            }

            is Event.DismissError -> {
                setState { copy(error = null) }
            }

            is Event.BottomBarButtonPressed -> {
                showBottomSheet(
                    sheetContent = SuccessBottomSheetContent.ShareDocument(
                        bottomSheetTextData = getShareDocumentTextData()
                    )
                )
            }

            is Event.ViewDocumentItemPressed -> {
                navigateToViewDocument(event.documentData)
            }

            is Event.BottomSheet.UpdateBottomSheetState -> {
                setState {
                    copy(
                        isBottomSheetOpen = event.isOpen
                    )
                }
            }

            is Event.BottomSheet.ShareDocument.PrimaryButtonPressed -> {
                hideBottomSheet()

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, event.documentUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                setEffect {
                    Effect.SharePdf(
                        intent = shareIntent,
                        chooserTitle = resourceProvider.getLocalizedString(LocalizableKey.Share),
                    )
                }
            }

            is Event.BottomSheet.ShareDocument.SecondaryButtonPressed -> {
                hideBottomSheet()
                setEffect { Effect.Navigation.Finish }
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

    private fun signAndSaveDocument(event: Event, originalDocumentName: String, qtspName: String) {
        setState { copy(isLoading = true) }

        viewModelScope.launch {
            when (val response = successInteractor.signAndSaveDocument(originalDocumentName)) {
                is SuccessInteractorSignAndSaveDocumentPartialState.Failure -> {
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
                            successCardData = null,
                            isBottomBarButtonEnabled = false,
                            isLoading = false,
                        )
                    }
                }

                is SuccessInteractorSignAndSaveDocumentPartialState.Success -> {
                    val headerConfig = ContentHeaderConfig(
                        appIconAndTextData = AppIconAndTextData(),
                        description = resourceProvider.getLocalizedString(LocalizableKey.SuccessDescription),
                        relyingPartyData = getHeaderConfigData(qtspName = qtspName)
                    )

                    val successCard = SuccessCardData(
                        leadingIcon = AppIcons.Verified,
                        documentData = response.savedDocument,
                        actionText = resourceProvider.getLocalizedString(LocalizableKey.View),
                    )

                    setState {
                        copy(
                            headerConfig = headerConfig,
                            successCardData = successCard,
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

    private fun getShareDocumentTextData(): BottomSheetTextData {
        return BottomSheetTextData(
            title = resourceProvider.getLocalizedString(LocalizableKey.SharingDocument),
            message = resourceProvider.getLocalizedString(LocalizableKey.CloseSharingMessage),
            positiveButtonText = resourceProvider.getLocalizedString(LocalizableKey.Share),
            negativeButtonText = resourceProvider.getLocalizedString(LocalizableKey.Close),
        )
    }

    private fun showBottomSheet(sheetContent: SuccessBottomSheetContent) {
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

    private fun getHeaderConfigData(qtspName: String): RelyingPartyData {
        return RelyingPartyData(
            isVerified = true,
            name = qtspName,
        )
    }
}