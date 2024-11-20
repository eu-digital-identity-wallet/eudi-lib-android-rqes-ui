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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import eu.europa.ec.eudi.rqesui.domain.extension.toUri
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.presentation.entities.SelectionItemUi
import eu.europa.ec.eudi.rqesui.presentation.extension.finish
import eu.europa.ec.eudi.rqesui.presentation.extension.openUrl
import eu.europa.ec.eudi.rqesui.presentation.ui.component.SelectionItem
import eu.europa.ec.eudi.rqesui.presentation.ui.component.content.ContentScreen
import eu.europa.ec.eudi.rqesui.presentation.ui.component.content.ContentTitle
import eu.europa.ec.eudi.rqesui.presentation.ui.component.content.ScreenNavigateAction
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.PreviewTheme
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.ThemeModePreviews
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.OneTimeLaunchedEffect
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.SPACING_LARGE
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.VSpacer
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.BottomSheetTextData
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.BottomSheetWithOptionsList
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.DialogBottomSheet
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.WrapBottomBarPrimaryButton
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.WrapModalBottomSheet
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectQtspScreen(
    navController: NavController,
    viewModel: SelectQtspViewModel
) {
    val state = viewModel.viewState.value
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
        stickyBottom = { paddingValues ->
            WrapBottomBarPrimaryButton(
                buttonText = state.bottomBarButtonText,
                padding = paddingValues,
                onButtonClick = {
                    viewModel.setEvent(
                        Event.BottomBarButtonPressed
                    )
                }
            )
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
                SelectQtspSheetContent(
                    sheetContent = state.sheetContent,
                    onEventSent = { event ->
                        viewModel.setEvent(event)
                    }
                )
            }
        }
    }

    OneTimeLaunchedEffect {
        viewModel.setEvent(Event.Init)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.Top
    ) {

        ContentTitle(
            title = state.title,
            subtitle = state.subtitle,
        )

        VSpacer.Large()

        state.selectionItem?.let { safeSelectionItem ->
            SelectionItem(
                modifier = Modifier.fillMaxWidth(),
                data = safeSelectionItem,
                onClick = {
                    onEventSend(
                        Event.ViewDocument(
                            documentData = safeSelectionItem.documentData
                        )
                    )
                }
            )
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
                    onEventSend(Event.FetchServiceAuthorizationUrl(service = effect.service))
                }
            }
        }.collect()
    }
}

@Composable
private fun SelectQtspSheetContent(
    sheetContent: SelectQtspBottomSheetContent,
    onEventSent: (event: Event) -> Unit
) {
    when (sheetContent) {
        is SelectQtspBottomSheetContent.ConfirmCancellation -> {
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

        is SelectQtspBottomSheetContent.SelectQTSP -> {
            BottomSheetWithOptionsList(
                textData = sheetContent.bottomSheetTextData,
                options = sheetContent.options,
                onEventSent = onEventSent
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemeModePreviews
@Composable
private fun SelectQtspScreenPreview() {
    PreviewTheme {
        Content(
            state = State(
                title = "Sign a document",
                subtitle = "Select a document from your device or scan QR",
                selectionItem = SelectionItemUi(
                    documentData = DocumentData(
                        documentName = "Document name.PDF",
                        uri = "".toUri()
                    ),
                    action = "VIEW",
                ),
                sheetContent = SelectQtspBottomSheetContent.ConfirmCancellation(
                    bottomSheetTextData = BottomSheetTextData(
                        title = "title",
                        message = "message",
                    )
                ),
                bottomBarButtonText = "Sign",
            ),
            effectFlow = Channel<Effect>().receiveAsFlow(),
            onEventSend = {},
            onNavigationRequested = {},
            paddingValues = PaddingValues(all = SPACING_LARGE.dp),
            modalBottomSheetState = rememberModalBottomSheetState(),
        )
    }
}