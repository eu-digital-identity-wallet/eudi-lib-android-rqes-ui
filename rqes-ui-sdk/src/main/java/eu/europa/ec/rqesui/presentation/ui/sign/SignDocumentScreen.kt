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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import eu.europa.ec.rqesui.R
import eu.europa.ec.rqesui.infrastructure.theme.value.Typography
import eu.europa.ec.rqesui.presentation.entities.SignDocumentOptionItemUi
import eu.europa.ec.rqesui.presentation.extension.finish
import eu.europa.ec.rqesui.presentation.ui.sign.model.SignDocumentSelectionData
import eu.europa.ec.rqesui.presentation.ui.sign.model.SignDocumentSelectionItem
import eu.europa.ec.rqesui.uilogic.component.content.ContentScreen
import eu.europa.ec.rqesui.uilogic.component.content.ContentTitleWithSubtitle
import eu.europa.ec.rqesui.uilogic.component.content.ScreenNavigateAction
import eu.europa.ec.rqesui.uilogic.component.preview.PreviewTheme
import eu.europa.ec.rqesui.uilogic.component.preview.ThemeModePreviews
import eu.europa.ec.rqesui.uilogic.component.utils.HSpacer
import eu.europa.ec.rqesui.uilogic.component.utils.SPACING_LARGE
import eu.europa.ec.rqesui.uilogic.component.utils.VSpacer
import eu.europa.ec.rqesui.uilogic.component.wrap.DialogBottomSheet
import eu.europa.ec.rqesui.uilogic.component.wrap.WrapModalBottomSheet
import eu.europa.ec.rqesui.uilogic.component.wrap.WrapPrimaryButton
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import java.net.URI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignDocumentScreen(
    navController: NavController,
    viewModel: SignDocumentViewModel
) {
    val state = viewModel.viewState.value
    val context = LocalContext.current

    val isBottomSheetOpen = state.isBottomSheetOpen
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    ContentScreen(
        isLoading = state.isLoading,
        navigatableAction = state.navigatableAction,
        onBack = state.onBackAction,
        contentErrorConfig = state.error,
    ) { paddingValues ->
        Content(
            state = state,
            effectFlow = viewModel.effect,
            onEventSend = { viewModel.setEvent(it) },
            onNavigationRequested = { navigationEffect ->
                when (navigationEffect) {
                    is Effect.Navigation.Pop -> navController.popBackStack()
                    is Effect.Navigation.Finish -> context.finish()
                }
            },
            paddingValues = paddingValues,
            modalBottomSheetState = bottomSheetState
        )

        if (isBottomSheetOpen) {
            WrapModalBottomSheet(
                onDismissRequest = {
                    viewModel.setEvent(Event.BottomSheet.UpdateBottomSheetState(isOpen = false))
                },
                sheetState = bottomSheetState
            ) {
                SignDocumentSheetContent(
                    onEventSent = { event ->
                        viewModel.setEvent(event)
                    }
                )
            }
        }
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
    modalBottomSheetState: SheetState? = null
) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(paddingValues),
            verticalArrangement = Arrangement.Top
        ) {
            ContentTitleWithSubtitle(
                title = state.title,
                subtitle = state.subtitle,
            )

            VSpacer.Custom(space = 24)

            LazyColumn {
                state.options.forEach { option ->
                    item {
                        SignDocumentSelectionItem(
                            modifier = Modifier.defaultMinSize(minHeight = 76.dp),
                            data = SignDocumentSelectionData(
                                text = option.text,
                                label = stringResource(R.string.sign_document_pdf_selection_item_label)
                            ),
                            enabled = true,
                            onClick = {
                                onEventSend(
                                    Event.OpenDocument(URI("uriValue"))
                                )
                            }
                        )

                        VSpacer.Medium()
                    }
                }
            }
        }

        ButtonContainerBottomBar(
            onPositiveClick = {
                onEventSend(
                    Event.SignDocument(URI("uriValue"))
                )
            }
        )
    }

    LaunchedEffect(Unit) {
        effectFlow.onEach { effect ->
            when (effect) {
                is Effect.Navigation -> onNavigationRequested(effect)
            }
        }.collect()
    }
}

@Composable
private fun ButtonContainerBottomBar(
    onPositiveClick: () -> Unit? = {}
) {
    Column(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = Color(0xFFCAC4D0)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(89.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HSpacer.Large()

            WrapPrimaryButton(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 40.dp),
                onClick = { onPositiveClick.invoke() }
            ) {
                Text(
                    text = stringResource(R.string.generic_sign_button_text),
                    style = Typography.labelLarge
                )
            }

            HSpacer.Large()
        }
    }
}


@Composable
private fun SignDocumentSheetContent(
    onEventSent: (event: Event) -> Unit
) {
    DialogBottomSheet(
        title = stringResource(id = R.string.sign_document_bottom_sheet_cancel_confirmation_title),
        message = stringResource(id = R.string.sign_document_bottom_sheet_cancel_confirmation_subtitle),
        positiveButtonText = stringResource(id = R.string.sign_document_bottom_sheet_continue_button_text),
        negativeButtonText = stringResource(id = R.string.sign_document_bottom_sheet_cancel_button_text),
        onPositiveClick = {
            onEventSent(
                Event.BottomSheet.UpdateBottomSheetState(isOpen = false)
            )
        },
        onNegativeClick = {
            onEventSent(
                Event.BottomSheet.CancelConfirmed
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemeModePreviews
@Composable
private fun DashboardSignDocumentScreenPreview() {
    PreviewTheme {
        Content(
            state = State(
                navigatableAction = ScreenNavigateAction.CANCELABLE,
                title = "Sign a document",
                subtitle = "Select a document to add in your EUDI Wallet",
                options = listOf(
                    SignDocumentOptionItemUi(
                        text = "From your device"
                    )
                )
            ),
            effectFlow = Channel<Effect>().receiveAsFlow(),
            onEventSend = {},
            onNavigationRequested = {},
            paddingValues = PaddingValues(all = SPACING_LARGE.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemeModePreviews
@Composable
private fun SignDocumentScreenPreview() {
    PreviewTheme {
        Content(
            state = State(
                navigatableAction = ScreenNavigateAction.NONE,
                title = "Sign a document",
                subtitle = "Select a document from your device or scan QR",

                options = listOf(
                    SignDocumentOptionItemUi(
                        text = "From your device"
                    ),
                )
            ),
            effectFlow = Channel<Effect>().receiveAsFlow(),
            onEventSend = {},
            onNavigationRequested = {},
            paddingValues = PaddingValues(all = SPACING_LARGE.dp),
        )
    }
}