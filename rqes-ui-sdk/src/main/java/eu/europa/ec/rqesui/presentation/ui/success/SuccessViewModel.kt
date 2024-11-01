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

import eu.europa.ec.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.rqesui.domain.interactor.SuccessInteractor
import eu.europa.ec.rqesui.infrastructure.EudiRQESUi
import eu.europa.ec.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.rqesui.infrastructure.provider.ResourceProvider
import eu.europa.ec.rqesui.presentation.architecture.MviViewModel
import eu.europa.ec.rqesui.presentation.architecture.ViewEvent
import eu.europa.ec.rqesui.presentation.architecture.ViewSideEffect
import eu.europa.ec.rqesui.presentation.architecture.ViewState
import eu.europa.ec.rqesui.presentation.entities.SelectionItemUi
import eu.europa.ec.rqesui.presentation.entities.config.ViewDocumentUiConfig
import eu.europa.ec.rqesui.presentation.navigation.SdkScreens
import eu.europa.ec.rqesui.presentation.navigation.helper.generateComposableArguments
import eu.europa.ec.rqesui.presentation.navigation.helper.generateComposableNavigationLink
import eu.europa.ec.rqesui.presentation.serializer.UiSerializer
import org.koin.android.annotation.KoinViewModel

internal data class State(
    val isLoading: Boolean = false,

    val documentName: String = "",
    val headline: String = "",
    val subtitle: String = "",
    val options: List<SelectionItemUi> = emptyList(),
    val buttonText: String = "",
) : ViewState

internal sealed class Event : ViewEvent {
    data object Pop : Event()
    data object Finish : Event()
    data object BottomBarButtonPressed : Event()

    data class ViewDocument(val documentData: DocumentData) : Event()
}

internal sealed class Effect : ViewSideEffect {
    sealed class Navigation : Effect() {
        data class SwitchScreen(val screenRoute: String) : Navigation()
        data object Finish : Navigation()
    }
}

@KoinViewModel
internal class SuccessViewModel(
    private val resourceProvider: ResourceProvider,
    private val successInteractor: SuccessInteractor,
    private val uiSerializer: UiSerializer,
) : MviViewModel<Event, State, Effect>() {

    override fun setInitialState(): State {
        val selectedQTSP = EudiRQESUi.getEudiRQESUiConfig().qtsps.getOrNull(0)
        val qtspName = selectedQTSP?.qtspName ?: "QTSP"
        val subtitleWithQTSP =
            resourceProvider.getLocalizedStringWithArgs(
                localizableKey = LocalizableKey.SignedBy,
                arguments = listOf(qtspName)
            )

        return State(
            documentName = successInteractor.getDocumentName(),
            headline = resourceProvider.getLocalizedString(LocalizableKey.Success),
            subtitle = resourceProvider.getLocalizedString(LocalizableKey.SuccessfullySignedDocument),
            options = listOf(
                SelectionItemUi(
                    title = successInteractor.getDocumentName(),
                    subTitle = subtitleWithQTSP,
                    action = "VIEW"
                )
            ),
            buttonText = resourceProvider.getLocalizedString(LocalizableKey.Close)
        )
    }

    override fun handleEvents(event: Event) {
        when (event) {
            is Event.BottomBarButtonPressed -> {
                setEffect {
                    Effect.Navigation.Finish
                }
            }

            is Event.Finish -> {
                setEffect { Effect.Navigation.Finish }
            }

            is Event.Pop -> {
                setEffect { Effect.Navigation.Finish }
            }

            is Event.ViewDocument -> {
                navigateToViewDocument(event.documentData)
            }
        }
    }

    private fun navigateToViewDocument(documentData: DocumentData) {
        val screenRoute = generateComposableNavigationLink(
            screen = SdkScreens.ViewDocument,
            arguments = generateComposableArguments(
                arguments = mapOf(
                    ViewDocumentUiConfig.serializedKeyName to uiSerializer.toBase64(
                        model = ViewDocumentUiConfig(
                            isSigned = true,
                            documentData = documentData
                        ),
                        parser = ViewDocumentUiConfig.Parser
                    )
                )
            )
        )
        setEffect {
            Effect.Navigation.SwitchScreen(screenRoute = screenRoute)
        }
    }
}