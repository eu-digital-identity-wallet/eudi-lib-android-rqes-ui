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
import eu.europa.ec.eudi.rqesui.presentation.entities.SelectionItemUi
import eu.europa.ec.eudi.rqesui.presentation.entities.config.ViewDocumentUiConfig
import eu.europa.ec.eudi.rqesui.presentation.navigation.SdkScreens
import eu.europa.ec.eudi.rqesui.presentation.navigation.helper.generateComposableArguments
import eu.europa.ec.eudi.rqesui.presentation.navigation.helper.generateComposableNavigationLink
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
        assertEquals(
            resourceProvider.getLocalizedString(LocalizableKey.SignDocument),
            initialState.title
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
    @Test
    fun `Given Case 1, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val response = SuccessInteractorGetSelectedFileAndQtspPartialState.Success(
                selectedFile = DocumentData(mockedDocumentName, uri = documentFileUri),
                selectedQtsp = qtspData
            )
            mockQTSPData(qtspData)
            mockGetSelectedFileAndQtspCall(response = response)

            // Act
            viewModel.setEvent(Event.Init)

            // Assert
            viewModel.effect.runFlowTest {
                val expectedEffect = Effect.OnSelectedFileAndQtspGot(
                    selectedFile = DocumentData(mockedDocumentName, uri = documentFileUri),
                    selectedQtsp = qtspData
                )
                assertEquals(expectedEffect, awaitItem())
            }
        }

    // Case 2
    @Test
    fun `Given Case 2, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val event = Event.SignAndSaveDocument(mockedDocumentName, mockedQtspName)

            val title = resourceProvider.getLocalizedString(LocalizableKey.SharingDocument)
            val message = resourceProvider.getLocalizedString(LocalizableKey.CloseSharingMessage)
            val signDocument = resourceProvider.getLocalizedString(LocalizableKey.SignDocument)

            val signedDocumentPrefix = "signed_0"
            val selectionITem = SelectionItemUi(
                action = resourceProvider.getLocalizedString(LocalizableKey.View),
                documentData = DocumentData(
                    documentName = "${signedDocumentPrefix}_$mockedDocumentName",
                    uri = documentFileUri
                )
            )

            val expectedState = State(
                isLoading = false,
                headline = resourceProvider.getLocalizedString(LocalizableKey.Success),
                selectionItem = selectionITem,
                error = null,
                isBottomSheetOpen = false,
                isBottomBarButtonEnabled = true,
                title = signDocument,
                subtitle = resourceProvider.getLocalizedString(LocalizableKey.SuccessfullySignedDocument),
                bottomBarButtonText = resourceProvider.getLocalizedString(LocalizableKey.Close),
                sheetContent = SuccessBottomSheetContent.ShareDocument(
                    bottomSheetTextData = BottomSheetTextData(
                        title = title,
                        message = message,
                        resourceProvider.getLocalizedString(LocalizableKey.Share),
                        resourceProvider.getLocalizedString(LocalizableKey.Close)
                    )
                )
            )

            whenever(successInteractor.signAndSaveDocument(originalDocumentName = mockedDocumentName))
                .thenReturn(
                    SuccessInteractorSignAndSaveDocumentPartialState.Success(
                        savedDocument = DocumentData(
                            documentName = "${signedDocumentPrefix}_$mockedDocumentName",
                            uri = documentFileUri
                        )
                    )
                )

            // Act
            viewModel.setEvent(event)

            // Assert
            assertEquals(expectedState, viewModel.viewState.value)
        }

    // Case 3
    @Test
    fun `Given Case 3, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val event = Event.SignAndSaveDocument(mockedDocumentName, mockedQtspName)
            val errorResponse = SuccessInteractorSignAndSaveDocumentPartialState.Failure(
                error = EudiRQESUiError(mockedPlainFailureMessage)
            )
            whenever(successInteractor.signAndSaveDocument(mockedDocumentName))
                .thenReturn(errorResponse)

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
    @Test
    fun `Given Case 4, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val expectedState = viewModel.viewState.value.copy(isBottomSheetOpen = true)

            // Act
            viewModel.setEvent(Event.BottomSheet.UpdateBottomSheetState(true))

            // Assert
            assertEquals(
                expectedState.isBottomSheetOpen,
                viewModel.viewState.value.isBottomSheetOpen
            )
        }

    // Case 5
    @Test
    fun `Given Case 5, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val documentData = DocumentData(mockedDocumentName, documentFileUri)

            // Act
            viewModel.setEvent(
                Event.ViewDocument(documentData)
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
    @Test
    fun `Given Case 8, When setEvent is called, Then the expected result is returned`() {
        // Act
        viewModel.setEvent(Event.DismissError)

        // Assert
        assertEquals(null, viewModel.viewState.value.error)
    }

    // Case 9
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
    //endregion
}
