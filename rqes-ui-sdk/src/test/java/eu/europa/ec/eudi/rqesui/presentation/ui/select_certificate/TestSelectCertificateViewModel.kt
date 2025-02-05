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

/**
package eu.europa.ec.eudi.rqesui.presentation.ui.select_certificate


import android.net.Uri
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetCredentialAuthorizationUrlPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetSelectedFilePartialState
import eu.europa.ec.eudi.rqesui.domain.entities.error.EudiRQESUiError
import eu.europa.ec.eudi.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.CertificateData
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.infrastructure.provider.ResourceProvider
import eu.europa.ec.eudi.rqesui.util.CoroutineTestRule
import eu.europa.ec.eudi.rqesui.util.mockedFetchCertificatesFailureMessage
import eu.europa.ec.eudi.rqesui.util.runFlowTest
import eu.europa.ec.eudi.rqesui.util.runTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
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
class TestSelectCertificateViewModel {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @Mock
    private lateinit var selectCertificateInteractor: SelectCertificateInteractor

    @Mock
    private lateinit var resourceProvider: ResourceProvider

    @Mock
    private lateinit var authorizationUri: Uri

    @Mock
    private lateinit var certificateData: CertificateData

    @Mock
    private lateinit var documentData: DocumentData

    private lateinit var viewModel: SelectCertificateViewModel

    private lateinit var autoCloseable: AutoCloseable

    @Before
    fun setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this)
        viewModel = SelectCertificateViewModel(selectCertificateInteractor, resourceProvider)

        mockLocalizedStrings(resourceProvider)
    }

    @After
    fun after() {
        autoCloseable.close()
    }

    //region setInitialState
    // Case 1
    // Function setInitialState() is called to initialize the ViewModel's state.
    // Case 1 Expected Result:
    // 1. The title is set to the localized string for SignDocument.
    // 2. The subtitle is set to the localized string for SelectCertificateTitle.
    // 3. The certificates section title is set to the localized string for SelectCertificateSubtitle.
    // 4. The bottom bar button text is set to the localized string for Sign.
    @Test
    fun `Given Case 1, When setInitialState is called, Then the expected result is returned`() {
        // Act
        viewModel.setInitialState()

        // Assert
        assertEquals(
            resourceProvider.getLocalizedString(LocalizableKey.SignDocument),
            viewModel.viewState.value.title
        )
        assertEquals(
            resourceProvider.getLocalizedString(LocalizableKey.SelectCertificateTitle),
            viewModel.viewState.value.subtitle
        )
        assertEquals(
            resourceProvider.getLocalizedString(LocalizableKey.SelectCertificateSubtitle),
            viewModel.viewState.value.certificatesSectionTitle
        )
        assertEquals(
            resourceProvider.getLocalizedString(LocalizableKey.Sign),
            viewModel.viewState.value.bottomBarButtonText
        )
    }
    //endregion

    //region setEvent
    // Case 1
    // Event CertificateSelected is triggered by calling setEvent() with a selected certificate index.
    // Case 1 Expected Result:
    // 1. The ViewModel's state is updated with the selectedCertificateIndex set to the value passed in the event.
    @Test
    fun `Given Case 1, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val selectedIndex = 0

            // Act
            viewModel.setEvent(
                Event.CertificateSelected(index = selectedIndex)
            )

            // Assert
            assertEquals(selectedIndex, viewModel.viewState.value.selectedCertificateIndex)
        }

    // Case 2
    // Events AuthorizeServiceAndFetchCertificates and BottomBarButtonPressed are triggered, simulating a
    // scenario where certificates are successfully fetched and an authorization URL is returned.
    // Case 2 Expected Result:
    // 1. When BottomBarButtonPressed is triggered after successful certificate fetching, an OpenUrl effect is emitted.
    // 2. The emitted URL matches the mocked authorization URI.
    @Test
    fun `Given Case 2, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val mockedCertificates = listOf(certificateData)
            val fetchCertificatesResponse =
                SelectCertificateInteractorAuthorizeServiceAndFetchCertificatesPartialState.Success(
                    certificates = mockedCertificates
                )
            val authResponse = EudiRqesGetCredentialAuthorizationUrlPartialState.Success(
                authorizationUrl = authorizationUri
            )
            mockAuthorizeServiceAndFetchCertificatesCall(response = fetchCertificatesResponse)
            mockGetCredentialAuthorizationUrlCall(response = authResponse)

            // Act
            viewModel.setEvent(Event.AuthorizeServiceAndFetchCertificates)
            viewModel.setEvent(Event.BottomBarButtonPressed)

            // Assert
            viewModel.effect.runFlowTest {
                val effect = awaitItem()
                assertTrue(effect is Effect.OpenUrl)
                assertEquals(authorizationUri, (effect as Effect.OpenUrl).uri)
            }
        }

    // Case 3
    // Event AuthorizeServiceAndFetchCertificates is triggered, simulating a failure scenario where
    // the interactor returns a Failure state with an error message.
    // Case 3 Expected Result:
    // 1. The ViewModel's state contains a non-null error object.
    // 2. The error's subtitle matches the mocked failure message.
    @Test
    fun `Given Case 3, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val response =
                SelectCertificateInteractorAuthorizeServiceAndFetchCertificatesPartialState.Failure(
                    EudiRQESUiError(message = mockedFetchCertificatesFailureMessage)
                )
            mockAuthorizeServiceAndFetchCertificatesCall(response = response)

            // Act
            viewModel.setEvent(Event.AuthorizeServiceAndFetchCertificates)

            // Assert
            val viewState = viewModel.viewState.value
            assertTrue(viewState.error != null)
            assertEquals(mockedFetchCertificatesFailureMessage, viewState.error?.errorSubTitle)
        }

    // Case 4
    // Function setEvent() is called with an Event.BottomSheet.CancelSignProcess.PrimaryButtonPressed event.
    // Case 4 Expected Result:
    // 1. The effect should trigger a CloseBottomSheet action, indicating that the bottom sheet will be closed.
    @Test
    fun `Given Case 4, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Act
            viewModel.setEvent(
                Event.BottomSheet.CancelSignProcess.PrimaryButtonPressed
            )

            // Assert
            viewModel.effect.runFlowTest {
                val effect = awaitItem()
                assertTrue(effect is Effect.CloseBottomSheet)
            }
        }

    // Case 5
    // Function setEvent() is called with an Event.BottomSheet.CancelSignProcess.SecondaryButtonPressed event.
    // Case 5 Expected Result:
    // 1. The effect should trigger a Finish navigation action, indicating that the current screen will be finished.
    @Test
    fun `Given Case 5, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Act
            viewModel.setEvent(Event.BottomSheet.CancelSignProcess.SecondaryButtonPressed)

            // Assert
            viewModel.effect.runFlowTest {
                val effect = awaitItem()
                assertTrue(effect is Effect.Navigation.Finish)
            }
        }

    // Case 6
    // Function setEvent() is called with an Event.Init.
    // Case 6 Expected Result:
    // 1. The view state should reflect a non-null selectionItem with the correct documentData.
    // 2. The effect should trigger Effect.OnSelectionItemCreated, indicating that the selection item
    // was successfully created and set.
    @Test
    fun `Given Case 6, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val mockSuccessState = EudiRqesGetSelectedFilePartialState.Success(file = documentData)
            whenever(selectCertificateInteractor.getSelectedFile()).thenReturn(mockSuccessState)

            // Act
            viewModel.setEvent(Event.Init)

            // Assert
            viewModel.viewStateHistory.runFlowTest {
                val state = awaitItem()
                assertNotNull(state.selectionItem)
                assertEquals(documentData, state.selectionItem?.documentData)
            }

            viewModel.effect.runFlowTest {
                assertEquals(Effect.OnSelectionItemCreated, awaitItem())
            }
        }

    // Case 7
    // Function setEvent() is called with an Event.Pop argument.
    // Case 7 Expected Result:
    // 1. The effect should trigger Effect.ShowBottomSheet, indicating that a bottom sheet is
    // displayed to the user.
    @Test
    fun `Given Case 7, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Act
            viewModel.setEvent(Event.Pop)

            // Assert
            viewModel.effect.runFlowTest {
                assertEquals(Effect.ShowBottomSheet, awaitItem())
            }
        }

    // Case 8
    // Function setEvent() is called with an Event.BottomSheet.UpdateBottomSheetState event,
    // and argument isOpen with value of true.
    // Case 8 Expected Result:
    // 1. The view state should reflect that the bottom sheet is open,
    // correspondingly isBottomSheetOpen state value should be true.
    @Test
    fun `Given Case 8, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Act
            viewModel.setEvent(
                Event.BottomSheet.UpdateBottomSheetState(isOpen = true)
            )

            // Assert
            assertTrue(viewModel.viewState.value.isBottomSheetOpen)
        }

    // Case 9
    // Function setEvent() is called with an Event.DismissError event.
    // Case 9 Expected Result:
    // 1. The view state should have its error field set to null, indicating that the error has been cleared.
    @Test
    fun `Given Case 9, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Act
            viewModel.setEvent(Event.DismissError)

            // Assert
            assertTrue(viewModel.viewState.value.error == null)
        }
    //endregion

    //region of helper functions
    private fun mockLocalizedStrings(resourceProvider: ResourceProvider) {
        LocalizableKey.entries.forEach { key ->
            whenever(resourceProvider.getLocalizedString(key)).thenReturn(key.defaultTranslation())
        }
    }

    private suspend fun mockAuthorizeServiceAndFetchCertificatesCall(
        response: SelectCertificateInteractorAuthorizeServiceAndFetchCertificatesPartialState
    ) {
        whenever(selectCertificateInteractor.authorizeServiceAndFetchCertificates())
            .thenReturn(response)
    }

    private suspend fun mockGetCredentialAuthorizationUrlCall(response: EudiRqesGetCredentialAuthorizationUrlPartialState) {
        whenever(selectCertificateInteractor.getCredentialAuthorizationUrl(certificateData))
            .thenReturn(response)
    }
    //endregion
}
 */