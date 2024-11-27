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

package eu.europa.ec.eudi.rqesui.domain.controller

import eu.europa.ec.eudi.rqesui.domain.entities.error.EudiRQESUiError
import eu.europa.ec.eudi.rqesui.infrastructure.EudiRQESUi
import eu.europa.ec.eudi.rqesui.infrastructure.config.EudiRQESUiConfig
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.QtspData
import eu.europa.ec.eudi.rqesui.infrastructure.provider.ResourceProvider
import eu.europa.ec.eudi.rqesui.util.CoroutineTestRule
import eu.europa.ec.eudi.rqesui.util.mockedExceptionWithMessage
import eu.europa.ec.eudi.rqesui.util.mockedExceptionWithNoMessage
import eu.europa.ec.eudi.rqesui.util.mockedGenericErrorMessage
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
class TestRqesController {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @Mock
    private lateinit var eudiRQESUi: EudiRQESUi

    @Mock
    private lateinit var eudiRQESUiConfig: EudiRQESUiConfig

    @Mock
    private lateinit var currentSelection: EudiRQESUi.CurrentSelection

    @Mock
    private lateinit var resourceProvider: ResourceProvider

    @Mock
    private lateinit var documentData: DocumentData

    @Mock
    private lateinit var qtspDataList: List<QtspData>

    private lateinit var rqesController: RqesController

    private lateinit var closeable: AutoCloseable

    @Before
    fun setUp() {
        closeable = MockitoAnnotations.openMocks(this)
        rqesController =
            RqesControllerImpl(
                eudiRQESUi = eudiRQESUi,
                resourceProvider = resourceProvider,
            )
        whenever(resourceProvider.genericErrorMessage())
            .thenReturn(mockedGenericErrorMessage)
    }

    @After
    fun after() {
        closeable.close()
    }

    //region getSelectedFile
    // Case 1
    // Expected Result:
    // 1. The function should return an instance of `EudiRqesGetSelectedFilePartialState.Success`.
    // 2. The `file` property in the returned state should match the `currentSelection.file` value
    @Test
    fun `Given Case 1, When getSelectedFile is called, Then the expected result is returned`() {
        // Arrange
        whenever(eudiRQESUi.getCurrentSelection())
            .thenReturn(currentSelection)
        whenever(currentSelection.file)
            .thenReturn(documentData)

        // Act
        val result = rqesController.getSelectedFile()

        // Assert
        assertTrue(result is EudiRqesGetSelectedFilePartialState.Success)
        val state = result as EudiRqesGetSelectedFilePartialState.Success
        assertEquals(currentSelection.file, state.file)
    }

    // Case 2
    // 1. Set eudiRQESUi.getCurrentSelection() function ti return null, indicating no file being selected.
    // Expected Result:
    // 1. The function should return an instance of `EudiRqesGetSelectedFilePartialState.Failure`.
    @Test
    fun `Given Case 2, When getSelectedFile is called, Then the expected result is returned`() {
        // Arrange
        whenever(eudiRQESUi.getCurrentSelection())
            .thenReturn(null)

        // Act
        val result = rqesController.getSelectedFile()

        // Assert
        assertTrue(result is EudiRqesGetSelectedFilePartialState.Failure)
    }

    // Case 3
    // 1. Simulate the `eudiRQESUi.getCurrentSelection()` function throwing an exception.
    // 2. Ensure the exception includes a specific error message that can be used for validation.
    // Expected Result:
    // 1. The function should return an instance of `EudiRqesGetSelectedFilePartialState.Failure`.
    // 2. The failure state should contain the error message from the thrown exception.
    @Test
    fun `Given Case 3, When getSelectedFile is called, Then the expected result is returned`() {
        // Arrange
        whenever(eudiRQESUi.getCurrentSelection())
            .thenThrow(mockedExceptionWithMessage)

        // Act
        val result = rqesController.getSelectedFile()

        // Assert
        assertTrue(result is EudiRqesGetSelectedFilePartialState.Failure)
        val failureState = result as EudiRqesGetSelectedFilePartialState.Failure
        assertEquals(mockedExceptionWithMessage.message, failureState.error.message)
    }
    //endregion

    //region getQtsps
    // 1. Simulate the `eudiRQESUi.getEudiRQESUiConfig()` function returning a mock configuration.
    // 2. Ensure the mock configuration contains a list of QTSPs.
    // Expected Result:
    // 1. The function should return an instance of `EudiRqesGetQtspsPartialState.Success`.
    // 2. The success state should include the list of QTSPs.
    @Test
    fun `Given Case 1, When getQtsps is called, Then the expected result is returned`() {
        // Arrange
        whenever(eudiRQESUi.getEudiRQESUiConfig())
            .thenReturn(eudiRQESUiConfig)
        whenever(eudiRQESUiConfig.qtsps)
            .thenReturn(qtspDataList)

        // Act
        val result = rqesController.getQtsps()

        // Assert
        assertEquals(EudiRqesGetQtspsPartialState.Success(qtspDataList), result)
    }

    // Case 2
    // 1. Simulate the `eudiRQESUi.getEudiRQESUiConfig()` function throwing an exception without message.
    // 3. Define a mocked generic error message (`mockedGenericErrorMessage`) to be used as a fallback.
    // Expected Result:
    // 1. The function should return an instance of `EudiRqesGetQtspsPartialState.Failure`.
    // 2. The failure state should include an `EudiRQESUiError` with the generic error message.
    @Test
    fun `Given Case 2, When getQtsps is called, Then the expected result is returned`() {
        // Arrange
        whenever(eudiRQESUi.getEudiRQESUiConfig())
            .thenThrow(mockedExceptionWithNoMessage)

        // Act
        val result = rqesController.getQtsps()

        // Assert
        val expectedError = EudiRQESUiError(message = mockedGenericErrorMessage)
        assertEquals(
            expectedError.message,
            (result as EudiRqesGetQtspsPartialState.Failure).error.message,
        )
    }
    //endregion
}
