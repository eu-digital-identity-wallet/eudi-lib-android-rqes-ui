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

import eu.europa.ec.rqesui.infrastructure.provider.ResourceProvider
import eu.europa.ec.rqesui.presentation.architecture.MviViewModel
import eu.europa.ec.rqesui.presentation.architecture.ViewEvent
import eu.europa.ec.rqesui.presentation.architecture.ViewSideEffect
import eu.europa.ec.rqesui.presentation.architecture.ViewState
import eu.europa.ec.rqesui.presentation.entities.config.ViewDocumentUiConfig
import eu.europa.ec.rqesui.presentation.serializer.UiSerializer
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

internal data class State(
    val isLoading: Boolean = false,
    val config: ViewDocumentUiConfig,
) : ViewState

internal sealed class Event : ViewEvent

internal sealed class Effect : ViewSideEffect

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
            config = deserializedConfig,
        )
    }

    override fun handleEvents(event: Event) {
        TODO("Not yet implemented")
    }

}