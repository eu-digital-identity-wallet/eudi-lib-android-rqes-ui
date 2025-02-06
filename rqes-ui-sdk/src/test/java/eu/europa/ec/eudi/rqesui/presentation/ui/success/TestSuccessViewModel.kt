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

package eu.europa.ec.eudi.rqesui.presentation.ui.success

import android.net.Uri
import eu.europa.ec.eudi.rqesui.domain.entities.error.EudiRQESUiError
import eu.europa.ec.eudi.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.eudi.rqesui.domain.extension.toUri
import eu.europa.ec.eudi.rqesui.domain.interactor.SuccessInteractor
import eu.europa.ec.eudi.rqesui.domain.interactor.SuccessInteractorGetSelectedFileAndQtspPartialState
import eu.europa.ec.eudi.rqesui.domain.interactor.SuccessInteractorSignAndSaveDocumentPartialState
import eu.europa.ec.eudi.rqesui.domain.serializer.UiSerializer
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.QtspData
import eu.europa.ec.eudi.rqesui.infrastructure.provider.ResourceProvider
import eu.europa.ec.eudi.rqesui.infrastructure.theme.values.ThemeColors
import eu.europa.ec.eudi.rqesui.presentation.entities.SelectionOptionUi
import eu.europa.ec.eudi.rqesui.presentation.entities.config.ViewDocumentUiConfig
import eu.europa.ec.eudi.rqesui.presentation.navigation.SdkScreens
import eu.europa.ec.eudi.rqesui.presentation.navigation.helper.generateComposableArguments
import eu.europa.ec.eudi.rqesui.presentation.navigation.helper.generateComposableNavigationLink
import eu.europa.ec.eudi.rqesui.presentation.ui.component.AppIconAndTextData
import eu.europa.ec.eudi.rqesui.presentation.ui.component.AppIcons
import eu.europa.ec.eudi.rqesui.presentation.ui.component.RelyingPartyData
import eu.europa.ec.eudi.rqesui.presentation.ui.component.content.ContentHeaderConfig
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.BottomSheetTextData
import eu.europa.ec.eudi.rqesui.util.CoroutineTestRule
import eu.europa.ec.eudi.rqesui.util.mockedDocumentName
import eu.europa.ec.eudi.rqesui.util.mockedLocalFileUri
import eu.europa.ec.eudi.rqesui.util.mockedPlainFailureMessage
import eu.europa.ec.eudi.rqesui.util.mockedQtspEndpoint
import eu.europa.ec.eudi.rqesui.util.mockedQtspName
import eu.europa.ec.eudi.rqesui.util.mockedScaUrl
import eu.europa.ec.eudi.rqesui.util.runFlowTest
import eu.europa.ec.eudi.rqesui.util.runTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
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

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TestSuccessViewModel {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @Mock
    private lateinit var successInteractor: SuccessInteractor

    @Mock
    private lateinit var resourceProvider: ResourceProvider

    @Mock
    private lateinit var uiSerializer: UiSerializer

    @Mock
    private lateinit var qtspData: QtspData

    private val documentFileUri = Uri.parse(mockedLocalFileUri)

    private lateinit var viewModel: SuccessViewModel

    private lateinit var autoCloseable: AutoCloseable

    @Before
    fun setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this)
        viewModel = SuccessViewModel(successInteractor, resourceProvider, uiSerializer)
        mockLocalizedStrings(resourceProvider)
    }

    @After
    fun after() {
        autoCloseable.close()
    }

    //region setInitialState
    // Case 1
    // Function setInitialState() is called to initialize the ViewModel state.
    // Case 1 Expected Result:
    // 1. The ViewModel's initialState should be correctly initialized with a ContentHeader showing
    // the app icon, a bottom bar and the related wordings for the bottom sheet.
    @Test
    fun `Given Case 1, When setInitialState is called, Then the expected result is returned`() {
        // Act
        val initialState = viewModel.setInitialState()

        // Assert
        val expectedSheetContentData = SuccessBottomSheetContent.ShareDocument(
            BottomSheetTextData(
                title = resourceProvider.getLocalizedString(LocalizableKey.SharingDocument),
                message = resourceProvider.getLocalizedString(LocalizableKey.CloseSharingMessage),
                positiveButtonText = resourceProvider.getLocalizedString(LocalizableKey.Share),
                negativeButtonText = resourceProvider.getLocalizedString(LocalizableKey.Close),
            )
        )
        val expectedHeaderConfig = ContentHeaderConfig(
            appIconAndTextData = AppIconAndTextData(),
            description = null,
        )

        assertEquals(
            expectedHeaderConfig,
            initialState.headerConfig
        )
        assertEquals(
            resourceProvider.getLocalizedString(LocalizableKey.Close),
            initialState.bottomBarButtonText
        )
        assertEquals(expectedSheetContentData, initialState.sheetContent)
    }
    //endregion

    //region setEvent
    // Case 1
    // Function setEvent(Event.Initialize) is called to initialize the ViewModel with selected file and QTSP data.
    // Case 1 Expected Result:
    // 1. The mocked response contains the selected file (document name and Uri) and QTSP data.
    // 2. When the Event.Initialize event is triggered, the ViewModel should emit the
    // Effect.OnSelectedFileAndQtspGot effect.
    @Test
    fun `Given Case 1, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val response = SuccessInteractorGetSelectedFileAndQtspPartialState.Success(
                selectedFile = DocumentData(mockedDocumentName, uri = documentFileUri),
                selectedQtsp = qtspData
            )
            mockQTSPData(qtspData = qtspData)
            mockGetSelectedFileAndQtspCall(response = response)

            // Act
            viewModel.setEvent(Event.Initialize)

            // Assert
            viewModel.effect.runFlowTest {
                val expectedEffect = Effect.OnSelectedFileAndQtspGot(
                    selectedFile = DocumentData(
                        documentName = mockedDocumentName,
                        uri = documentFileUri
                    ),
                    selectedQtsp = qtspData
                )
                assertEquals(expectedEffect, awaitItem())
            }
        }

    // Case 2
    // Function setEvent(Event.SignAndSaveDocument) is called to trigger the signing and saving of a document.
    // Case 2 Expected Result:
    // 1. The mocked response simulates a successfully signed and saved document with a text prefix.
    // 2. When the event is triggered, the ViewModel updates its state to reflect success.
    // This includes the updated selection item, contentHeader, bottom bar button text and
    // bottom sheet content.
    @Test
    fun `Given Case 2, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val event = Event.SignAndSaveDocument(mockedDocumentName, mockedQtspName)

            val signedDocumentPrefix = "signed_0"
            val selectionItem = SelectionOptionUi(
                leadingIcon = AppIcons.Verified,
                leadingIconTint = ThemeColors.success,
                mainText = "${signedDocumentPrefix}_$mockedDocumentName",
                actionText = resourceProvider.getLocalizedString(LocalizableKey.View),
                event = Event.ViewDocumentItemPressed(
                    documentData = DocumentData(
                        documentName = "${signedDocumentPrefix}_$mockedDocumentName",
                        uri = documentFileUri
                    )
                ),
            )

            val title = resourceProvider.getLocalizedString(LocalizableKey.SharingDocument)
            val message = resourceProvider.getLocalizedString(LocalizableKey.CloseSharingMessage)

            val headerConfig = ContentHeaderConfig(
                appIconAndTextData = AppIconAndTextData(),
                description = resourceProvider.getLocalizedString(LocalizableKey.SuccessDescription),
                relyingPartyData = RelyingPartyData(isVerified = true, name = mockedQtspName)
            )

            val expectedState = State(
                isLoading = false,
                headerConfig = headerConfig,
                selectionItem = selectionItem,
                error = null,
                isBottomSheetOpen = false,
                isBottomBarButtonEnabled = true,
                bottomBarButtonText = resourceProvider.getLocalizedString(LocalizableKey.Close),
                sheetContent = SuccessBottomSheetContent.ShareDocument(
                    bottomSheetTextData = BottomSheetTextData(
                        title = title,
                        message = message,
                        positiveButtonText = resourceProvider.getLocalizedString(LocalizableKey.Share),
                        negativeButtonText = resourceProvider.getLocalizedString(LocalizableKey.Close)
                    )
                )
            )

            val response = SuccessInteractorSignAndSaveDocumentPartialState.Success(
                savedDocument = DocumentData(
                    documentName = "${signedDocumentPrefix}_$mockedDocumentName",
                    uri = documentFileUri
                )
            )
            mockSignAndSaveDocumentCall(
                documentName = mockedDocumentName,
                response = response
            )

            // Act
            viewModel.setEvent(event)

            // Assert
            assertEquals(expectedState, viewModel.viewState.value)
        }

    // Case 3
    // Function setEvent(Event.SignAndSaveDocument) is called to trigger the signing and saving of a document.
    // Case 3 Expected Result:
    // 1. The mocked response simulates a failure during the signing and saving process, returning an error message.
    // 2. When the event is triggered, the ViewModel updates its state to reflect the error.
    @Test
    fun `Given Case 3, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val event = Event.SignAndSaveDocument(mockedDocumentName, mockedQtspName)
            val errorResponse = SuccessInteractorSignAndSaveDocumentPartialState.Failure(
                error = EudiRQESUiError(mockedPlainFailureMessage)
            )
            mockSignAndSaveDocumentCall(documentName = mockedDocumentName, response = errorResponse)

            // Act
            viewModel.setEvent(event)

            // Assert
            val currentState = viewModel.viewState.value
            assertEquals(
                mockedPlainFailureMessage,
                currentState.error?.errorSubTitle
            )
        }

    // Case 4
    // Function setEvent(Event.BottomSheet.UpdateBottomSheetState) is called to update the state of the bottom sheet.
    // Case 4 Expected Result:
    // 1. The bottom sheet's open state should be updated to true.
    // 2. The ViewModel's state is modified accordingly.
    @Test
    fun `Given Case 4, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val expectedState = viewModel.viewState.value.copy(isBottomSheetOpen = true)

            // Act
            viewModel.setEvent(
                Event.BottomSheet.UpdateBottomSheetState(isOpen = true)
            )

            // Assert
            assertEquals(
                expectedState.isBottomSheetOpen,
                viewModel.viewState.value.isBottomSheetOpen
            )
        }

    // Case 5
    // Function setEvent(Event.ViewDocumentItemPressed) is called to handle navigation to the ViewDocument screen.
    // Case 5 Expected Result:
    // 1. The correct screen route is generated based on the document data.
    // 2. The ViewModel emits an Effect.Navigation.SwitchScreen to navigate to next screen.
    @Test
    fun `Given Case 5, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val documentData = DocumentData(mockedDocumentName, documentFileUri)

            // Act
            viewModel.setEvent(
                Event.ViewDocumentItemPressed(documentData = documentData)
            )

            // Assert
            viewModel.effect.runFlowTest {
                val expectedScreenRoute = mockScreenRouteGeneration(documentData)
                val expectedEffect =
                    Effect.Navigation.SwitchScreen(screenRoute = expectedScreenRoute)
                assertEquals(expectedEffect, awaitItem())
            }
        }

    // Case 6
    // Function setEvent(Event.Pop) is called to trigger the related event.
    // Case 6 Expected Result:
    // 1. The ViewModel emits the expected navigation effect to finish the current screen.
    @Test
    fun `Given Case 6, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Act
            viewModel.setEvent(Event.Pop)

            // Assert
            viewModel.effect.runFlowTest {
                assertEquals(Effect.Navigation.Finish, awaitItem())
            }
        }

    // Case 7
    // Function setEvent(Event.BottomBarButtonPressed) is called to trigger the bottom sheet.
    // Case 7 Expected Result:
    // 1. The ViewModel emits the expected effect to display the bottom sheet.
    @Test
    fun `Given Case 7, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Act
            viewModel.setEvent(
                Event.BottomBarButtonPressed
            )

            // Assert
            viewModel.effect.runFlowTest {
                assertEquals(Effect.ShowBottomSheet, awaitItem())
            }
        }

    // Case 8
    // Function setEvent(Event.DismissError) is called to handle the event of dismissing an error.
    // Case 8 Expected Result:
    // 1. The error in the ViewState should be set to null, indicating the the error was cleared.
    @Test
    fun `Given Case 8, When setEvent is called, Then the expected result is returned`() {
        // Act
        viewModel.setEvent(Event.DismissError)

        // Assert
        assertEquals(null, viewModel.viewState.value.error)
    }

    // Case 9
    // Function setEvent(Event.BottomSheet.ShareDocument.PrimaryButtonPressed) is called to handle the event
    // of pressing the primary button to share a document.
    // Case 9 Expected Result:
    // 1. The bottom sheet should be closed (Effect.CloseBottomSheet).
    // 2. An effect of type Effect.SharePdf should be emitted, indicating that the document will be shared.
    @Test
    fun `Given Case 9, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val event =
                Event.BottomSheet.ShareDocument.PrimaryButtonPressed(documentUri = documentFileUri)

            // Act
            viewModel.setEvent(event)

            // Assert
            viewModel.effect.runFlowTest {
                assertTrue(awaitItem() is Effect.CloseBottomSheet)
                assertTrue(awaitItem() is Effect.SharePdf)
            }
        }

    // Case 10
    // Function setEvent(Event.BottomSheet.ShareDocument.SecondaryButtonPressed) is called to handle the event
    // of pressing the secondary button in the `Share Document` bottom sheet.
    // Case 10 Expected Result:
    // 1. The bottom sheet should be closed (Effect.CloseBottomSheet).
    // 2. The current screen should be finished (Effect.Navigation.Finish).
    @Test
    fun `Given Case 10, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val event = Event.BottomSheet.ShareDocument.SecondaryButtonPressed

            // Act
            viewModel.setEvent(event)

            // Assert
            viewModel.effect.runFlowTest {
                assertTrue(awaitItem() is Effect.CloseBottomSheet)
                assertTrue(awaitItem() is Effect.Navigation.Finish)
            }
        }
    //endregion

    // region of helper functions
    private fun mockLocalizedStrings(resourceProvider: ResourceProvider) {
        LocalizableKey.entries.forEach { key ->
            whenever(resourceProvider.getLocalizedString(key)).thenReturn(key.defaultTranslation())
        }
    }

    private fun mockQTSPData(qtspData: QtspData) {
        with(qtspData) {
            whenever(this.name).thenReturn(mockedQtspName)
            whenever(this.endpoint).thenReturn(mockedQtspEndpoint.toUri())
            whenever(this.scaUrl).thenReturn(mockedScaUrl)
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

    private fun mockGetSelectedFileAndQtspCall(response: SuccessInteractorGetSelectedFileAndQtspPartialState) {
        whenever(successInteractor.getSelectedFileAndQtsp())
            .thenReturn(response)
    }

    private suspend fun mockSignAndSaveDocumentCall(
        documentName: String,
        response: SuccessInteractorSignAndSaveDocumentPartialState
    ) {
        whenever(successInteractor.signAndSaveDocument(originalDocumentName = documentName))
            .thenReturn(response)
    }
    //endregion
}