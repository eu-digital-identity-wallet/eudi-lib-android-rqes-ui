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

package eu.europa.ec.eudi.rqesui.presentation.ui.view_document

import eu.europa.ec.eudi.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.eudi.rqesui.domain.serializer.UiSerializer
import eu.europa.ec.eudi.rqesui.infrastructure.provider.ResourceProvider
import eu.europa.ec.eudi.rqesui.presentation.architecture.MviViewModel
import eu.europa.ec.eudi.rqesui.presentation.architecture.ViewEvent
import eu.europa.ec.eudi.rqesui.presentation.architecture.ViewSideEffect
import eu.europa.ec.eudi.rqesui.presentation.architecture.ViewState
import eu.europa.ec.eudi.rqesui.presentation.entities.config.ViewDocumentUiConfig
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

internal data class State(
    val config: ViewDocumentUiConfig,
    val isLoading: Boolean = false,
    val buttonText: String,
) : ViewState

internal sealed class Event : ViewEvent {
    data object Pop : Event()
    data object BottomBarButtonPressed : Event()
    data class LoadingStateChanged(val isLoading: Boolean) : Event()
}

internal sealed class Effect : ViewSideEffect {
    sealed class Navigation : Effect() {
        data object Pop : Navigation()
    }
}

@KoinViewModel
internal class ViewDocumentViewModel(
    private val resourceProvider: ResourceProvider,
    private val uiSerializer: UiSerializer,
    @InjectedParam private val serializedViewDocumentUiConfig: String,
) : MviViewModel<Event, State, Effect>() {

    override fun setInitialState(): State {
        val deserializedConfig: ViewDocumentUiConfig = uiSerializer.fromBase64(
            payload = serializedViewDocumentUiConfig,
            model = ViewDocumentUiConfig::class.java,
            parser = ViewDocumentUiConfig.Parser
        ) ?: throw RuntimeException("ViewDocumentUiConfig:: is Missing or invalid")

        return State(
            isLoading = true,
            config = deserializedConfig,
            buttonText = resourceProvider.getLocalizedString(LocalizableKey.Close),
        )
    }

    override fun handleEvents(event: Event) {
        when (event) {
            is Event.Pop -> {
                setEffect { Effect.Navigation.Pop }
            }

            is Event.BottomBarButtonPressed -> {
                setEffect { Effect.Navigation.Pop }
            }

            is Event.LoadingStateChanged -> {
                setState {
                    copy(isLoading = event.isLoading)
                }
            }
        }
    }
}