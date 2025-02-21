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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import eu.europa.ec.eudi.rqesui.domain.extension.toUri
import eu.europa.ec.eudi.rqesui.domain.util.safeLet
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.presentation.entities.ButtonActionUi
import eu.europa.ec.eudi.rqesui.presentation.entities.SelectionOptionUi
import eu.europa.ec.eudi.rqesui.presentation.entities.config.OptionsSelectionScreenState
import eu.europa.ec.eudi.rqesui.presentation.entities.config.OptionsSelectionUiConfig
import eu.europa.ec.eudi.rqesui.presentation.extension.finish
import eu.europa.ec.eudi.rqesui.presentation.extension.openUrl
import eu.europa.ec.eudi.rqesui.presentation.ui.component.AppIcons
import eu.europa.ec.eudi.rqesui.presentation.ui.component.SelectionItem
import eu.europa.ec.eudi.rqesui.presentation.ui.component.content.ContentScreen
import eu.europa.ec.eudi.rqesui.presentation.ui.component.content.ScreenNavigateAction
import eu.europa.ec.eudi.rqesui.presentation.ui.component.content.ToolbarConfig
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.PreviewTheme
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.ThemeModePreviews
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.OneTimeLaunchedEffect
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.SPACING_LARGE
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.VSpacer
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.BottomSheetTextData
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.BottomSheetWithOptionsList
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.DialogBottomSheet
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.WrapBottomBarSecondaryButton
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.WrapModalBottomSheet
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OptionsSelectionScreen(
    navController: NavController,
    viewModel: OptionsSelectionViewModel
) {
    val state: State by viewModel.viewState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val isBottomSheetOpen = state.isBottomSheetOpen
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    ContentScreen(
        isLoading = state.isLoading,
        navigatableAction = ScreenNavigateAction.CANCELABLE,
        onBack = { viewModel.setEvent(Event.Pop) },
        contentErrorConfig = state.error,
        toolBarConfig = ToolbarConfig(title = state.title),
        stickyBottom = { paddingValues ->
            if (state.isBottomBarButtonVisible) {
                state.bottomBarButtonAction?.let { safeButtonAction ->
                    WrapBottomBarSecondaryButton(
                        stickyBottomContentModifier = Modifier
                            .fillMaxWidth()
                            .padding(paddingValues),
                        buttonText = safeButtonAction.buttonText,
                        onButtonClick = {
                            viewModel.setEvent(
                                safeButtonAction.event
                            )
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Content(
            state = state,
            effectFlow = viewModel.effect,
            onEventSend = { viewModel.setEvent(it) },
            onNavigationRequested = { navigationEffect ->
                when (navigationEffect) {
                    is Effect.Navigation.SwitchScreen -> navController.navigate(navigationEffect.screenRoute)
                    is Effect.Navigation.Finish -> context.finish()
                }
            },
            paddingValues = paddingValues,
            modalBottomSheetState = bottomSheetState,
        )

        if (isBottomSheetOpen) {
            WrapModalBottomSheet(
                onDismissRequest = {
                    viewModel.setEvent(Event.BottomSheet.UpdateBottomSheetState(isOpen = false))
                },
                sheetState = bottomSheetState
            ) {
                OptionsSelectionSheetContent(
                    sheetContent = state.sheetContent,
                    state = state,
                    onEventSent = { event ->
                        viewModel.setEvent(event)
                    }
                )
            }
        }
    }

    OneTimeLaunchedEffect {
        viewModel.setEvent(
            Event.Initialize(
                screenSelectionState = state.config.optionsSelectionScreenState
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    state: State,
    effectFlow: Flow<Effect>,
    onEventSend: (Event) -> Unit,
    onNavigationRequested: (Effect.Navigation) -> Unit,
    paddingValues: PaddingValues,
    modalBottomSheetState: SheetState,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(scrollState)
    ) {

        safeLet(
            state.documentSelectionItem,
            state.documentSelectionItem?.event
        ) { safeSelectionItem, selectionItemEvent ->
            SelectionItemWithDivider(
                selectionItemData = safeSelectionItem,
                showDividerAbove = false,
                onClick = onEventSend,
            )
        }

        state.qtspServiceSelectionItem?.let { safeSelectionItem ->
            SelectionItemWithDivider(
                selectionItemData = safeSelectionItem,
                showDividerAbove = true,
                onClick = onEventSend,
            )
        }

        AnimatedVisibility(visible = state.certificateDataList.isNotEmpty()) {
            state.certificateSelectionItem?.let { safeSelectionItem ->
                SelectionItemWithDivider(
                    selectionItemData = safeSelectionItem,
                    showDividerAbove = true,
                    onClick = onEventSend,
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        effectFlow.onEach { effect ->
            when (effect) {
                is Effect.Navigation -> onNavigationRequested(effect)

                is Effect.CloseBottomSheet -> {
                    coroutineScope.launch {
                        modalBottomSheetState.hide()
                    }.invokeOnCompletion {
                        if (!modalBottomSheetState.isVisible) {
                            onEventSend(Event.BottomSheet.UpdateBottomSheetState(isOpen = false))
                        }
                    }
                }

                is Effect.ShowBottomSheet -> {
                    onEventSend(Event.BottomSheet.UpdateBottomSheetState(isOpen = true))
                }

                is Effect.OpenUrl -> {
                    context.openUrl(uri = effect.uri)
                }

                is Effect.OnSelectedQtspUpdated -> {
                    onEventSend(
                        Event.FetchServiceAuthorizationUrl(service = effect.service)
                    )
                }

                is Effect.OnCertificateSelectionItemCreated -> {
                    onEventSend(Event.AuthorizeServiceAndFetchCertificates)
                }
            }
        }.collect()
    }
}

@Composable
private fun OptionsSelectionSheetContent(
    sheetContent: OptionsSelectionBottomSheetContent,
    state: State,
    onEventSent: (event: Event) -> Unit,
) {
    when (sheetContent) {
        is OptionsSelectionBottomSheetContent.ConfirmCancellation -> {
            DialogBottomSheet(
                textData = sheetContent.bottomSheetTextData,
                onPositiveClick = {
                    onEventSent(Event.BottomSheet.CancelSignProcess.PrimaryButtonPressed)
                },
                onNegativeClick = {
                    onEventSent(Event.BottomSheet.CancelSignProcess.SecondaryButtonPressed)
                }
            )
        }

        is OptionsSelectionBottomSheetContent.SelectQTSP -> {
            BottomSheetWithOptionsList(
                textData = sheetContent.bottomSheetTextData,
                options = sheetContent.options,
                onIndexSelected = { selectedIndex ->
                    onEventSent(
                        Event.BottomSheet.QtspIndexSelectedOnRadioButtonPressed(
                            index = selectedIndex
                        )
                    )
                },
                onPositiveClick = {
                    sheetContent.options.getOrNull(state.selectedQtspIndex)?.let { safeOption ->
                        onEventSent(safeOption.event)
                    }
                },
                onNegativeClick = {
                    onEventSent(
                        Event.BottomSheet.CancelQtspSelection
                    )
                }
            )
        }

        is OptionsSelectionBottomSheetContent.SelectCertificate -> {
            BottomSheetWithOptionsList(
                textData = sheetContent.bottomSheetTextData,
                options = sheetContent.options,
                onIndexSelected = { selectedIndex ->
                    onEventSent(
                        Event.BottomSheet.CertificateIndexSelectedOnRadioButtonPressed(
                            index = selectedIndex
                        )
                    )
                },
                onPositiveClick = {
                    sheetContent.options
                        .getOrNull(state.selectedCertificateIndex)?.let { safeOption ->
                            onEventSent(safeOption.event)
                        }
                },
                onNegativeClick = {
                    onEventSent(
                        Event.BottomSheet.CancelCertificateSelection
                    )
                }
            )
        }
    }
}

@Composable
private fun <T : Event> SelectionItemWithDivider(
    selectionItemData: SelectionOptionUi<T>,
    showDividerAbove: Boolean,
    onClick: (T) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (showDividerAbove) {
            ListDivider()
            VSpacer.Medium()
        }

        SelectionItem(
            modifier = Modifier.fillMaxWidth(),
            selectionItemData = selectionItemData,
            onClick = onClick
        )
    }
}


@Composable
private fun ListDivider() {
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemeModePreviews
@Composable
private fun OptionsSelectionScreenContentPreview() {
    val dummyEventForPreview = Event.ViewDocumentItemPressed(
        documentData = DocumentData(
            documentName = "File_to_be_signed.pdf",
            uri = "mockedUri".toUri()
        )
    )

    PreviewTheme {
        Content(
            state = State(
                title = "Sign document",
                documentSelectionItem = SelectionOptionUi(
                    overlineText = "Document",
                    mainText = "File_to_be_signed.pdf",
                    subtitle = "Choose a document from your device to sign electronically.",
                    actionText = "VIEW",
                    enabled = true,
                    event = dummyEventForPreview,
                    leadingIcon = AppIcons.StepOne,
                    trailingIcon = AppIcons.KeyboardArrowRight,
                ),
                sheetContent = OptionsSelectionBottomSheetContent.ConfirmCancellation(
                    bottomSheetTextData = BottomSheetTextData(
                        title = "title",
                        message = "message",
                    )
                ),
                bottomBarButtonAction = ButtonActionUi(
                    buttonText = "Continue",
                    event = Event.BottomBarButtonPressed(uri = "mockedUri".toUri())
                ),
                selectedQtspIndex = 0,
                selectedCertificateIndex = 0,
                config = OptionsSelectionUiConfig(
                    optionsSelectionScreenState = OptionsSelectionScreenState.QtspSelection,
                ),
            ),
            effectFlow = Channel<Effect>().receiveAsFlow(),
            onEventSend = {},
            onNavigationRequested = {},
            paddingValues = PaddingValues(all = SPACING_LARGE.dp),
            modalBottomSheetState = rememberModalBottomSheetState(),
        )
    }
}