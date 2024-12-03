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

package eu.europa.ec.eudi.rqesui.presentation.ui.view_document

import android.net.Uri
import eu.europa.ec.eudi.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.eudi.rqesui.domain.serializer.UiSerializer
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.infrastructure.provider.ResourceProvider
import eu.europa.ec.eudi.rqesui.presentation.entities.config.ViewDocumentUiConfig
import eu.europa.ec.eudi.rqesui.util.CoroutineTestRule
import eu.europa.ec.eudi.rqesui.util.mockedDocumentName
import eu.europa.ec.eudi.rqesui.util.mockedLocalFileUri
import eu.europa.ec.eudi.rqesui.util.mockedSerializedConfig
import eu.europa.ec.eudi.rqesui.util.runFlowTest
import eu.europa.ec.eudi.rqesui.util.runTest
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TestViewDocumentViewModel {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @Mock
    private lateinit var uiSerializer: UiSerializer

    @Mock
    private lateinit var resourceProvider: ResourceProvider

    private lateinit var viewModel: ViewDocumentViewModel

    private lateinit var autoCloseable: AutoCloseable

    private val documentFileUri = Uri.parse(mockedLocalFileUri)

    private val deserializedConfig = ViewDocumentUiConfig(
        isSigned = true,
        documentData = DocumentData(
            documentName = mockedDocumentName,
            uri = documentFileUri
        )
    )

    @Before
    fun setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this)
        whenever(
            uiSerializer.fromBase64(
                payload = mockedSerializedConfig,
                model = ViewDocumentUiConfig::class.java,
                parser = ViewDocumentUiConfig.Parser
            )
        ).thenReturn(deserializedConfig)

        viewModel = ViewDocumentViewModel(
            uiSerializer = uiSerializer,
            resourceProvider = resourceProvider,
            serializedViewDocumentUiConfig = mockedSerializedConfig
        )
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
    // 1. The isLoading field is set to true.
    // 2. The config field is populated with the deserialized ViewDocumentUiConfig.
    // 3. The buttonText field is set to the localized string for Close.
    @Test
    fun `Given Case 1, When setInitialState is called, Then the expected result is returned`() {
        // Arrange
        whenever(
            uiSerializer.fromBase64(
                payload = mockedSerializedConfig,
                model = ViewDocumentUiConfig::class.java,
                parser = ViewDocumentUiConfig.Parser
            )
        ).thenReturn(deserializedConfig)

        // Act
        val initialState = viewModel.setInitialState()

        // Assert
        assertEquals(true, initialState.isLoading)
        assertEquals(deserializedConfig, initialState.config)
        assertEquals(
            resourceProvider.getLocalizedString(LocalizableKey.Close),
            initialState.buttonText
        )
    }

    // Case 2
    // Function setInitialState() is called when the deserialization returns null.
    // Case 2 Expected Result:
    // 1. A RuntimeException should be thrown thrown.
    @Test
    fun `Given Case 2, When setInitialState is called, Then the expected result is returned`() {
        whenever(
            uiSerializer.fromBase64(
                payload = mockedSerializedConfig,
                model = ViewDocumentUiConfig::class.java,
                parser = ViewDocumentUiConfig.Parser
            )
        ).thenReturn(null)

        // Assert that a RuntimeException is thrown
        assertThrows(RuntimeException::class.java) {
            viewModel.setInitialState()
        }
    }
    //endregion

    //region handleEvents
    // Case 1
    // Function handleEvents() is called with Event.Pop as argument.
    // Case 1 Expected Result:
    // 1. The effect should trigger an `Effect.Navigation.Pop` action.
    @Test
    fun `Given Case 1, When handleEvents is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Act
            viewModel.handleEvents(Event.Pop)

            // Assert
            viewModel.effect.runFlowTest {
                val expectedResult = Effect.Navigation.Pop
                assertEquals(expectedResult, awaitItem())
            }
        }
    //endregion

    //region setEvent
    // Case 1
    // Function setEvent() is called with an Event.Pop event.
    // Case 1 Expected Result:
    // 1. The effect should trigger an `Effect.Navigation.Pop` action.
    @Test
    fun `Given Case 1, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Act
            viewModel.setEvent(Event.Pop)

            // Assert
            viewModel.effect.runFlowTest {
                val expectedResult = Effect.Navigation.Pop
                assertEquals(expectedResult, awaitItem())
            }
        }

    // Case 2
    // Function setEvent() is called with an Event.BottomBarButtonPressed event.
    // Case 2 Expected Result:
    // 1. The effect should trigger an `Effect.Navigation.Pop` action.
    @Test
    fun `Given Case 2, When setEvent is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Act
            viewModel.setEvent(Event.BottomBarButtonPressed)

            // Assert
            viewModel.effect.runFlowTest {
                val expectedResult = Effect.Navigation.Pop
                assertEquals(expectedResult, awaitItem())
            }
        }

    // Case 3
    // Function setEvent() is called with an Event.LoadingStateChanged event where isLoading is set to false.
    // Case 3 Expected Result:
    // 1. The view state should be updated with the a loading state value of false.
    @Test
    fun `Given Case 3 When setEvent is called, Then the expected result is returned`() {
        // Act
        viewModel.setEvent(
            Event.LoadingStateChanged(isLoading = false)
        )

        val expectedState = false
        assertEquals(expectedState, viewModel.viewState.value.isLoading)
    }
    //endregion

    // region of helper functions
    private fun mockLocalizedStrings(resourceProvider: ResourceProvider) {
        LocalizableKey.entries.forEach { key ->
            whenever(resourceProvider.getLocalizedString(key)).thenReturn(key.defaultTranslation())
        }
    }
    //endregion
}