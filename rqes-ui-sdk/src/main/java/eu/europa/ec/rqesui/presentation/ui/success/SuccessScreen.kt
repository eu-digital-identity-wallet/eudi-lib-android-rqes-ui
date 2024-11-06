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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import eu.europa.ec.rqesui.domain.extension.toUri
import eu.europa.ec.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.rqesui.infrastructure.theme.values.successVariant
import eu.europa.ec.rqesui.presentation.entities.SelectionItemUi
import eu.europa.ec.rqesui.presentation.extension.finish
import eu.europa.ec.rqesui.presentation.ui.component.SelectionItem
import eu.europa.ec.rqesui.presentation.ui.component.TextWithBadge
import eu.europa.ec.rqesui.presentation.ui.component.content.ContentHeadline
import eu.europa.ec.rqesui.presentation.ui.component.content.ContentScreen
import eu.europa.ec.rqesui.presentation.ui.component.content.ContentSubtitle
import eu.europa.ec.rqesui.presentation.ui.component.content.ScreenNavigateAction
import eu.europa.ec.rqesui.presentation.ui.component.preview.PreviewTheme
import eu.europa.ec.rqesui.presentation.ui.component.preview.ThemeModePreviews
import eu.europa.ec.rqesui.presentation.ui.component.utils.SPACING_LARGE
import eu.europa.ec.rqesui.presentation.ui.component.utils.VSpacer
import eu.europa.ec.rqesui.presentation.ui.component.wrap.WrapBottomBarSecondaryButton
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

@Composable
internal fun SuccessScreen(
    navController: NavController,
    viewModel: SuccessViewModel
) {
    val state = viewModel.viewState.value
    val context = LocalContext.current

    ContentScreen(
        isLoading = state.isLoading,
        navigatableAction = ScreenNavigateAction.CANCELABLE,
        onBack = {
            //TODO What should happen here? Pop? Finish? Other?
            viewModel.setEvent(Event.Pop)
        },
        bottomBar = {
            WrapBottomBarSecondaryButton(
                buttonText = state.bottomBarButtonText,
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
            .padding(paddingValues)
    ) {
        ContentHeadline(
            headline = state.headline
        )

        VSpacer.Large()

        ContentSubtitle(
            subtitle = state.subtitle
        )

        TextWithBadge(
            message = state.selectionItem.documentData.documentName,
            showBadge = true
        )

        VSpacer.Large()

        SelectionItem(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.successVariant
            ),
            data = state.selectionItem,
            onClick = {
                onEventSend(
                    Event.ViewDocument(
                        documentData = state.selectionItem.documentData
                    )
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

@ThemeModePreviews
@Composable
private fun SuccessScreenPreview() {
    PreviewTheme {
        val documentName = "Document name.PDF"
        Content(
            state = State(
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
                bottomBarButtonText = "Close"
            ),
            effectFlow = Channel<Effect>().receiveAsFlow(),
            onEventSend = {},
            onNavigationRequested = {},
            paddingValues = PaddingValues(all = SPACING_LARGE.dp),
        )
    }
}