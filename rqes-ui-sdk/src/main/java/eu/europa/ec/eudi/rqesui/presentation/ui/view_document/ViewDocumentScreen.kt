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

package eu.europa.ec.eudi.rqesui.presentation.ui.view_document

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.github.barteksc.pdfviewer.PDFView
import eu.europa.ec.eudi.rqesui.domain.extension.toUriOrEmpty
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.infrastructure.theme.values.ThemeColors
import eu.europa.ec.eudi.rqesui.infrastructure.theme.values.divider
import eu.europa.ec.eudi.rqesui.presentation.entities.config.ViewDocumentUiConfig
import eu.europa.ec.eudi.rqesui.presentation.ui.component.AppIcons
import eu.europa.ec.eudi.rqesui.presentation.ui.component.content.ContentScreen
import eu.europa.ec.eudi.rqesui.presentation.ui.component.content.ScreenNavigateAction
import eu.europa.ec.eudi.rqesui.presentation.ui.component.content.ToolbarAction
import eu.europa.ec.eudi.rqesui.presentation.ui.component.content.ToolbarConfig
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.PreviewTheme
import eu.europa.ec.eudi.rqesui.presentation.ui.component.preview.ThemeModePreviews
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
    val state: State by viewModel.viewState.collectAsStateWithLifecycle()

    ContentScreen(
        isLoading = state.isLoading,
        toolBarConfig = rememberToolbarConfig(
            isSigned = state.config.isSigned,
            state = state
        ),
        navigatableAction = ScreenNavigateAction.BACKABLE,
        onBack = {
            viewModel.setEvent(Event.Pop)
        }
    ) { paddingValues ->
        Content(
            state = state,
            effectFlow = viewModel.effect,
            onEventSend = { viewModel.setEvent(it) },
            onNavigationRequested = { navigationEffect ->
                when (navigationEffect) {
                    is Effect.Navigation.Pop -> navController.popBackStack()
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.divider)
            .padding(top = paddingValues.calculateTopPadding())
    ) {
        state.config.documentData.let { file ->
            AndroidView(
                modifier = Modifier.matchParentSize(),
                factory = { context -> PDFView(context, null) },
                update = { pdfView ->
                    pdfView
                        .fromUri(file.uri)
                        .enableAnnotationRendering(true)
                        .onLoad {
                            onEventSend(Event.LoadingStateChanged(isLoading = false))
                        }
                        .load()
                }
            )
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

@Composable
private fun rememberToolbarConfig(
    isSigned: Boolean,
    state: State
): ToolbarConfig {
    return remember(isSigned) {
        val toolbarActions = if (isSigned) {
            listOf(
                ToolbarAction(
                    icon = AppIcons.Verified,
                    customTint = ThemeColors.success,
                    clickable = false,
                    onClick = {}
                )
            )
        } else {
            emptyList()
        }

        ToolbarConfig(
            actions = toolbarActions,
            hasShadow = true,
            title = state.config.documentData.documentName
        )
    }
}

@ThemeModePreviews
@Composable
private fun ViewDocumentScreenPreview() {
    PreviewTheme {
        Content(
            state = State(
                isLoading = false,
                config = ViewDocumentUiConfig(
                    isSigned = true,
                    documentData = DocumentData(
                        documentName = "Document.pdf",
                        uri = "uriPath".toUriOrEmpty()
                    )
                ),
                buttonText = "Close"
            ),
            effectFlow = Channel<Effect>().receiveAsFlow(),
            onEventSend = {},
            onNavigationRequested = {},
            paddingValues = PaddingValues()
        )
    }
}