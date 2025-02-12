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

package eu.europa.ec.eudi.rqesui.presentation.ui.options_selection

import android.net.Uri
import eu.europa.ec.eudi.rqes.CredentialInfo
import eu.europa.ec.eudi.rqes.core.RQESService
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetCredentialAuthorizationUrlPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetQtspsPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetSelectedFilePartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetServiceAuthorizationUrlPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesSetSelectedQtspPartialState
import eu.europa.ec.eudi.rqesui.domain.entities.error.EudiRQESUiError
import eu.europa.ec.eudi.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.eudi.rqesui.domain.extension.toUri
import eu.europa.ec.eudi.rqesui.domain.interactor.OptionsSelectionInteractor
import eu.europa.ec.eudi.rqesui.domain.interactor.OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState
import eu.europa.ec.eudi.rqesui.domain.interactor.OptionsSelectionInteractorGetSelectedQtspPartialState
import eu.europa.ec.eudi.rqesui.domain.serializer.UiSerializer
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.CertificateData
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.QtspData
import eu.europa.ec.eudi.rqesui.infrastructure.provider.ResourceProvider
import eu.europa.ec.eudi.rqesui.infrastructure.theme.values.ThemeColors
import eu.europa.ec.eudi.rqesui.presentation.entities.ModalOptionUi
import eu.europa.ec.eudi.rqesui.presentation.entities.SelectionOptionUi
import eu.europa.ec.eudi.rqesui.presentation.entities.config.OptionsSelectionUiConfig
import eu.europa.ec.eudi.rqesui.presentation.entities.config.ViewDocumentUiConfig
import eu.europa.ec.eudi.rqesui.presentation.navigation.SdkScreens
import eu.europa.ec.eudi.rqesui.presentation.navigation.helper.generateComposableArguments
import eu.europa.ec.eudi.rqesui.presentation.navigation.helper.generateComposableNavigationLink
import eu.europa.ec.eudi.rqesui.presentation.ui.component.AppIcons
import eu.europa.ec.eudi.rqesui.presentation.ui.component.wrap.BottomSheetTextData
import eu.europa.ec.eudi.rqesui.util.CoroutineTestRule
import eu.europa.ec.eudi.rqesui.util.mockedAuthorizationUrl
import eu.europa.ec.eudi.rqesui.util.mockedCertificateName
import eu.europa.ec.eudi.rqesui.util.mockedDocumentName
import eu.europa.ec.eudi.rqesui.util.mockedFetchCertificatesFailureMessage
import eu.europa.ec.eudi.rqesui.util.mockedLocalFileUri
import eu.europa.ec.eudi.rqesui.util.mockedPlainFailureMessage
import eu.europa.ec.eudi.rqesui.util.mockedQtspName
import eu.europa.ec.eudi.rqesui.util.mockedSerializedConfig
import eu.europa.ec.eudi.rqesui.util.mockedUri
import eu.europa.ec.eudi.rqesui.util.runFlowTest
import eu.europa.ec.eudi.rqesui.util.runTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TestOptionsSelectionViewModel {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @Mock
    private lateinit var optionsSelectionInteractor: OptionsSelectionInteractor

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

    @Mock
    private lateinit var authorizationUri: Uri

    @Mock
    private lateinit var certificateData: CertificateData

    @Mock
    private lateinit var credentialInfo: CredentialInfo

    private val documentFileUri = Uri.parse(mockedLocalFileUri)

    private lateinit var viewModel: OptionsSelectionViewModel

    private lateinit var autoCloseable: AutoCloseable

    private val deserializedConfig = OptionsSelectionUiConfig(
        optionsSelectionScreenState = QTSP_SELECTION_STATE
    )

    @Before
    fun setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this)

        whenever(
            uiSerializer.fromBase64(
                payload = mockedSerializedConfig,
                model = OptionsSelectionUiConfig::class.java,
                parser = OptionsSelectionUiConfig.Parser
            )
        ).thenReturn(deserializedConfig)

        viewModel = OptionsSelectionViewModel(
            optionsSelectionInteractor,
            resourceProvider,
            uiSerializer,
            mockedSerializedConfig
        )
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
    // 2. The bottomBarButtonAction is null indicating that it is not shown initially.
    // 3. The bottom sheet content is set with a "ConfirmCancellation" structure containing localized
    // strings for CancelSignProcessTitle, CancelSignProcessSubtitle, CancelSignProcessPrimaryText,
    // CancelSignProcessSecondaryText.
    @Test
    fun `Given Case 1, When setInitialState is called, Then the expected result is returned`() {
        // Act
        viewModel.setInitialState()

        // Assert
        val expectedState = State(
            isLoading = false,
            error = null,
            isBottomSheetOpen = false,
            title = resourceProvider.getLocalizedString(LocalizableKey.SignDocument),
            documentSelectionItem = null,
            bottomBarButtonAction = null,
            sheetContent = OptionsSelectionBottomSheetContent.ConfirmCancellation(
                bottomSheetTextData = BottomSheetTextData(
                    title = resourceProvider.getLocalizedString(LocalizableKey.CancelSignProcessTitle),
                    message = resourceProvider.getLocalizedString(LocalizableKey.CancelSignProcessSubtitle),
                    positiveButtonText = resourceProvider.getLocalizedString(LocalizableKey.CancelSignProcessPrimaryText),
                    negativeButtonText = resourceProvider.getLocalizedString(LocalizableKey.CancelSignProcessSecondaryText)
                )
            ),
            selectedQtspIndex = 0,
            selectedCertificateIndex = 0,
            config = deserializedConfig
        )
        assertEquals(expectedState, viewModel.viewState.value)
    }
    //endregion

    //region setEvent, Initialize with Screen State
    // Case 1
    // setEvent with argument: Event.Initialize for state QTSP_SELECTION_STATE
    // the ViewModel updates the `documentSelectionItem` in the state correctly.
    // Case 1 Expected Result:
    // 1. The `selectQtspInteractor.getSelectedFile()` function returns a successful state with `documentData`.
    // 2. The `viewModel.setEvent(Event.Initialize)` function is called, triggering the creation of a `SelectionOptionUi`.
    // 3. The `qtspServiceSelectionItem` in the ViewModel's state is populated with info to select a Qtsp
    // 4. The `certificateSelectionItem` is null initially.
    // containing the expected `documentData` and `View` action string.
    @Test
    fun `Given Case 1, When setEvent is called, Then the expected result is returned`() {
        // Arrange
        whenever(optionsSelectionInteractor.getSelectedFile())
            .thenReturn(EudiRqesGetSelectedFilePartialState.Success(file = documentData))

        // Act
        viewModel.setEvent(
            Event.Initialize(
                screenSelectionState = QTSP_SELECTION_STATE
            )
        )

        // Assert
        assertEquals(
            SelectionOptionUi(
                overlineText = resourceProvider.getLocalizedString(LocalizableKey.SelectDocumentTitle),
                mainText = documentData.documentName,
                subtitle = resourceProvider.getLocalizedString(LocalizableKey.SelectDocumentSubtitle),
                actionText = resourceProvider.getLocalizedString(LocalizableKey.View),
                leadingIcon = AppIcons.StepOne,
                leadingIconTint = ThemeColors.success,
                trailingIcon = AppIcons.KeyboardArrowRight,
                enabled = true,
                event = Event.ViewDocumentItemPressed(
                    documentData = documentData
                )
            ),
            viewModel.viewState.value.documentSelectionItem
        )
        assertEquals(
            SelectionOptionUi(
                overlineText = null,
                mainText = resourceProvider.getLocalizedString(LocalizableKey.SelectSigningService),
                subtitle = resourceProvider.getLocalizedString(LocalizableKey.SelectSigningServiceSubtitle),
                actionText = null,
                leadingIcon = AppIcons.StepTwo,
                trailingIcon = AppIcons.KeyboardArrowRight,
                enabled = true,
                event = Event.RqesServiceSelectionItemPressed
            ),
            viewModel.viewState.value.qtspServiceSelectionItem
        )
        assertEquals(
            null,
            viewModel.viewState.value.certificateSelectionItem
        )
    }

    // Case 2
    // setEvent with argument: Event.Initialize for state CERTIFICATE_SELECTION_STATE
    // The function `createSelectionItem` is tested to ensure that when it sets the success state,
    // the ViewModel updates the `documentSelectionItem` in the state correctly.
    // Case 2 Expected Result:
    // 1. The interactor getSelectedFile() function returns a successful state with `documentData`.
    // 2. The `viewModel.setEvent(Event.Initialize)` function is called, triggering the creation of a `SelectionOptionUi`.
    // 3. The `certificateSelectionItem` in the ViewModel's state is updated.
    @Test
    fun `Given Case 2, When setEvent is called, Then the expected result is returned`() {
        // Arrange
        whenever(optionsSelectionInteractor.getSelectedFile())
            .thenReturn(EudiRqesGetSelectedFilePartialState.Success(file = documentData))
        whenever(optionsSelectionInteractor.getSelectedQtsp())
            .thenReturn(
                OptionsSelectionInteractorGetSelectedQtspPartialState.Success(
                    selectedQtsp = qtspData
                )
            )

        // Act
        viewModel.setEvent(
            Event.Initialize(
                screenSelectionState = CERTIFICATE_SELECTION_STATE
            )
        )

        // Assert
        assertEquals(
            SelectionOptionUi(
                overlineText = resourceProvider.getLocalizedString(LocalizableKey.SelectDocumentTitle),
                mainText = documentData.documentName,
                subtitle = resourceProvider.getLocalizedString(LocalizableKey.SelectDocumentSubtitle),
                actionText = resourceProvider.getLocalizedString(LocalizableKey.View),
                leadingIcon = AppIcons.StepOne,
                leadingIconTint = ThemeColors.success,
                trailingIcon = AppIcons.KeyboardArrowRight,
                enabled = true,
                event = Event.ViewDocumentItemPressed(
                    documentData = documentData
                )
            ),
            viewModel.viewState.value.documentSelectionItem
        )
        assertEquals(
            SelectionOptionUi(
                overlineText = resourceProvider.getLocalizedString(
                    LocalizableKey.SigningService
                ),
                mainText = qtspData.name,
                subtitle = resourceProvider.getLocalizedString(LocalizableKey.SelectSigningServiceSubtitle),
                actionText = null,
                leadingIcon = AppIcons.StepTwo,
                leadingIconTint = ThemeColors.success,
                trailingIcon = AppIcons.KeyboardArrowRight,
                enabled = false,
                event = Event.RqesServiceSelectionItemPressed
            ),
            viewModel.viewState.value.qtspServiceSelectionItem
        )
        assertEquals(
            SelectionOptionUi(
                overlineText = null,
                mainText = resourceProvider.getLocalizedString(LocalizableKey.SelectSigningCertificateTitle),
                subtitle = resourceProvider.getLocalizedString(LocalizableKey.SelectCertificateSubtitle),
                leadingIcon = AppIcons.StepThree,
                trailingIcon = AppIcons.KeyboardArrowRight,
                enabled = true,
                event = Event.CertificateSelectionItemPressed
            ),
            viewModel.viewState.value.certificateSelectionItem
        )
    }

    // Case 3
    // Function setEvent() is called with an Event.Initialize for QTSP_SELECTION_STATE.
    // Case 3 Expected Result:
    // 1. The view state should reflect a non-null selectionItem with the correct documentData.
    // 2. The effect should trigger Effect.OnSelectionItemCreated, indicating that the selection item
    // was successfully created and set.
    @Test
    fun `Given Case 3 on QTSP_SELECTION_STATE, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val mockSuccessState = EudiRqesGetSelectedFilePartialState.Success(file = documentData)
            whenever(optionsSelectionInteractor.getSelectedFile()).thenReturn(mockSuccessState)

            // Act
            viewModel.setEvent(
                Event.Initialize(screenSelectionState = QTSP_SELECTION_STATE)
            )

            // Assert
            viewModel.viewStateHistory.runFlowTest {
                val state = awaitItem()
                assertEquals(documentData, state.documentSelectionItem?.event?.documentData)
            }
        }

    // Case 4
    // Function setEvent() is called with Event.Initialize(QTSP_SELECTION_STATE) and
    // EudiRqesGetSelectedFilePartialState.Failure is returned when attempting to get selected file
    // Case 4 Expected Result:
    // 1. Error state is set with:
    //    - Correct error message
    //    - Working cancel action that:
    //      * Dismisses error
    //      * Triggers Navigation.Finish effect
    //    - Working retry action that:
    //      * Attempts to get selected file again
    // 3. Error handling flow completes properly
    @Test
    fun `Given Case 4 on QTSP_SELECTION_STATE, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val errorMessage = mockedPlainFailureMessage
            whenever(optionsSelectionInteractor.getSelectedFile())
                .thenReturn(
                    EudiRqesGetSelectedFilePartialState.Failure(
                        error = EudiRQESUiError(message = errorMessage)
                    )
                )

            // Act
            viewModel.setEvent(
                Event.Initialize(screenSelectionState = QTSP_SELECTION_STATE)
            )

            // Assert
            with(viewModel.viewState.value) {
                // Verify error state
                assertNotNull(error)
                with(error) {
                    // Check error message
                    assertEquals(errorMessage, errorSubTitle)

                    // Test cancel action
                    onCancel.invoke()

                    // Verify error is dismissed and navigation effect is triggered
                    viewModel.effect.runFlowTest {
                        val effect = awaitItem()
                        assertTrue(effect is Effect.Navigation.Finish)
                    }

                    // Test retry action
                    onRetry?.invoke()
                    verify(optionsSelectionInteractor, times(4)).getSelectedFile()
                }
            }
        }

    // Case 5
    // Function setEvent() is called with Event.Initialize(CERTIFICATE_SELECTION_STATE) and
    // OptionsSelectionInteractorGetSelectedQtspPartialState.Failure is returned when attempting to get selected QTSP
    // (while file selection succeeds)
    // Case 5 Expected Result:
    // 1. Error state is set with:
    //    - Correct error message from failure
    //    - Working cancel action
    //    - Working retry action that attempts to get selected QTSP again
    // 2. File selection success state is maintained
    @Test
    fun `Given Case 5, When setEvent is called on CERTIFICATE_SELECTION_STATE, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            whenever(optionsSelectionInteractor.getSelectedFile())
                .thenReturn(EudiRqesGetSelectedFilePartialState.Success(file = documentData))

            val errorMessage = mockedPlainFailureMessage
            whenever(optionsSelectionInteractor.getSelectedQtsp())
                .thenReturn(
                    OptionsSelectionInteractorGetSelectedQtspPartialState.Failure(
                        error = EudiRQESUiError(message = errorMessage)
                    )
                )

            // Act
            viewModel.setEvent(
                Event.Initialize(screenSelectionState = CERTIFICATE_SELECTION_STATE)
            )

            // Assert
            with(viewModel.viewState.value) {
                // Assert error state
                assertNotNull(error)
                with(error) {
                    assertEquals(errorMessage, errorSubTitle)
                    // Test cancel action
                    onCancel()
                    // Test retry action
                    onRetry?.invoke()
                }
            }
        }
    //endregion

    //region setEvent for Document file, QTSP Service, Certificate selection items
    // Case 1
    // Function setEvent() is called with an Event.ViewDocumentItemPressed event, with
    // documentData object as argument.
    // Case 1 Expected Result:
    // 1. The navigation effect should trigger a screen switch and the expected screen route
    // based on the document data should be provided.
    @Test
    fun `Given Case 1 for selection item, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val documentData =
                DocumentData(documentName = mockedDocumentName, uri = documentFileUri)

            // Act
            viewModel.setEvent(
                Event.ViewDocumentItemPressed(documentData)
            )

            // Assert
            viewModel.effect.runFlowTest {
                val expectedScreenRoute = mockScreenRouteGeneration(documentData)
                val expectedEffect =
                    Effect.Navigation.SwitchScreen(screenRoute = expectedScreenRoute)
                assertEquals(expectedEffect, awaitItem())
            }
        }

    // Case 2
    // Function setEvent() is called with an RqesServiceSelectionItemPressed event.
    // Scenario: User is on the Certificate selection step and triggers QTSP selection
    // User is on Certificate selection step, QTSP selection is triggered
    // Then the bottom sheet with QTSP options should be displayed
    // Case 2 Expected Result:
    // Bottom sheet content is properly populated with QTSP options
    // Bottom sheet text data is correctly set
    // Bottom sheet is shown through effect
    @Test
    fun `Given Case 2 for selection item, When setEvent is called, Then bottom sheet with QTSP options is shown`() =
        coroutineRule.runTest {
            // Arrange
            val mockQtsps = listOf(qtspData)
            val selectedIndex = 0
            whenever(qtspData.name).thenReturn(mockedQtspName)
            whenever(optionsSelectionInteractor.getQtsps())
                .thenReturn(EudiRqesGetQtspsPartialState.Success(qtsps = mockQtsps))

            // Act
            viewModel.setEvent(
                Event.RqesServiceSelectionItemPressed
            )

            // Assert
            val expectedBottomSheetContent = OptionsSelectionBottomSheetContent.SelectQTSP(
                bottomSheetTextData = BottomSheetTextData(
                    title = resourceProvider.getLocalizedString(LocalizableKey.SelectServiceTitle),
                    message = resourceProvider.getLocalizedString(LocalizableKey.SelectServiceSubtitle),
                    positiveButtonText = resourceProvider.getLocalizedString(LocalizableKey.Done),
                    negativeButtonText = resourceProvider.getLocalizedString(LocalizableKey.Cancel),
                ),
                options = listOf(
                    ModalOptionUi(
                        title = mockedQtspName,
                        trailingIcon = null,
                        event = Event.BottomSheet.QtspSelectedOnDoneButtonPressed(
                            mockQtsps[selectedIndex]
                        ),
                        radioButtonSelected = true
                    ),
                ),
                selectedIndex = selectedIndex
            )

            assertEquals(expectedBottomSheetContent, viewModel.viewState.value.sheetContent)
            viewModel.effect.runFlowTest {
                assertEquals(Effect.ShowBottomSheet, awaitItem())
            }
        }

    // Case 3
    // Function setEvent() is called with an Event.RqesServiceSelectionItemPressed event and
    // EudiRqesGetQtspsPartialState.Failure is returned
    // Case 3 Expected Result:
    // 1. Corresponding error should be set into view state.
    @Test
    fun `Given Case 3 for selection item, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val errorMessage = mockedPlainFailureMessage
            val failureState = EudiRqesGetQtspsPartialState.Failure(
                error = EudiRQESUiError(message = errorMessage)
            )
            whenever(optionsSelectionInteractor.getQtsps())
                .thenReturn(failureState)

            // Act
            viewModel.setEvent(
                Event.RqesServiceSelectionItemPressed
            )

            // Assert
            assertNotNull(viewModel.viewState.value.error)
        }

    // Case 4
    // Function setEvent() is called with an Event.CertificateSelectionItemPressed event and
    // multiple certificates are available in the state
    // Case 4 Expected Result:
    // 1. Bottom sheet content is populated with list of available certificates
    // 2. Each certificate option has correct title and selection state
    // 3. Bottom sheet text data contains correct localized strings
    // 4. First certificate is selected by default (selectedIndex = 0)
    // 5. ShowBottomSheet effect is emitted
    @Test
    fun `Given Case 4 for selection item, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            whenever(certificateData.name).thenReturn(mockedCertificateName)
            val mockedOtherCertificate = "Certificate 2"
            val mockCertificates = listOf(
                certificateData,
                CertificateData(name = mockedOtherCertificate, certificate = credentialInfo)
            )

            whenever(optionsSelectionInteractor.authorizeServiceAndFetchCertificates())
                .thenReturn(
                    OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState.Success(
                        certificates = mockCertificates
                    )
                )

            // First fetch the certificates
            viewModel.setEvent(Event.AuthorizeServiceAndFetchCertificates)

            // Act
            viewModel.setEvent(Event.CertificateSelectionItemPressed)

            // Assert
            val expectedBottomSheetContent = OptionsSelectionBottomSheetContent.SelectCertificate(
                bottomSheetTextData = BottomSheetTextData(
                    title = resourceProvider.getLocalizedString(LocalizableKey.SigningCertificates),
                    message = resourceProvider.getLocalizedString(LocalizableKey.SelectSigningCertificateSubtitle),
                    positiveButtonText = resourceProvider.getLocalizedString(LocalizableKey.Done),
                    negativeButtonText = resourceProvider.getLocalizedString(LocalizableKey.Cancel),
                ),
                options = listOf(
                    ModalOptionUi(
                        title = mockedCertificateName,
                        trailingIcon = null,
                        event = Event.BottomSheet.CertificateSelectedOnDoneButtonPressed(
                            mockCertificates[0]
                        ),
                        radioButtonSelected = true
                    ),
                    ModalOptionUi(
                        title = mockedOtherCertificate,
                        trailingIcon = null,
                        event = Event.BottomSheet.CertificateSelectedOnDoneButtonPressed(
                            mockCertificates[1]
                        ),
                        radioButtonSelected = false
                    )
                ),
                selectedIndex = 0
            )

            assertEquals(
                expectedBottomSheetContent,
                viewModel.viewState.value.sheetContent
            )

            // Verify bottom sheet is shown
            viewModel.effect.runFlowTest {
                assertEquals(Effect.ShowBottomSheet, awaitItem())
            }
        }
    //endregion

    //region setEvent, Bottom Sheet category
    // Case 1
    // Function setEvent() is called with an Event.BottomSheet.UpdateBottomSheetState event,
    // setting isOpen to true.
    // Case 1 Expected Result:
    // 1. The bottom sheet should be opened and the view state's `isBottomSheetOpen` field should be true.
    @Test
    fun `Given Case 1, When setEvent for Bottom Sheet is called, Then the expected result is returned`() {
        // Act
        viewModel.setEvent(Event.BottomSheet.UpdateBottomSheetState(isOpen = true))

        // Assert
        assertTrue(viewModel.viewState.value.isBottomSheetOpen)
    }

    // Case 2
    // Function setEvent() is called with an Event.BottomSheet.CancelSignProcess.PrimaryButtonPressed event.
    // Case 2 Expected Result:
    // 1. The bottom sheet should be closed and the view state's `isBottomSheetOpen` field should be false.
    @Test
    fun `Given Case 2, When setEvent for Bottom Sheet is called, Then the expected result is returned`() {
        // Act
        viewModel.setEvent(
            Event.BottomSheet.CancelSignProcess.PrimaryButtonPressed
        )

        // Assert
        assertFalse(viewModel.viewState.value.isBottomSheetOpen)
    }

    // Case 3
    // Function setEvent() is called with an Event.BottomSheet.CancelSignProcess.SecondaryButtonPressed event.
    // Case 3 Expected Result:
    // 1. The navigation effect should trigger a finish action.
    @Test
    fun `Given Case 3, When setEvent for Bottom Sheet is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Act
            viewModel.setEvent(
                Event.BottomSheet.CancelSignProcess.SecondaryButtonPressed
            )

            // Assert
            viewModel.effect.runFlowTest {
                assertEquals(Effect.Navigation.Finish, awaitItem())
            }
        }

    // Case 4
    // Function setEvent() is called with an Event.BottomSheet.QtspSelectedOnDoneButtonPressed event,
    // with qtstData as argument.
    // Case 4 Expected Result:
    // 1. The BottomSheet should close, emitting Effect.CloseBottomSheet.
    // 2. The Qtsp selection should update, emitting Effect.OnSelectedQtspUpdated.
    @Test
    fun `Given Case 4, When setEvent for Bottom Sheet is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            whenever(optionsSelectionInteractor.updateQtspUserSelection(qtspData)).thenReturn(
                EudiRqesSetSelectedQtspPartialState.Success(service = rqesService)
            )

            // Act
            viewModel.setEvent(
                Event.BottomSheet.QtspSelectedOnDoneButtonPressed(qtspData)
            )

            // Assert
            viewModel.effect.runFlowTest {
                assertEquals(Effect.CloseBottomSheet, awaitItem())
                assertEquals(Effect.OnSelectedQtspUpdated(service = rqesService), awaitItem())
            }
        }

    // Case 5
    // Function setEvent() is called with Event.BottomSheet.QtspIndexSelectedOnRadioButtonPressed and
    // a specific index is selected in the QTSP selection bottom sheet
    // Case 5 Expected Result:
    // 1. Selected QTSP index in view state is updated to match the selected index
    // 2. State update occurs without side effects
    @Test
    fun `Given Case 5, When setEvent for Bottom Sheet is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val indexSelected = 0

            // Act
            viewModel.setEvent(
                Event.BottomSheet.QtspIndexSelectedOnRadioButtonPressed(index = indexSelected)
            )

            // Assert
            assertEquals(
                indexSelected,
                viewModel.viewState.value.selectedQtspIndex
            )
        }

    // Case 6
    // Event CertificateSelected is triggered by calling setEvent() with a selected certificate index.
    // Case 6 Expected Result:
    // 1. The ViewModel's state is updated with the selectedCertificateIndex set to the value passed in the event.
    @Test
    fun `Given Case 6, When setEvent for Bottom Sheet is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val selectedIndex = 0

            // Act
            viewModel.setEvent(
                Event.BottomSheet.CertificateIndexSelectedOnRadioButtonPressed(index = selectedIndex)
            )

            // Assert
            assertEquals(selectedIndex, viewModel.viewState.value.selectedCertificateIndex)
        }

    // Case 7
    // Function setEvent() is called with an Event.BottomSheet.CancelSignProcess.SecondaryButtonPressed event.
    // Case 7 Expected Result:
    // 1. The effect should trigger a Finish navigation action, indicating that the current screen will be finished.
    @Test
    fun `Given Case 7, When setEvent for Bottom Sheet is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Act
            viewModel.setEvent(
                Event.BottomSheet.CancelSignProcess.SecondaryButtonPressed
            )

            // Assert
            viewModel.effect.runFlowTest {
                val effect = awaitItem()
                assertTrue(effect is Effect.Navigation.Finish)
            }
        }

    // Case 8
    // Function setEvent() is called with an Event.BottomSheet.UpdateBottomSheetState event,
    // and argument isOpen with value of true.
    // Case 8 Expected Result:
    // 1. The view state should reflect that the bottom sheet is open,
    // correspondingly isBottomSheetOpen state value should be true.
    @Test
    fun `Given Case 8, When setEvent for Bottom Sheet is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Act
            viewModel.setEvent(
                Event.BottomSheet.UpdateBottomSheetState(isOpen = true)
            )

            // Assert
            assertTrue(viewModel.viewState.value.isBottomSheetOpen)
        }

    // Case 9
    // Function setEvent() is called with an Event.QtspSelectedOnDoneButtonPressed event on Certificate step and
    // EudiRqesGetServiceAuthorizationUrlPartialStateFailure is returned
    // Case 9 Expected Result:
    // 1. Bottom sheet is closed (CloseBottomSheet effect is emitted)
    // 2. Error state is set with correct error message
    // 3. Error configuration includes retry and cancel actions
    // 4. Error is properly dismissed when cancel is called
    @Test
    fun `Given Case 9, When setEvent for Bottom Sheet is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val failureState = EudiRqesSetSelectedQtspPartialState.Failure(
                error = EudiRQESUiError(message = mockedPlainFailureMessage)
            )
            whenever(optionsSelectionInteractor.updateQtspUserSelection(qtspData)).thenReturn(
                failureState
            )

            viewModel.setEvent(
                Event.BottomSheet.QtspSelectedOnDoneButtonPressed(
                    qtspData = qtspData
                )
            )

            // Assert
            with(viewModel.viewState.value) {
                // Verify error state
                assertNotNull(error)
                assertEquals(mockedPlainFailureMessage, error.errorSubTitle)

                // Test error configuration
                viewModel.effect.runFlowTest {
                    assertEquals(Effect.CloseBottomSheet, awaitItem())
                }

                // Test retry action
                error.onRetry?.invoke()

                // Test cancel action
                error.onCancel()
                assertNull(viewModel.viewState.value.error)
            }
        }

    // Case 10
    // Function setEvent() is called with an Event.BottomSheet.CertificateSelectedOnDoneButtonPressed event and
    // EudiRqesGetCredentialAuthorizationUrlPartialState.Success is returned with authorization URL
    // Case 10 Expected Result:
    // 1. Bottom bar button is configured with correct text ("Continue")
    // 2. Bottom bar button event contains the authorization URL
    // 3. Bottom bar is made visible
    // 4. No error state is present
    // 5. Proper interaction with getCredentialAuthorizationUrl is verified
    @Test
    fun `Given Case 10, When setEvent for Bottom Sheet is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val authorizationUrl = Uri.parse(mockedAuthorizationUrl)
            val mockedCertificates = listOf(
                certificateData
            )
            whenever(certificateData.name).thenReturn(mockedCertificateName)
            whenever(optionsSelectionInteractor.authorizeServiceAndFetchCertificates())
                .thenReturn(
                    OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState.Success(
                        certificates = mockedCertificates
                    )
                )

            whenever(optionsSelectionInteractor.getCredentialAuthorizationUrl(certificateData))
                .thenReturn(
                    EudiRqesGetCredentialAuthorizationUrlPartialState.Success(
                        authorizationUrl = authorizationUrl
                    )
                )

            // First load certificates
            viewModel.setEvent(
                Event.AuthorizeServiceAndFetchCertificates
            )

            // Act
            viewModel.setEvent(
                Event.BottomSheet.CertificateSelectedOnDoneButtonPressed(
                    certificateData
                )
            )

            // Assert
            viewModel.viewState.runFlowTest {
                with(viewModel.viewState.value) {
                    assertNotNull(bottomBarButtonAction)
                    bottomBarButtonAction.let { button ->
                        assertEquals(
                            resourceProvider.getLocalizedString(LocalizableKey.Continue),
                            button.buttonText
                        )

                        assertEquals(
                            authorizationUrl,
                            (button.event).uri
                        )
                    }

                    // Bottom bar visibility
                    assertTrue(isBottomBarButtonVisible)

                    // Verify no error state
                    assertNull(error)
                }
            }

            // Verify interaction
            verify(optionsSelectionInteractor).getCredentialAuthorizationUrl(certificateData)
        }

    // Case 11
    // Function setEvent() is called with complete certificate selection flow:
    // 1. Initialize with CERTIFICATE_SELECTION_STATE
    // 2. Fetch certificates successfully
    // 3. Select certificate index
    // 4. Show certificate selection
    // 5. Complete certificate selection with CertificateSelectedOnDoneButtonPressed
    // Case 11 Expected Result:
    // 1. Certificate selection item is properly updated with:
    //    - Correct certificate name as main text
    //    - Success color for leading icon
    //    - Disabled state
    // 2. Selected file and QTSP data are properly maintained
    // 3. Complete flow executes without errors
    @Test
    fun `Given Case 11, When setEvent for Bottom Sheet is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            whenever(optionsSelectionInteractor.getSelectedFile())
                .thenReturn(EudiRqesGetSelectedFilePartialState.Success(file = documentData))
            whenever(optionsSelectionInteractor.getSelectedQtsp())
                .thenReturn(
                    OptionsSelectionInteractorGetSelectedQtspPartialState.Success(
                        selectedQtsp = qtspData
                    )
                )

            val authorizationUrl = Uri.parse(mockedAuthorizationUrl)
            whenever(certificateData.name).thenReturn(mockedCertificateName)

            // Mock successful certificates fetch
            val mockCertificates = listOf(certificateData)
            whenever(optionsSelectionInteractor.authorizeServiceAndFetchCertificates())
                .thenReturn(
                    OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState.Success(
                        certificates = mockCertificates
                    )
                )

            // Mock authorization URL fetch
            whenever(optionsSelectionInteractor.getCredentialAuthorizationUrl(certificateData))
                .thenReturn(
                    EudiRqesGetCredentialAuthorizationUrlPartialState.Success(
                        authorizationUrl = authorizationUrl
                    )
                )

            viewModel.setEvent(Event.Initialize(screenSelectionState = CERTIFICATE_SELECTION_STATE))
            viewModel.setEvent(Event.AuthorizeServiceAndFetchCertificates)
            // Click certificate selection screen item
            viewModel.setEvent(Event.CertificateSelectionItemPressed)
            // Select certificate index
            val indexSelected = 0
            viewModel.setEvent(Event.BottomSheet.CertificateIndexSelectedOnRadioButtonPressed(index = indexSelected))

            // Act
            viewModel.setEvent(
                Event.BottomSheet.CertificateSelectedOnDoneButtonPressed(
                    certificateData
                )
            )

            // Assert
            with(viewModel.viewState.value) {
                assertNotNull(certificateSelectionItem)
                with(certificateSelectionItem) {
                    assertEquals(mockedCertificateName, mainText)
                    assertEquals(ThemeColors.success, leadingIconTint)
                    assertFalse(enabled)
                }
            }
        }

    // Case 12
    // Function setEvent() is called with complete certificate selection flow but
    // EudiRqesGetCredentialAuthorizationUrlPartialState.Failure is returned when fetching authorization URL
    // Flow:
    // 1. Successfully fetch certificates
    // 2. Select certificate index
    // 3. Attempt to get credential authorization URL (fails)
    // Case 12 Expected Result:
    // 1. Error state is set with:
    //    - Correct error message
    //    - Working retry action that:
    //      * Dismisses error
    //      * Attempts to get authorization URL again
    //    - Working cancel action that dismisses error
    // 2. Bottom bar is properly handled:
    //    - Not visible during error state
    //    - Button action is null
    // 3. Interactions are verified:
    //    - Authorization URL is requested correct number of times
    @Test
    fun `Given Case 12, When setEvent for Bottom Sheet is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val errorMessage = mockedPlainFailureMessage

            // Mock certificate data
            whenever(certificateData.name).thenReturn(mockedCertificateName)

            // Mock successful certificates fetch
            val mockedCertificates = listOf(certificateData)
            whenever(optionsSelectionInteractor.authorizeServiceAndFetchCertificates())
                .thenReturn(
                    OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState.Success(
                        certificates = mockedCertificates
                    )
                )

            // Mock authorization URL fetch failure
            whenever(optionsSelectionInteractor.getCredentialAuthorizationUrl(certificateData))
                .thenReturn(
                    EudiRqesGetCredentialAuthorizationUrlPartialState.Failure(
                        error = EudiRQESUiError(message = errorMessage)
                    )
                )

            // Initialize certificates
            viewModel.setEvent(Event.AuthorizeServiceAndFetchCertificates)

            // Select certificate index
            val indexSelected = 0
            viewModel.setEvent(
                Event.BottomSheet.CertificateIndexSelectedOnRadioButtonPressed(index = indexSelected)
            )

            // Act
            viewModel.setEvent(
                Event.BottomSheet.CertificateSelectedOnDoneButtonPressed(
                    certificateData
                )
            )

            // Assert
            with(viewModel.viewState.value) {
                // Verify error state
                assertNotNull(error)
                error.let { errorConfig ->
                    assertEquals(errorMessage, errorConfig.errorSubTitle)

                    // Test retry action
                    errorConfig.onRetry?.invoke()
                    // Should dismiss error and retry the event
                    verify(optionsSelectionInteractor, times(2))
                        .getCredentialAuthorizationUrl(certificateData)

                    // Test cancel action
                    errorConfig.onCancel()
                    // After cancel, error should be dismissed
                    assertNull(viewModel.viewState.value.error)
                }

                // Verify bottom bar is not visible in error state
                assertFalse(isBottomBarButtonVisible)
                assertNull(bottomBarButtonAction)
            }

            // Verify interactions
            verify(optionsSelectionInteractor, times(2))
                .getCredentialAuthorizationUrl(certificateData)
        }

    // Case 13
    // Function setEvent for BottomSheet.CancelQtspSelection
    @Test
    fun `Given Case 13, When BottomSheet CancelQtspSelection event is sent, Then CloseBottomSheet effect is observed`() =
        coroutineRule.runTest {
            viewModel.setEvent(
                Event.BottomSheet.CancelQtspSelection
            )

            viewModel.effect.runFlowTest {
                val effect = awaitItem()
                assertTrue(effect is Effect.CloseBottomSheet)
            }
        }

    // Case 14
    // Function setEvent for BottomSheet.CancelSignProcess.PrimaryButtonPressed
    @Test
    fun `Given Case 14, When BottomSheet CancelSignProcess PrimaryButtonPressed event is sent, Then CloseBottomSheet effect is observed`() =
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

    // Case 15
    // Function setEvent for BottomSheet.CancelCertificateSelection
    @Test
    fun `Given Case 15, When BottomSheet CancelCertificateSelection event is sent, Then CloseBottomSheet effect is observed`() =
        coroutineRule.runTest {
            viewModel.setEvent(
                Event.BottomSheet.CancelCertificateSelection
            )

            viewModel.effect.runFlowTest {
                val effect = awaitItem()
                assertTrue(effect is Effect.CloseBottomSheet)
            }
        }
    //endregion

    //region setEvent, AuthorizeServiceAndFetchCertificates, FetchServiceAuthorizationUrl

    // Case 1
    // Events AuthorizeServiceAndFetchCertificates and BottomBarButtonPressed are triggered, simulating a
    // scenario where certificates are successfully fetched and an authorization URL is returned.
    // Case 1 Expected Result:
    // 1. When BottomBarButtonPressed is triggered after successful certificate fetching, an OpenUrl effect is emitted.
    // 2. The emitted URL matches the mocked authorization URI.
    @Test
    fun `Given Case 1, When setEvent for AuthorizeServiceAndFetchCertificates is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val mockedCertificates = listOf(certificateData)
            val fetchCertificatesResponse =
                OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState.Success(
                    certificates = mockedCertificates
                )
            val authResponse = EudiRqesGetCredentialAuthorizationUrlPartialState.Success(
                authorizationUrl = authorizationUri
            )
            mockAuthorizeServiceAndFetchCertificatesCall(response = fetchCertificatesResponse)
            mockGetCredentialAuthorizationUrlCall(response = authResponse)

            // Act
            viewModel.setEvent(Event.AuthorizeServiceAndFetchCertificates)
            viewModel.setEvent(
                Event.BottomBarButtonPressed(
                    uri = authorizationUri
                )
            )

            // Assert
            viewModel.effect.runFlowTest {
                val effect = awaitItem()
                assertTrue(effect is Effect.OpenUrl)
                assertEquals(authorizationUri, (effect as Effect.OpenUrl).uri)
            }
        }

    // Case 2
    // Event AuthorizeServiceAndFetchCertificates is triggered, simulating a failure scenario where
    // the interactor returns a Failure state with an error message.
    // Case 2 Expected Result:
    // 1. The ViewModel's state contains a non-null error object.
    // 2. The error's subtitle matches the mocked failure message.
    @Test
    fun `Given Case 2, When setEvent for AuthorizeServiceAndFetchCertificates is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val response =
                OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState.Failure(
                    EudiRQESUiError(message = mockedFetchCertificatesFailureMessage)
                )
            mockAuthorizeServiceAndFetchCertificatesCall(response = response)

            // Act
            viewModel.setEvent(
                Event.AuthorizeServiceAndFetchCertificates
            )

            // Assert
            val viewState = viewModel.viewState.value
            assertTrue(viewState.error != null)
            assertEquals(mockedFetchCertificatesFailureMessage, viewState.error?.errorSubTitle)
        }

    // Case 3
    // Function setEvent() is called with an Event.FetchServiceAuthorizationUrl event and an
    // rqesService object as argument.
    // Case 3 Expected Result:
    // 1. The service authorization URL should be fetched, triggering Effect.OpenUrl with the authorization Uri.
    @Test
    fun `Given Case 3, When setEvent for FetchServiceAuthorizationUrl is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val response = EudiRqesGetServiceAuthorizationUrlPartialState.Success(
                authorizationUrl = mockedAuthorizationUrl.toUri()
            )
            mockGetServiceAuthorizationUrlCall(response = response)

            // Act
            viewModel.setEvent(
                Event.FetchServiceAuthorizationUrl(rqesService)
            )

            // Assert
            viewModel.effect.runFlowTest {
                assertTrue(awaitItem() is Effect.OpenUrl)
            }
        }
    //end region

    //region Other Events
    // Case 1
    // The function `setEvent` is tested to ensure that when the event `Event.BottomBarButtonPressed` is triggered
    // Case 1 Expected Result:
    // 1. The given uri should be opened and the activity should finish
    @Test
    fun `When setEvent for BottomBarButtonPressed is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Act
            viewModel.setEvent(
                Event.BottomBarButtonPressed(uri = mockedUri.toUri())
            )

            // Assert
            viewModel.effect.runFlowTest {
                assertEquals(Effect.OpenUrl(mockedUri.toUri()), awaitItem())
                assertEquals(Effect.Navigation.Finish, awaitItem())
            }
        }

    // Case 2
    // Function setEvent() is called with an Event.DismissError event.
    // Case 2 Expected Result:
    // 1. The view state should have its error field set to null, indicating that the error has been cleared.
    @Test
    fun `When setEvent for DismissError is called, Then the expected result is returned`() {
        // Act
        viewModel.setEvent(
            Event.DismissError
        )

        // Assert
        assertNull(viewModel.viewState.value.error)
    }

    // Case 3
    // Function setEvent(Event.Pop) is called to trigger the event of popping the current screen or action.
    // Case 3 Expected Result:
    // 1. The sheet content should be updated to the expected "ConfirmCancellation" content.
    // 2. The effect should trigger Effect.ShowBottomSheet, indicating that a bottom sheet is
    // displayed to the user.
    @Test
    fun `Given Case 3, When setEvent for Event Pop is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val sheetContent = OptionsSelectionBottomSheetContent.ConfirmCancellation(
                bottomSheetTextData = mockConfirmCancellationTextData()
            )

            // Act
            viewModel.setEvent(
                Event.Pop
            )

            // Assert
            assertEquals(sheetContent, viewModel.viewState.value.sheetContent)
            viewModel.effect.runFlowTest {
                val expectedEffect = Effect.ShowBottomSheet
                assertEquals(expectedEffect, awaitItem())
            }
        }

    // Case 4
    // Function setEvent(Event.Finish)
    @Test
    fun `When Finish event is sent, Then Navigation Finish effect is observed`() =
        coroutineRule.runTest {
            viewModel.setEvent(
                Event.Finish
            )

            viewModel.effect.runFlowTest {
                val effect = awaitItem()
                assertTrue(effect is Effect.Navigation.Finish)
            }
        }
    //endregion


    //region of helper functions
    private fun mockLocalizedStrings(resourceProvider: ResourceProvider) {
        LocalizableKey.entries.forEach { key ->
            whenever(resourceProvider.getLocalizedString(key))
                .thenReturn(key.defaultTranslation())
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

    private suspend fun mockGetServiceAuthorizationUrlCall(response: EudiRqesGetServiceAuthorizationUrlPartialState) {
        whenever(optionsSelectionInteractor.getServiceAuthorizationUrl(rqesService))
            .thenReturn(response)
    }

    private fun mockConfirmCancellationTextData(): BottomSheetTextData {
        return BottomSheetTextData(
            title = resourceProvider.getLocalizedString(LocalizableKey.CancelSignProcessTitle),
            message = resourceProvider.getLocalizedString(LocalizableKey.CancelSignProcessSubtitle),
            positiveButtonText = resourceProvider.getLocalizedString(LocalizableKey.CancelSignProcessPrimaryText),
            negativeButtonText = resourceProvider.getLocalizedString(LocalizableKey.CancelSignProcessSecondaryText),
        )
    }

    private suspend fun mockAuthorizeServiceAndFetchCertificatesCall(
        response: OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState
    ) {
        whenever(optionsSelectionInteractor.authorizeServiceAndFetchCertificates())
            .thenReturn(response)
    }

    private suspend fun mockGetCredentialAuthorizationUrlCall(response: EudiRqesGetCredentialAuthorizationUrlPartialState) {
        whenever(optionsSelectionInteractor.getCredentialAuthorizationUrl(certificateData))
            .thenReturn(response)
    }
    //endregion
}