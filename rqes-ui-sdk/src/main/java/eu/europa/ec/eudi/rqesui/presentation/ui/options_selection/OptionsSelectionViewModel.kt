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

package eu.europa.ec.eudi.rqesui.presentation.ui.options_selection

import android.net.Uri
import androidx.lifecycle.viewModelScope
import eu.europa.ec.eudi.rqes.core.RQESService
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetCredentialAuthorizationUrlPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetQtspsPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetSelectedFilePartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetServiceAuthorizationUrlPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesSetSelectedQtspPartialState
import eu.europa.ec.eudi.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.eudi.rqesui.domain.interactor.OptionsSelectionInteractor
import eu.europa.ec.eudi.rqesui.domain.interactor.OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState
import eu.europa.ec.eudi.rqesui.domain.interactor.OptionsSelectionInteractorGetSelectedFileAndQtspPartialState
import eu.europa.ec.eudi.rqesui.domain.serializer.UiSerializer
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.CertificateData
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.QtspData
import eu.europa.ec.eudi.rqesui.infrastructure.provider.ResourceProvider
import eu.europa.ec.eudi.rqesui.infrastructure.theme.values.ThemeColors
import eu.europa.ec.eudi.rqesui.presentation.architecture.MviViewModel
import eu.europa.ec.eudi.rqesui.presentation.architecture.ViewEvent
import eu.europa.ec.eudi.rqesui.presentation.architecture.ViewSideEffect
import eu.europa.ec.eudi.rqesui.presentation.architecture.ViewState
import eu.europa.ec.eudi.rqesui.presentation.entities.ButtonActionUi
import eu.europa.ec.eudi.rqesui.presentation.entities.ModalOptionUi
import eu.europa.ec.eudi.rqesui.presentation.entities.SelectionOptionUi
import eu.europa.ec.eudi.rqesui.presentation.entities.config.OptionsSelectionUiConfig
import eu.europa.ec.eudi.rqesui.presentation.entities.config.ViewDocumentUiConfig
import eu.europa.ec.eudi.rqesui.presentation.navigation.SdkScreens
import eu.europa.ec.eudi.rqesui.presentation.navigation.helper.generateComposableArguments
import eu.europa.ec.eudi.rqesui.presentation.navigation.helper.generateComposableNavigationLink
import eu.europa.ec.eudi.rqesui.presentation.ui.component.AppIcons
import eu.europa.ec.eudi.rqesui.presentation.ui.component.content.ContentErrorConfig
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.BottomSheetTextData
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

internal data class State(
    val isLoading: Boolean = false,
    val config: OptionsSelectionUiConfig,
    val currentScreenSelectionState: String = QTSP_SELECTION_STATE,

    val documentSelectionItem: SelectionOptionUi<Event.ViewDocumentItemPressed>? = null,
    val qtspServiceSelectionItem: SelectionOptionUi<Event.RqesServiceSelectionItemPressed>? = null,
    val certificateSelectionItem: SelectionOptionUi<Event.CertificateSelectionItemPressed>? = null,

    val error: ContentErrorConfig? = null,
    val isBottomSheetOpen: Boolean = false,

    val title: String,
    val sheetContent: OptionsSelectionBottomSheetContent,
    val selectedQtspIndex: Int,
    val selectedCertificateIndex: Int,

    val certificateDataList: List<CertificateData> = emptyList(),
    val bottomBarButtonAction: ButtonActionUi<Event.BottomBarButtonPressed>? = null,
    val isBottomBarButtonVisible: Boolean = false,
) : ViewState

internal const val QTSP_SELECTION_STATE = "QTSP_SELECTION"
internal const val CERTIFICATE_SELECTION_STATE = "CERTIFICATE_SELECTION"

internal sealed class Event : ViewEvent {
    data class Initialize(val screenSelectionState: String) : Event()
    data object Pop : Event()
    data object Finish : Event()
    data object DismissError : Event()
    data class BottomBarButtonPressed(val uri: Uri) : Event()

    data class ViewDocumentItemPressed(val documentData: DocumentData) : Event()
    data object RqesServiceSelectionItemPressed : Event()
    data object CertificateSelectionItemPressed : Event()

    data object AuthorizeServiceAndFetchCertificates : Event()
    data class FetchServiceAuthorizationUrl(val service: RQESService) : Event()

    sealed class BottomSheet : Event() {
        data class UpdateBottomSheetState(val isOpen: Boolean) : BottomSheet()

        sealed class CancelSignProcess : BottomSheet() {
            data object PrimaryButtonPressed : CancelSignProcess()
            data object SecondaryButtonPressed : CancelSignProcess()
        }

        data class QtspIndexSelectedOnRadioButtonPressed(val index: Int) : BottomSheet()
        data class QtspSelectedOnDoneButtonPressed(val qtspData: QtspData) : BottomSheet()

        data class CertificateIndexSelectedOnRadioButtonPressed(val index: Int) : BottomSheet()
        data class CertificateSelectedOnDoneButtonPressed(val certificateData: CertificateData) :
            BottomSheet()

        data object CancelQtspSelection : BottomSheet()
        data object CancelCertificateSelection : BottomSheet()
    }
}

internal sealed class Effect : ViewSideEffect {
    sealed class Navigation : Effect() {
        data class SwitchScreen(val screenRoute: String) : Navigation()
        data object Finish : Navigation()
    }

    data object ShowBottomSheet : Effect()
    data object CloseBottomSheet : Effect()

    data class OnSelectedQtspUpdated(val service: RQESService) : Effect()
    data object OnCertificateSelectionItemCreated : Effect()

    data class OpenUrl(val uri: Uri) : Effect()
}

internal sealed class OptionsSelectionBottomSheetContent {
    data class ConfirmCancellation(
        val bottomSheetTextData: BottomSheetTextData,
    ) : OptionsSelectionBottomSheetContent()

    data class SelectQTSP(
        val bottomSheetTextData: BottomSheetTextData,
        val options: List<ModalOptionUi<Event>>,
        val selectedIndex: Int,
    ) : OptionsSelectionBottomSheetContent()

    data class SelectCertificate(
        val bottomSheetTextData: BottomSheetTextData,
        val options: List<ModalOptionUi<Event>>,
        val selectedIndex: Int
    ) : OptionsSelectionBottomSheetContent()
}

@KoinViewModel
internal class OptionsSelectionViewModel(
    private val optionsSelectionInteractor: OptionsSelectionInteractor,
    private val resourceProvider: ResourceProvider,
    private val uiSerializer: UiSerializer,
    @InjectedParam private val serializedOptionsSelectionUiConfig: String,
) : MviViewModel<Event, State, Effect>() {

    override fun setInitialState(): State {
        val deserializedConfig: OptionsSelectionUiConfig = uiSerializer.fromBase64(
            payload = serializedOptionsSelectionUiConfig,
            model = OptionsSelectionUiConfig::class.java,
            parser = OptionsSelectionUiConfig.Parser
        ) ?: throw RuntimeException("OptionsSelectionUiConfig:: is Missing or invalid")

        return State(
            title = resourceProvider.getLocalizedString(LocalizableKey.SignDocument),
            sheetContent = OptionsSelectionBottomSheetContent.ConfirmCancellation(
                bottomSheetTextData = getConfirmCancellationTextData()
            ),
            selectedQtspIndex = 0,
            selectedCertificateIndex = 0,
            config = deserializedConfig
        )
    }

    override fun handleEvents(event: Event) {
        when (event) {
            is Event.Initialize -> {
                createFileSelectionItem(event)

                when (event.screenSelectionState) {
                    QTSP_SELECTION_STATE -> {
                        createQTSPSelectionItem(event)
                    }

                    CERTIFICATE_SELECTION_STATE -> {
                        createQTSPSelectionItemOnSelectCertificateStep(event = event)
                        createCertificateSelectionItemOnSelectCertificateStep(event = event)
                    }
                }
            }

            is Event.Pop -> {
                showBottomSheet(
                    sheetContent = OptionsSelectionBottomSheetContent.ConfirmCancellation(
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

            is Event.ViewDocumentItemPressed -> {
                navigateToViewDocument(documentData = event.documentData)
            }

            is Event.BottomBarButtonPressed -> {
                setEffect { Effect.OpenUrl(uri = event.uri) }
                setEffect { Effect.Navigation.Finish }
            }

            is Event.BottomSheet.QtspSelectedOnDoneButtonPressed -> {
                val response = optionsSelectionInteractor.updateQtspUserSelection(event.qtspData)
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
                        updateQTSPSelectionItem(
                            qtspData = event.qtspData,
                            rqesService = response.service
                        )
                    }
                }
            }

            is Event.FetchServiceAuthorizationUrl -> {
                fetchServiceAuthorizationUrl(event, event.service)
            }

            is Event.AuthorizeServiceAndFetchCertificates -> {
                authorizeServiceAndFetchCertificates(event)
            }

            is Event.BottomSheet.CancelQtspSelection -> {
                hideBottomSheet()
            }

            is Event.BottomSheet.QtspIndexSelectedOnRadioButtonPressed -> {
                setState {
                    copy(selectedQtspIndex = event.index)
                }
            }

            is Event.RqesServiceSelectionItemPressed -> {
                getQtsps(event)
            }

            is Event.CertificateSelectionItemPressed -> {
                val bottomSheetOptions: List<ModalOptionUi<Event>> =
                    viewState.value.certificateDataList.mapIndexed { index, certificateData ->
                        ModalOptionUi(
                            title = certificateData.name,
                            trailingIcon = null,
                            event = Event.BottomSheet.CertificateSelectedOnDoneButtonPressed(
                                certificateData
                            ),
                            radioButtonSelected = index == viewState.value.selectedQtspIndex
                        )
                    }

                showBottomSheet(
                    sheetContent = OptionsSelectionBottomSheetContent.SelectCertificate(
                        bottomSheetTextData = getSelectCertificateTextData(),
                        options = bottomSheetOptions,
                        selectedIndex = 0
                    )
                )
            }

            is Event.BottomSheet.CertificateIndexSelectedOnRadioButtonPressed -> {
                setState {
                    copy(selectedCertificateIndex = event.index)
                }
            }

            is Event.BottomSheet.CertificateSelectedOnDoneButtonPressed -> {
                hideBottomSheet()
                with(viewState.value) {
                    if (certificateDataList.isNotEmpty() && selectedCertificateIndex >= 0) {
                        getCertificateAuthorizationUrl(
                            event,
                            certificateDataList[selectedCertificateIndex],
                        )
                    }
                }
            }

            is Event.BottomSheet.CancelCertificateSelection -> {
                hideBottomSheet()
            }
        }
    }

    private fun createFileSelectionItem(event: Event) {
        when (val response = optionsSelectionInteractor.getSelectedFile()) {
            is EudiRqesGetSelectedFilePartialState.Failure -> {
                setState {
                    copy(
                        documentSelectionItem = null,
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
                        documentSelectionItem = SelectionOptionUi(
                            overlineText = resourceProvider.getLocalizedString(LocalizableKey.SelectDocumentTitle),
                            mainText = response.file.documentName,
                            subtitle = resourceProvider.getLocalizedString(LocalizableKey.SelectDocumentSubtitle),
                            actionText = resourceProvider.getLocalizedString(LocalizableKey.View),
                            leadingIcon = AppIcons.StepOne,
                            leadingIconTint = ThemeColors.success,
                            trailingIcon = AppIcons.KeyboardArrowRight,
                            enabled = true,
                            event = Event.ViewDocumentItemPressed(
                                documentData = response.file
                            )
                        )
                    )
                }
            }
        }
    }

    private fun createQTSPSelectionItem(event: Event) {
        when (val response = optionsSelectionInteractor.getSelectedFile()) {
            is EudiRqesGetSelectedFilePartialState.Failure -> {
                setState {
                    copy(
                        qtspServiceSelectionItem = null,
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
                        qtspServiceSelectionItem = SelectionOptionUi(
                            overlineText = null,
                            mainText = resourceProvider.getLocalizedString(LocalizableKey.SelectSigningService),
                            subtitle = resourceProvider.getLocalizedString(LocalizableKey.SelectSigningServiceSubtitle),
                            actionText = null,
                            leadingIcon = AppIcons.StepTwo,
                            trailingIcon = AppIcons.KeyboardArrowRight,
                            enabled = true,
                            event = Event.RqesServiceSelectionItemPressed
                        )
                    )
                }
            }
        }
    }

    private fun createQTSPSelectionItemOnSelectCertificateStep(event: Event) {
        when (val response = optionsSelectionInteractor.getSelectedFileAndQtsp()) {
            is OptionsSelectionInteractorGetSelectedFileAndQtspPartialState.Failure -> {
                setState {
                    copy(
                        qtspServiceSelectionItem = null,
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

            is OptionsSelectionInteractorGetSelectedFileAndQtspPartialState.Success -> {
                setState {
                    copy(
                        qtspServiceSelectionItem = SelectionOptionUi(
                            overlineText = resourceProvider.getLocalizedString(
                                LocalizableKey.SigningService
                            ),
                            mainText = response.selectedQtsp.name,
                            subtitle = resourceProvider.getLocalizedString(LocalizableKey.SelectSigningServiceSubtitle),
                            actionText = null,
                            leadingIcon = AppIcons.StepTwo,
                            leadingIconTint = ThemeColors.success,
                            trailingIcon = AppIcons.KeyboardArrowRight,
                            enabled = false,
                            event = Event.RqesServiceSelectionItemPressed
                        )
                    )
                }
            }
        }
    }

    private fun updateQTSPSelectionItem(
        qtspData: QtspData,
        rqesService: RQESService
    ) {
        setState {
            copy(
                qtspServiceSelectionItem = qtspServiceSelectionItem?.copy(
                    overlineText = resourceProvider.getLocalizedString(
                        LocalizableKey.SigningService
                    ),
                    mainText = qtspData.name,
                    leadingIconTint = ThemeColors.success
                )
            )
        }

        setEffect {
            Effect.OnSelectedQtspUpdated(service = rqesService)
        }
    }

    private fun createCertificateSelectionItemOnSelectCertificateStep(event: Event) {
        when (val response = optionsSelectionInteractor.getSelectedFile()) {
            is EudiRqesGetSelectedFilePartialState.Failure -> {
                setState {
                    copy(
                        certificateSelectionItem = null,
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
                        certificateSelectionItem = SelectionOptionUi(
                            overlineText = null,
                            mainText = resourceProvider.getLocalizedString(LocalizableKey.SelectSigningCertificateTitle),
                            subtitle = resourceProvider.getLocalizedString(LocalizableKey.SelectCertificateSubtitle),
                            leadingIcon = AppIcons.StepThree,
                            trailingIcon = AppIcons.KeyboardArrowRight,
                            enabled = true,
                            event = Event.CertificateSelectionItemPressed
                        )
                    )
                }
                setEffect {
                    Effect.OnCertificateSelectionItemCreated
                }
            }
        }
    }

    private fun getQtsps(event: Event) {
        when (val response = optionsSelectionInteractor.getQtsps()) {
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
                    response.qtsps.mapIndexed { index, qtspData ->
                        ModalOptionUi(
                            title = qtspData.name,
                            trailingIcon = null,
                            event = Event.BottomSheet.QtspSelectedOnDoneButtonPressed(qtspData),
                            radioButtonSelected = index == viewState.value.selectedQtspIndex
                        )
                    }

                showBottomSheet(
                    sheetContent = OptionsSelectionBottomSheetContent.SelectQTSP(
                        bottomSheetTextData = getSelectQTSPTextData(),
                        options = bottomSheetOptions,
                        selectedIndex = viewState.value.selectedQtspIndex,
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
            val response = optionsSelectionInteractor.getServiceAuthorizationUrl(
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

    private fun authorizeServiceAndFetchCertificates(event: Event) {
        setState { copy(isLoading = true) }

        viewModelScope.launch {
            val response = optionsSelectionInteractor.authorizeServiceAndFetchCertificates()
            when (response) {
                is OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState.Failure -> {
                    setState {
                        copy(
                            certificateDataList = emptyList(),
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

                is OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState.Success -> {
                    setState {
                        copy(
                            certificateDataList = response.certificates,
                            isLoading = false,
                        )
                    }
                }
            }
        }
    }

    private fun getCertificateAuthorizationUrl(
        event: Event,
        certificateData: CertificateData
    ) {
        setState { copy(isLoading = true) }

        viewModelScope.launch {
            val response = optionsSelectionInteractor.getCredentialAuthorizationUrl(
                certificateData = certificateData,
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
                        val buttonAction = ButtonActionUi(
                            buttonText = resourceProvider.getLocalizedString(LocalizableKey.Continue),
                            event = Event.BottomBarButtonPressed(uri = response.authorizationUrl)
                        )
                        copy(
                            isLoading = false,
                            bottomBarButtonAction = buttonAction,
                            isBottomBarButtonVisible = true
                        )
                    }
                    updateCertificateSelectionItem(certificateData = certificateData)
                }
            }
        }
    }

    private fun updateCertificateSelectionItem(certificateData: CertificateData) {
        setState {
            copy(
                certificateSelectionItem = certificateSelectionItem?.copy(
                    overlineText = resourceProvider.getLocalizedString(
                        LocalizableKey.SigningCertificate
                    ),
                    mainText = certificateData.name,
                    leadingIconTint = ThemeColors.success,
                    enabled = false
                )
            )
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
            positiveButtonText = resourceProvider.getLocalizedString(LocalizableKey.Done),
            negativeButtonText = resourceProvider.getLocalizedString(LocalizableKey.Cancel),
        )
    }

    private fun getSelectCertificateTextData(): BottomSheetTextData {
        return BottomSheetTextData(
            title = resourceProvider.getLocalizedString(LocalizableKey.SigningCertificates),
            message = resourceProvider.getLocalizedString(LocalizableKey.SelectSigningCertificateSubtitle),
            positiveButtonText = resourceProvider.getLocalizedString(LocalizableKey.Done),
            negativeButtonText = resourceProvider.getLocalizedString(LocalizableKey.Cancel),
        )
    }

    private fun showBottomSheet(sheetContent: OptionsSelectionBottomSheetContent) {
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