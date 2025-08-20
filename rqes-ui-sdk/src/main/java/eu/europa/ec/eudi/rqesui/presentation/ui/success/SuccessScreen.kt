/*
 * Copyright (c) 2025 European Commission
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.europa.ec.eudi.rqesui.presentation.ui.success

import android.net.Uri
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import eu.europa.ec.eudi.rqesui.domain.extension.toUriOrEmpty
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.presentation.extension.finish
import eu.europa.ec.eudi.rqesui.presentation.extension.openIntentChooser
import eu.europa.ec.eudi.rqesui.presentation.extension.openUrl
import eu.europa.ec.eudi.rqesui.presentation.ui.component.AppIcons
import eu.europa.ec.eudi.rqesui.presentation.ui.component.SuccessCard
import eu.europa.ec.eudi.rqesui.presentation.ui.component.SuccessCardData
import eu.europa.ec.eudi.rqesui.presentation.ui.component.content.ContentHeader
import eu.europa.ec.eudi.rqesui.presentation.ui.component.content.ContentScreen
import eu.europa.ec.eudi.rqesui.presentation.ui.component.content.ScreenNavigateAction
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.PreviewTheme
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.ThemeModePreviews
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.OneTimeLaunchedEffect
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.SPACING_LARGE
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.SPACING_SMALL
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.BottomSheetTextData
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
internal fun SuccessScreen(
    navController: NavController,
    viewModel: SuccessViewModel
) {
    val state: State by viewModel.viewState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val isBottomSheetOpen = state.isBottomSheetOpen
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    ContentScreen(
        isLoading = state.isLoading,
        navigatableAction = ScreenNavigateAction.NONE,
        contentErrorConfig = state.error,
        stickyBottom = { paddingValues ->
            WrapBottomBarSecondaryButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues),
                buttonText = state.bottomBarButtonText,
                enabled = state.isBottomBarButtonEnabled,
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
                    is Effect.Navigation.SwitchScreen -> {
                        navController.navigate(navigationEffect.screenRoute)
                    }

                    is Effect.Navigation.Finish -> context.finish()
                    is Effect.Navigation.OpenRedirectUrl -> {
                        context.openUrl(navigationEffect.url)
                        context.finish()
                    }
                }
            },
            paddingValues = paddingValues,
            modalBottomSheetState = bottomSheetState,
        )

        if (isBottomSheetOpen) {
            state.successCardData?.let { safeSuccessCardData ->
                WrapModalBottomSheet(
                    onDismissRequest = {
                        viewModel.setEvent(
                            Event.BottomSheet.UpdateBottomSheetState(isOpen = false)
                        )
                    },
                    sheetState = bottomSheetState
                ) {
                    SuccessSheetContent(
                        sheetContent = state.sheetContent,
                        documentUri = safeSuccessCardData.documentData.uri,
                        onEventSent = { event ->
                            viewModel.setEvent(event)
                        }
                    )
                }
            }
        }
    }

    OneTimeLaunchedEffect {
        viewModel.setEvent(Event.Initialize)
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
        verticalArrangement = Arrangement.spacedBy(SPACING_SMALL.dp)
    ) {
        ContentHeader(
            modifier = Modifier.fillMaxWidth(),
            config = state.headerConfig,
        )

        state.successCardData?.let { safeSuccessCardData ->
            SuccessCard(
                modifier = Modifier.fillMaxWidth(),
                successCardData = safeSuccessCardData,
                onClick = {
                    onEventSend(
                        Event.ViewDocumentItemPressed(
                            documentData = safeSuccessCardData.documentData
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
                            onEventSend(
                                Event.BottomSheet.UpdateBottomSheetState(isOpen = false)
                            )
                        }
                    }
                }

                is Effect.ShowBottomSheet -> {
                    onEventSend(Event.BottomSheet.UpdateBottomSheetState(isOpen = true))
                }

                is Effect.OnSelectedFileAndQtspGot -> {
                    onEventSend(
                        Event.SignAndSaveDocument(
                            originalDocumentName = effect.selectedFile.documentName,
                            qtspName = effect.selectedQtsp.name,
                        )
                    )
                }

                is Effect.SharePdf -> {
                    context.openIntentChooser(
                        intent = effect.intent,
                        title = effect.chooserTitle,
                    )
                }
            }
        }.collect()
    }
}

@Composable
private fun SuccessSheetContent(
    sheetContent: SuccessBottomSheetContent,
    documentUri: Uri,
    onEventSent: (event: Event) -> Unit
) {
    when (sheetContent) {
        is SuccessBottomSheetContent.ShareDocument -> {
            DialogBottomSheet(
                textData = sheetContent.bottomSheetTextData,
                onPositiveClick = {
                    onEventSent(
                        Event.BottomSheet.ShareDocument.PrimaryButtonPressed(
                            documentUri = documentUri,
                        )
                    )
                },
                onNegativeClick = {
                    onEventSent(Event.BottomSheet.ShareDocument.SecondaryButtonPressed)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemeModePreviews
@Composable
private fun SuccessScreenPreview() {
    PreviewTheme {
        Content(
            state = State(
                successCardData = SuccessCardData(
                    leadingIcon = AppIcons.Verified,
                    documentData = DocumentData(
                        documentName = "File_to_be_signed.pdf",
                        uri = "mockedUri".toUriOrEmpty()
                    ),
                    actionText = "VIEW",
                ),
                bottomBarButtonText = "Close",
                sheetContent = SuccessBottomSheetContent.ShareDocument(
                    bottomSheetTextData = BottomSheetTextData(
                        title = "Sharing document?",
                        message = "Closing will redirect you back to the dashboard without saving or sharing the document.",
                        positiveButtonText = "Share",
                        negativeButtonText = "Close",
                    )
                )
            ),
            effectFlow = Channel<Effect>().receiveAsFlow(),
            onEventSend = {},
            onNavigationRequested = {},
            paddingValues = PaddingValues(all = SPACING_LARGE.dp),
            modalBottomSheetState = rememberModalBottomSheetState(),
        )
    }
}