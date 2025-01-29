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

package eu.europa.ec.eudi.rqesui.presentation.ui.select_qtsp

import android.net.Uri
import eu.europa.ec.eudi.rqes.core.RQESService
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetQtspsPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetSelectedFilePartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetServiceAuthorizationUrlPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesSetSelectedQtspPartialState
import eu.europa.ec.eudi.rqesui.domain.entities.error.EudiRQESUiError
import eu.europa.ec.eudi.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.eudi.rqesui.domain.extension.toUri
import eu.europa.ec.eudi.rqesui.domain.interactor.SelectQtspInteractor
import eu.europa.ec.eudi.rqesui.domain.serializer.UiSerializer
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.QtspData
import eu.europa.ec.eudi.rqesui.infrastructure.provider.ResourceProvider
import eu.europa.ec.eudi.rqesui.presentation.entities.ModalOptionUi
import eu.europa.ec.eudi.rqesui.presentation.entities.SelectionItemUi
import eu.europa.ec.eudi.rqesui.presentation.entities.config.ViewDocumentUiConfig
import eu.europa.ec.eudi.rqesui.presentation.navigation.SdkScreens
import eu.europa.ec.eudi.rqesui.presentation.navigation.helper.generateComposableArguments
import eu.europa.ec.eudi.rqesui.presentation.navigation.helper.generateComposableNavigationLink
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.BottomSheetTextData
import eu.europa.ec.eudi.rqesui.util.CoroutineTestRule
import eu.europa.ec.eudi.rqesui.util.mockedAuthorizationUrl
import eu.europa.ec.eudi.rqesui.util.mockedDocumentName
import eu.europa.ec.eudi.rqesui.util.mockedLocalFileUri
import eu.europa.ec.eudi.rqesui.util.mockedPlainFailureMessage
import eu.europa.ec.eudi.rqesui.util.mockedQtspName
import eu.europa.ec.eudi.rqesui.util.runFlowTest
import eu.europa.ec.eudi.rqesui.util.runTest
import junit.framework.TestCase.assertNull
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TestSelectQtspViewModel {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @Mock
    private lateinit var selectQtspInteractor: SelectQtspInteractor

    @Mock
    private lateinit var resourceProvider: ResourceProvider

    @Mock
    private lateinit var uiSerializer: UiSerializer

    @Mock
    private lateinit var qtspData: QtspData

    @Mock
    private lateinit var rqesService: RQESService

    @Mock
    private lateinit var documentData: DocumentData

    private val documentFileUri = Uri.parse(mockedLocalFileUri)

    private lateinit var viewModel: SelectQtspViewModel

    private lateinit var autoCloseable: AutoCloseable

    @Before
    fun setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this)
        viewModel = SelectQtspViewModel(selectQtspInteractor, resourceProvider, uiSerializer)
        mockLocalizedStrings(resourceProvider)
    }

    @After
    fun after() {
        autoCloseable.close()
    }

    //region setInitialState
    // Case 1
    // The function setInitialState() is called to initialize the ViewModel's state with default values.
    // Case 1 Expected Result:
    // 1. The title is set to the localized string for "SignDocument".
    // 2. The subtitle is set to the localized string for "ConfirmSelectionTitle".
    // 3. The bottom bar button text is set to the localized string for "Sign".
    // 4. The bottom sheet content is set with a "ConfirmCancellation" structure containing localized
    // strings for CancelSignProcessTitle, CancelSignProcessSubtitle, CancelSignProcessPrimaryText,
    // CancelSignProcessSecondaryText.
    @Test
    fun `Given Case 1, When setInitialState is called, Then the expected result is returned`() {
        // Act
        viewModel.setInitialState()

        // Assert
        val expectedState = State(
            isLoading = false,
            selectionItem = null,
            error = null,
            isBottomSheetOpen = false,
            title = resourceProvider.getLocalizedString(LocalizableKey.SignDocument),
            subtitle = resourceProvider.getLocalizedString(LocalizableKey.ConfirmSelectionTitle),
            bottomBarButtonText = resourceProvider.getLocalizedString(LocalizableKey.Sign),
            sheetContent = SelectQtspBottomSheetContent.ConfirmCancellation(
                bottomSheetTextData = BottomSheetTextData(
                    title = resourceProvider.getLocalizedString(LocalizableKey.CancelSignProcessTitle),
                    message = resourceProvider.getLocalizedString(LocalizableKey.CancelSignProcessSubtitle),
                    positiveButtonText = resourceProvider.getLocalizedString(LocalizableKey.CancelSignProcessPrimaryText),
                    negativeButtonText = resourceProvider.getLocalizedString(LocalizableKey.CancelSignProcessSecondaryText)
                )
            )
        )
        assertEquals(expectedState, viewModel.viewState.value)
    }
    //endregion

    //region setEvent
    // Case 1
    // The function `createSelectionItem` is tested to ensure that when it sets the success state,
    // the ViewModel updates the `selectionItem` in the state correctly.
    // Case 1 Expected Result:
    // 1. The `selectQtspInteractor.getSelectedFile()` function returns a successful state with `documentData`.
    // 2. The `viewModel.setEvent(Event.Init)` function is called, triggering the creation of a `SelectionItemUi`.
    // 3. The `selectionItem` in the ViewModel's state is updated to reflect the successful selection,
    // containing the expected `documentData` and `View` action string.
    @Test
    fun `Given Case 1, When setEvent is called, Then the expected result is returned`() {
        // Arrange
        whenever(selectQtspInteractor.getSelectedFile())
            .thenReturn(EudiRqesGetSelectedFilePartialState.Success(file = documentData))

        // Act
        viewModel.setEvent(Event.Init)

        // Assert
        assertEquals(
            SelectionItemUi(
                documentData = documentData,
                action = resourceProvider.getLocalizedString(LocalizableKey.View),
                subtitle = null
            ),
            viewModel.viewState.value.selectionItem
        )
    }

    // Case 2
    // The function `setEvent` is tested to ensure that when the event `Event.BottomBarButtonPressed` is triggered
    // Case 2 Expected Result:
    // 1. The `selectQtspInteractor.getQtsps()` function returns a successful state with a list of QTSP data.
    // 2. The `viewModel.setEvent(Event.BottomBarButtonPressed)` function is called, triggering the update of the `sheetContent`.
    // 3. The `sheetContent` in the ViewModel's state is updated to reflect a `SelectQtspBottomSheetContent.SelectQTSP`.
    // 4. The `bottomSheetTextData` is set with localized string values for the title and message.
    // 5. The `options` in the `sheetContent` are a list of `ModalOptionUi<Event>` objects, each corresponding to a QTSP data entry
    @Test
    fun `Given Case 2, When setEvent is called, Then the expected result is returned`() {
        // Arrange
        val mockedQtspDataList = listOf(qtspData)
        whenever(qtspData.name).thenReturn(mockedQtspName)
        whenever(selectQtspInteractor.getQtsps())
            .thenReturn(EudiRqesGetQtspsPartialState.Success(qtsps = mockedQtspDataList))

        // Act
        viewModel.setEvent(Event.BottomBarButtonPressed)

        // Assert
        val bottomSheetOptions: List<ModalOptionUi<Event>> =
            mockedQtspDataList.map { qtspData ->
                ModalOptionUi(
                    title = qtspData.name,
                    trailingIcon = null,
                    event = Event.BottomSheet.QtspSelected(qtspData)
                )
            }
        val sheetContent = SelectQtspBottomSheetContent.SelectQTSP(
            bottomSheetTextData = BottomSheetTextData(
                title = resourceProvider.getLocalizedString(LocalizableKey.SelectServiceTitle),
                message = resourceProvider.getLocalizedString(LocalizableKey.SelectServiceSubtitle),
            ),
            options = bottomSheetOptions
        )

        assertEquals(sheetContent, viewModel.viewState.value.sheetContent)
    }

    // Case 3
    // The function `setEvent` is tested to ensure that when the event `Event.FetchServiceAuthorizationUrl` is triggered
    // and a failure response is returned from the `mockGetServiceAuthorizationUrlCall`
    // Case 3 Expected Result:
    // 1. The `mockGetServiceAuthorizationUrlCall` is called with a response that simulates a failure,
    //    returning an error.
    // 2. The `setEvent` call with event of `Event.FetchServiceAuthorizationUrl` triggers a service authorization fetch action.
    // 3. The `viewModel.viewState.value.error` is updated with the error returned in the failure response.
    // 4. The test asserts that the `error` in the `viewState` is not null, ensuring that the failure
    //    is correctly reflected in the ViewModel's state.
    @Test
    fun `Given Case 3, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val response = EudiRqesGetServiceAuthorizationUrlPartialState.Failure(
                error = EudiRQESUiError(mockedPlainFailureMessage)
            )
            mockGetServiceAuthorizationUrlCall(response = response)

            // Act
            viewModel.setEvent(Event.FetchServiceAuthorizationUrl(rqesService))

            // Assert
            assertNotNull(viewModel.viewState.value.error)
        }

    // Case 4
    // Function setEvent() is called with an Event.Finish event.
    // Case 4 Expected Result:
    // 1. The effect should trigger a navigation finish action.
    @Test
    fun `Given Case 4, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Act
            viewModel.setEvent(Event.Finish)

            // Assert
            viewModel.effect.runFlowTest {
                assertEquals(Effect.Navigation.Finish, awaitItem())
            }
        }

    // Case 5
    // Function setEvent() is called with an Event.DismissError event.
    // Case 5 Expected Result:
    // 1. The view state should have its error field set to null, indicating that the error has been cleared.
    @Test
    fun `Given Case 5, When setEvent is called, Then the expected result is returned`() {
        // Act
        viewModel.setEvent(Event.DismissError)

        // Assert
        assertNull(viewModel.viewState.value.error)
    }

    // Case 6
    // Function setEvent() is called with an Event.BottomSheet.UpdateBottomSheetState event,
    // setting isOpen to true.
    // Case 6 Expected Result:
    // 1. The bottom sheet should be opened and the view state's `isBottomSheetOpen` field should be true.
    @Test
    fun `Given Case 6, When setEvent is called, Then the expected result is returned`() {
        // Act
        viewModel.setEvent(Event.BottomSheet.UpdateBottomSheetState(isOpen = true))

        // Assert
        assertTrue(viewModel.viewState.value.isBottomSheetOpen)
    }

    // Case 7
    // Function setEvent() is called with an Event.BottomSheet.CancelSignProcess.PrimaryButtonPressed event.
    // Case 7 Expected Result:
    // 1. The bottom sheet should be closed and the view state's `isBottomSheetOpen` field should be false.
    @Test
    fun `Given Case 7, When setEvent is called, Then the expected result is returned`() {
        // Act
        viewModel.setEvent(Event.BottomSheet.CancelSignProcess.PrimaryButtonPressed)

        // Assert
        assertFalse(viewModel.viewState.value.isBottomSheetOpen)
    }

    // Case 8
    // Function setEvent() is called with an Event.BottomSheet.CancelSignProcess.SecondaryButtonPressed event.
    // Case 8 Expected Result:
    // 1. The navigation effect should trigger a finish action.
    @Test
    fun `Given Case 8, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Act
            viewModel.setEvent(Event.BottomSheet.CancelSignProcess.SecondaryButtonPressed)

            // Assert
            viewModel.effect.runFlowTest {
                assertEquals(Effect.Navigation.Finish, awaitItem())
            }
        }

    // Case 9
    // Function setEvent() is called with an Event.ViewDocument event, with
    // documentData object as argument.
    // Case 9 Expected Result:
    // 1. The navigation effect should trigger a screen switch and the expected screen route
    // based on the document data should be provided.
    @Test
    fun `Given Case 9, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val documentData =
                DocumentData(documentName = mockedDocumentName, uri = documentFileUri)

            // Act
            viewModel.setEvent(Event.ViewDocument(documentData))

            // Assert
            viewModel.effect.runFlowTest {
                val expectedScreenRoute = mockScreenRouteGeneration(documentData)
                val expectedEffect =
                    Effect.Navigation.SwitchScreen(screenRoute = expectedScreenRoute)
                assertEquals(expectedEffect, awaitItem())
            }
        }

    // Case 10
    // Function setEvent() is called with an Event.BottomSheet.QtspSelected event, with
    // qtstData as argument.
    // Case 10 Expected Result:
    // 1. The BottomSheet should close, emitting Effect.CloseBottomSheet.
    // 2. The Qtsp selection should update, emitting Effect.OnSelectedQtspUpdated.
    @Test
    fun `Given Case 10, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            whenever(selectQtspInteractor.updateQtspUserSelection(qtspData)).thenReturn(
                EudiRqesSetSelectedQtspPartialState.Success(service = rqesService)
            )

            // Act
            viewModel.setEvent(Event.BottomSheet.QtspSelected(qtspData))

            // Assert
            viewModel.effect.runFlowTest {
                assertEquals(Effect.CloseBottomSheet, awaitItem())
                assertEquals(Effect.OnSelectedQtspUpdated(service = rqesService), awaitItem())
            }
        }

    // Case 11
    // Function setEvent() is called with an Event.FetchServiceAuthorizationUrl event and an
    // rqesService object as argument.
    // Case 11 Expected Result:
    // 1. The service authorization URL should be fetched, triggering Effect.OpenUrl with the authorization Uri.
    @Test
    fun `Given Case 11, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val response = EudiRqesGetServiceAuthorizationUrlPartialState.Success(
                authorizationUrl = mockedAuthorizationUrl.toUri()
            )
            mockGetServiceAuthorizationUrlCall(response = response)

            // Act
            viewModel.setEvent(Event.FetchServiceAuthorizationUrl(rqesService))

            // Assert
            viewModel.effect.runFlowTest {
                assertTrue(awaitItem() is Effect.OpenUrl)
            }
        }

    // Case 12
    // Function setEvent(Event.Pop) is called to trigger the event of popping the current screen or action.
    // Case 12 Expected Result:
    // 1. The sheet content should be updated to the expected "ConfirmCancellation" content.
    // 2. The bottom sheet should be shown (Effect.ShowBottomSheet).
    @Test
    fun `Given Case 12, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val sheetContent = SelectQtspBottomSheetContent.ConfirmCancellation(
                bottomSheetTextData = mockConfirmCancellationTextData()
            )

            // Act
            viewModel.setEvent(Event.Pop)

            // Assert
            assertEquals(sheetContent, viewModel.viewState.value.sheetContent)
            viewModel.effect.runFlowTest {
                val expectedEffect = Effect.ShowBottomSheet
                assertEquals(expectedEffect, awaitItem())
            }
        }
    //endregion

    //region of helper functions
    private fun mockLocalizedStrings(resourceProvider: ResourceProvider) {
        LocalizableKey.entries.forEach { key ->
            whenever(resourceProvider.getLocalizedString(key)).thenReturn(key.defaultTranslation())
        }
    }

    private fun mockScreenRouteGeneration(documentData: DocumentData): String {
        return generateComposableNavigationLink(
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
    }

    private fun mockConfirmCancellationTextData(): BottomSheetTextData {
        return BottomSheetTextData(
            title = resourceProvider.getLocalizedString(LocalizableKey.CancelSignProcessTitle),
            message = resourceProvider.getLocalizedString(LocalizableKey.CancelSignProcessSubtitle),
            positiveButtonText = resourceProvider.getLocalizedString(LocalizableKey.CancelSignProcessPrimaryText),
            negativeButtonText = resourceProvider.getLocalizedString(LocalizableKey.CancelSignProcessSecondaryText),
        )
    }

    private suspend fun mockGetServiceAuthorizationUrlCall(response: EudiRqesGetServiceAuthorizationUrlPartialState) {
        whenever(selectQtspInteractor.getServiceAuthorizationUrl(rqesService))
            .thenReturn(response)
    }
    //endregion
}