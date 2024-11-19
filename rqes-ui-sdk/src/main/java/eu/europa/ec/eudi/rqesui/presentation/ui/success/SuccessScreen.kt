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

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import eu.europa.ec.eudi.rqesui.domain.extension.toUri
import eu.europa.ec.eudi.rqesui.domain.util.safeLet
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.infrastructure.theme.values.success
import eu.europa.ec.eudi.rqesui.presentation.entities.SelectionItemUi
import eu.europa.ec.eudi.rqesui.presentation.extension.finish
import eu.europa.ec.eudi.rqesui.presentation.extension.openIntentChooser
import eu.europa.ec.eudi.rqesui.presentation.ui.component.SelectionItem
import eu.europa.ec.eudi.rqesui.presentation.ui.component.TextWithBadge
import eu.europa.ec.eudi.rqesui.presentation.ui.component.content.ContentScreen
import eu.europa.ec.eudi.rqesui.presentation.ui.component.content.ContentTitle
import eu.europa.ec.eudi.rqesui.presentation.ui.component.content.ScreenNavigateAction
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.PreviewTheme
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.ThemeModePreviews
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.OneTimeLaunchedEffect
import eu.europa.ec.eudi.rqesui.presentation.ui.component.utils.SPACING_LARGE
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
    val state = viewModel.viewState.value
    val context = LocalContext.current

    val isBottomSheetOpen = state.isBottomSheetOpen
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    ContentScreen(
        isLoading = state.isLoading,
        navigatableAction = ScreenNavigateAction.NONE,
        contentErrorConfig = state.error,
        stickyBottom = {
            WrapBottomBarSecondaryButton(
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
                    is Effect.Navigation.SwitchScreen -> navController.navigate(navigationEffect.screenRoute)
                    is Effect.Navigation.Finish -> context.finish()
                }
            },
            paddingValues = paddingValues,
            modalBottomSheetState = bottomSheetState,
        )

        if (isBottomSheetOpen) {
            state.selectionItem?.let { safeSelectionItem ->
                WrapModalBottomSheet(
                    onDismissRequest = {
                        viewModel.setEvent(Event.BottomSheet.UpdateBottomSheetState(isOpen = false))
                    },
                    sheetState = bottomSheetState
                ) {
                    SuccessSheetContent(
                        sheetContent = state.sheetContent,
                        documentUri = safeSelectionItem.documentData.uri,
                        onEventSent = { event ->
                            viewModel.setEvent(event)
                        }
                    )
                }
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
        verticalArrangement = Arrangement.spacedBy(SPACING_LARGE.dp)
    ) {

        ContentTitle(
            title = state.title,
            verticalPadding = PaddingValues(0.dp)
        )

        state.headline?.let { safeHeadline ->
            Text(
                text = safeHeadline,
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = MaterialTheme.colorScheme.success
                )
            )
        }

        safeLet(
            state.subtitle,
            state.selectionItem
        ) { subtitle, selectionItem ->

            Column {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                TextWithBadge(
                    message = selectionItem.documentData.documentName,
                    showBadge = true
                )
            }

            SelectionItem(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                ),
                data = selectionItem,
                onClick = {
                    onEventSend(
                        Event.ViewDocument(
                            documentData = selectionItem.documentData
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
        val documentName = "Document name.PDF"
        Content(
            state = State(
                title = "Sign document",
                headline = "Success",
                subtitle = "You successfully signed your document",
                selectionItem = SelectionItemUi(
                    documentData = DocumentData(
                        documentName = documentName,
                        uri = "".toUri()
                    ),
                    subtitle = "Signed by: Entrust",
                    action = "View",
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