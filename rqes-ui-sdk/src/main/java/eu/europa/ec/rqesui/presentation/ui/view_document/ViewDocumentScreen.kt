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

package eu.europa.ec.rqesui.presentation.ui.view_document

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import eu.europa.ec.rqesui.domain.extension.toUri
import eu.europa.ec.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.rqesui.infrastructure.theme.values.success
import eu.europa.ec.rqesui.presentation.entities.config.ViewDocumentUiConfig
import eu.europa.ec.rqesui.presentation.extension.finish
import eu.europa.ec.rqesui.presentation.ui.component.AppIcons
import eu.europa.ec.rqesui.presentation.ui.component.content.ContentScreen
import eu.europa.ec.rqesui.presentation.ui.component.content.ScreenNavigateAction
import eu.europa.ec.rqesui.presentation.ui.component.content.SecondaryButtonContainerBottomBar
import eu.europa.ec.rqesui.presentation.ui.component.content.ToolbarAction
import eu.europa.ec.rqesui.presentation.ui.component.content.ToolbarConfig
import eu.europa.ec.rqesui.presentation.ui.component.pdf.PdfViewer
import eu.europa.ec.rqesui.presentation.ui.component.preview.PreviewTheme
import eu.europa.ec.rqesui.presentation.ui.component.preview.ThemeModePreviews
import eu.europa.ec.rqesui.presentation.ui.component.utils.SPACING_LARGE
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

@Composable
internal fun ViewDocumentScreen(
    navController: NavController,
    viewModel: ViewDocumentViewModel,
) {
    val context = LocalContext.current
    val state = viewModel.viewState.value

    val toolbarActions = listOf(
        ToolbarAction(
            icon = AppIcons.Verified,
            customTint = MaterialTheme.colorScheme.success,
            onClick = {}
        )
    ).takeIf { state.config.isSigned } ?: emptyList()

    ContentScreen(
        isLoading = state.isLoading,
        toolBarConfig = ToolbarConfig(
            title = state.documentName,
            actions = toolbarActions,
            hasShadow = true
        ),
        navigatableAction = ScreenNavigateAction.BACKABLE,
        onBack = {
            viewModel.setEvent(Event.Pop)
        },
        bottomBar = {
            SecondaryButtonContainerBottomBar(
                buttonText = state.buttonText,
                onButtonClick = {
                    viewModel.setEvent(Event.Finish)
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
                    is Effect.Navigation.Pop -> navController.navigateUp()
                }
            },
            paddingValues = paddingValues,
        )
    }
}

@Composable
private fun Content(
    state: State,
    effectFlow: Flow<Effect>,
    onEventSend: (Event) -> Unit,
    onNavigationRequested: (Effect.Navigation) -> Unit,
    paddingValues: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.Top
    ) {
        state.documentUri?.let { uri ->
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {
                PdfViewer(
                    modifier = Modifier.fillMaxSize(),
                    documentUri = uri,
                    onLoadingListener = { isLoading ->
                        onEventSend(
                            Event.LoadingStateChanged(isLoading = isLoading)
                        )
                    }
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        effectFlow.onEach { effect ->
            when (effect) {
                is Effect.Navigation -> onNavigationRequested(effect)
            }
        }.collect()
    }
}

@ThemeModePreviews
@Composable
private fun ViewDocumentScreenPreview() {
    PreviewTheme {
        Content(
            state = State(
                isLoading = false,
                documentName = "Document.pdf",
                documentUri = null,
                config = ViewDocumentUiConfig(
                    isSigned = true,
                    documentData = DocumentData(
                        documentName = "Document.pdf",
                        uri = "uriPath".toUri()
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