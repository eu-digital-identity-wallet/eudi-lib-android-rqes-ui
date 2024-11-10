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

import eu.europa.ec.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.rqesui.domain.serializer.UiSerializer
import eu.europa.ec.rqesui.infrastructure.provider.ResourceProvider
import eu.europa.ec.rqesui.presentation.architecture.MviViewModel
import eu.europa.ec.rqesui.presentation.architecture.ViewEvent
import eu.europa.ec.rqesui.presentation.architecture.ViewSideEffect
import eu.europa.ec.rqesui.presentation.architecture.ViewState
import eu.europa.ec.rqesui.presentation.entities.config.ViewDocumentUiConfig
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

        data object Finish : Navigation()
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

            //TODO should it have different behavior based on the Screen that opened ViewDocument Screen?
            // (would also mean to add a new variable in ViewDocumentUiConfig)
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