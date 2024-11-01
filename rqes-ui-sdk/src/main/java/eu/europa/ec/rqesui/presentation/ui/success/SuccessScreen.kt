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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import eu.europa.ec.rqesui.infrastructure.theme.values.success
import eu.europa.ec.rqesui.infrastructure.theme.values.successVariant
import eu.europa.ec.rqesui.presentation.extension.finish
import eu.europa.ec.rqesui.presentation.ui.component.SelectionItem
import eu.europa.ec.rqesui.presentation.ui.component.TextWithBadge
import eu.europa.ec.rqesui.presentation.ui.component.content.ContentHeadline
import eu.europa.ec.rqesui.presentation.ui.component.content.ContentScreen
import eu.europa.ec.rqesui.presentation.ui.component.content.ContentSubtitle
import eu.europa.ec.rqesui.presentation.ui.component.content.ScreenNavigateAction
import eu.europa.ec.rqesui.presentation.ui.component.content.SecondaryButtonContainerBottomBar
import eu.europa.ec.rqesui.presentation.ui.component.utils.SPACING_EXTRA_SMALL
import eu.europa.ec.rqesui.presentation.ui.component.utils.VSpacer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import java.net.URI

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
        onBack = { viewModel.setEvent(Event.Pop) },
        bottomBar = {
            SecondaryButtonContainerBottomBar(
                buttonText = state.buttonText,
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
                    message = state.documentName,
                    showBadge = true
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
            }
        }.collect()
    }
}