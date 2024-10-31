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

package eu.europa.ec.rqesui.presentation.ui.success

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import eu.europa.ec.rqesui.R
import eu.europa.ec.rqesui.infrastructure.theme.values.devider
import eu.europa.ec.rqesui.infrastructure.theme.values.success
import eu.europa.ec.rqesui.infrastructure.theme.values.successVariant
import eu.europa.ec.rqesui.presentation.extension.finish
import eu.europa.ec.rqesui.presentation.ui.component.AppIcons
import eu.europa.ec.rqesui.presentation.ui.component.SelectionItem
import eu.europa.ec.rqesui.presentation.ui.component.content.ContentHeadline
import eu.europa.ec.rqesui.presentation.ui.component.content.ContentScreen
import eu.europa.ec.rqesui.presentation.ui.component.content.ContentSubtitle
import eu.europa.ec.rqesui.presentation.ui.component.content.ScreenNavigateAction
import eu.europa.ec.rqesui.presentation.ui.component.preview.ThemeModePreviews
import eu.europa.ec.rqesui.presentation.ui.component.utils.SPACING_EXTRA_SMALL
import eu.europa.ec.rqesui.presentation.ui.component.utils.SPACING_LARGE
import eu.europa.ec.rqesui.presentation.ui.component.utils.VSpacer
import eu.europa.ec.rqesui.presentation.ui.component.wrap.DialogBottomSheet
import eu.europa.ec.rqesui.presentation.ui.component.wrap.WrapIcon
import eu.europa.ec.rqesui.presentation.ui.component.wrap.WrapModalBottomSheet
import eu.europa.ec.rqesui.presentation.ui.component.wrap.WrapSecondaryButton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.net.URI

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
        navigatableAction = ScreenNavigateAction.CANCELABLE,
        onBack = { viewModel.setEvent(Event.Pop) },
        bottomBar = {
            SecondaryButtonContainerBottomBar(
                buttonText = state.buttonText,
                onPositiveClick = {
                    viewModel.setEvent(
                        Event.CloseButtonPressed
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
                SuccessSheetContent(
                    sheetContent = state.sheetContent,
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
    modalBottomSheetState: SheetState,
) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.Top
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Top
        ) {
            ContentHeadline(
                headline = state.headline,
                textColor = MaterialTheme.colorScheme.success
            )

            VSpacer.Large()

            Column(verticalArrangement = Arrangement.spacedBy(SPACING_EXTRA_SMALL.dp)) {
                ContentSubtitle(
                    subtitle = state.subtitle
                )
                TextWithBadge(
                    documentTitle = state.documentName
                )
            }

            VSpacer.Large()

            LazyColumn {
                state.options.forEach { option ->
                    item {
                        SelectionItem(
                            modifier = Modifier.wrapContentHeight(),
                            data = option,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.successVariant
                            ),
                            onClick = {
                                onEventSend(
                                    Event.ViewDocument(
                                        documentUri = URI("uriValue")
                                    )
                                )
                            }
                        )

                        VSpacer.Medium()
                    }
                }
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
                            onEventSend(
                                Event.BottomSheet.UpdateBottomSheetState(isOpen = false)
                            )
                        }
                    }
                }

                is Effect.ShowBottomSheet -> {
                    onEventSend(
                        Event.BottomSheet.UpdateBottomSheetState(isOpen = true)
                    )
                }
            }
        }.collect()
    }
}

@Composable
private fun SuccessSheetContent(
    sheetContent: SuccessBottomSheetContent,
    onEventSent: (event: Event) -> Unit
) {
    when (sheetContent) {
        is SuccessBottomSheetContent.ConfirmCancellation -> {
            DialogBottomSheet(
                title = stringResource(id = R.string.sign_document_bottom_sheet_cancel_confirmation_title),
                message = stringResource(id = R.string.sign_document_bottom_sheet_cancel_confirmation_subtitle),
                positiveButtonText = stringResource(id = R.string.sign_document_bottom_sheet_continue_button_text),
                negativeButtonText = stringResource(id = R.string.sign_document_bottom_sheet_cancel_button_text),
                onPositiveClick = {
                    onEventSent(
                        Event.BottomSheet.CancelSignProcess.PrimaryButtonPressed
                    )
                },
                onNegativeClick = {
                    onEventSent(
                        Event.BottomSheet.CancelSignProcess.SecondaryButtonPressed
                    )
                }
            )
        }
    }
}

@Composable
internal fun SecondaryButtonContainerBottomBar(
    buttonText: String,
    onPositiveClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxWidth()
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.devider
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    all = SPACING_LARGE.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WrapSecondaryButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onPositiveClick.invoke() }
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun TextWithBadge(
    documentTitle: String,
    isSigned: Boolean = true
) {
    val inlineContentMap = mapOf(
        "badgeIconId" to InlineTextContent(
            Placeholder(20.sp, 20.sp, PlaceholderVerticalAlign.TextCenter)
        ) {
            WrapIcon(
                iconData = AppIcons.Verified,
                customTint = MaterialTheme.colorScheme.success
            )
        }
    )

    val textWithBadgeData = TextWithBadgeData(
        textAfterBadge = documentTitle,
        isSigned = isSigned
    )

    Text(
        modifier = Modifier
            .offset(x = -SPACING_EXTRA_SMALL.dp)
            .takeIf { isSigned } ?: Modifier,
        text = textWithBadgeData.annotatedString,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        ),
        inlineContent = inlineContentMap
    )
}

internal data class TextWithBadgeData(
    private val textAfterBadge: String? = null,
    private val isSigned: Boolean
) {
    val annotatedString = buildAnnotatedString {
        if (isSigned) {
            append(" ")
            appendInlineContent(id = "badgeIconId")
            append(" ")
        }
        if (!textAfterBadge.isNullOrEmpty()) {
            append(textAfterBadge)
        }
    }
}

@ThemeModePreviews
@Composable
private fun TextWithBadgePreview() {
    TextWithBadge(
        documentTitle = "Document_title.PDF",
        isSigned = true
    )
}
