/*
 * Copyright (c) 2026 European Commission
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.europa.ec.eudi.rqesui.domain.controller

import android.net.Uri
import androidx.core.content.FileProvider
import eu.europa.ec.eudi.documentretrieval.DispatchOutcome
import eu.europa.ec.eudi.rqes.AuthorizationCode
import eu.europa.ec.eudi.rqes.CredentialInfo
import eu.europa.ec.eudi.rqes.HashAlgorithmOID
import eu.europa.ec.eudi.rqes.HttpsUrl
import eu.europa.ec.eudi.rqes.core.RQESService
import eu.europa.ec.eudi.rqes.core.SignedDocuments
import eu.europa.ec.eudi.rqes.core.documentRetrieval.ResolutionOutcome
import eu.europa.ec.eudi.rqesui.domain.entities.error.EudiRQESUiError
import eu.europa.ec.eudi.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.eudi.rqesui.domain.extension.toUriOrEmpty
import eu.europa.ec.eudi.rqesui.infrastructure.EudiRQESUi
import eu.europa.ec.eudi.rqesui.infrastructure.config.EudiRQESUiConfig
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.CertificateData
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.QtspData
import eu.europa.ec.eudi.rqesui.infrastructure.provider.ResourceProvider
import eu.europa.ec.eudi.rqesui.util.CoroutineTestRule
import eu.europa.ec.eudi.rqesui.util.mockedAuthorizationCode
import eu.europa.ec.eudi.rqesui.util.mockedAuthorizationHttpsUrl
import eu.europa.ec.eudi.rqesui.util.mockedAuthorizationUrl
import eu.europa.ec.eudi.rqesui.util.mockedCertificateName
import eu.europa.ec.eudi.rqesui.util.mockedCertificatesNotFoundMessage
import eu.europa.ec.eudi.rqesui.util.mockedClientId
import eu.europa.ec.eudi.rqesui.util.mockedClientSecret
import eu.europa.ec.eudi.rqesui.util.mockedDocumentName
import eu.europa.ec.eudi.rqesui.util.mockedDocumentNotFoundMessage
import eu.europa.ec.eudi.rqesui.util.mockedExceptionWithMessage
import eu.europa.ec.eudi.rqesui.util.mockedExceptionWithNoMessage
import eu.europa.ec.eudi.rqesui.util.mockedGenericErrorMessage
import eu.europa.ec.eudi.rqesui.util.mockedGenericErrorTitle
import eu.europa.ec.eudi.rqesui.util.mockedGenericServiceErrorMessage
import eu.europa.ec.eudi.rqesui.util.mockedLocalFileUri
import eu.europa.ec.eudi.rqesui.util.mockedQtspEndpoint
import eu.europa.ec.eudi.rqesui.util.mockedQtspName
import eu.europa.ec.eudi.rqesui.util.mockedQtspNotFound
import eu.europa.ec.eudi.rqesui.util.mockedTsaUrl
import eu.europa.ec.eudi.rqesui.util.mockedUri
import eu.europa.ec.eudi.rqesui.util.runTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mockStatic
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File
import java.net.URI

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
    private lateinit var sessionData: EudiRQESUi.SessionData

    @Mock
    private lateinit var resourceProvider: ResourceProvider

    @Mock
    private lateinit var documentData: DocumentData

    @Mock
    private lateinit var qtspData: QtspData

    @Mock
    private lateinit var qtspDataList: List<QtspData>

    @Mock
    private lateinit var rqesService: RQESService

    @Mock
    private lateinit var rqesServiceAuthorized: RQESService.Authorized

    @Mock
    private lateinit var credentialAuthorized: RQESService.CredentialAuthorized

    @Mock
    private lateinit var signedDocuments: SignedDocuments

    @Mock
    private lateinit var credentialInfo: CredentialInfo

    @Mock
    private lateinit var eudiRQESUiConfigForRetrieval: EudiRQESUiConfig

    @Mock
    private lateinit var resolutionOutcome: ResolutionOutcome

    @Mock
    private lateinit var shareableUri: Uri

    private lateinit var rqesController: RqesController

    private lateinit var closeable: AutoCloseable

    @Before
    fun setUp() {
        closeable = MockitoAnnotations.openMocks(this)
        rqesController =
            RqesControllerImpl(
                eudiRQESUi = eudiRQESUi,
                resourceProvider = resourceProvider,
                dispatcher = coroutineRule.testDispatcher,
            )
        whenever(resourceProvider.genericErrorMessage())
            .thenReturn(mockedGenericErrorMessage)
        whenever(resourceProvider.genericServiceErrorMessage())
            .thenReturn(mockedGenericServiceErrorMessage)
        whenever(resourceProvider.genericErrorTitle())
            .thenReturn(mockedGenericErrorTitle)
        whenever(resourceProvider.getSignedDocumentsCache())
            .thenReturn(RuntimeEnvironment.getApplication().cacheDir)
    }

    @After
    fun after() {
        closeable.close()
    }

    //region getSelectedFile
    // Case 1
    // Expected Result:
    // 1. The function should return an instance of `EudiRqesGetSelectedFilePartialState.Success`.
    // 2. The `file` property in the returned state should match the `sessionData.file` value
    @Test
    fun `Given Case 1, When getSelectedFile is called, Then the expected result is returned`() {
        // Arrange
        whenever(eudiRQESUi.getSessionData())
            .thenReturn(sessionData)
        whenever(sessionData.file)
            .thenReturn(documentData)

        // Act
        val result = rqesController.getSelectedFile()

        // Assert
        assertTrue(result is EudiRqesGetSelectedFilePartialState.Success)
        val state = result as EudiRqesGetSelectedFilePartialState.Success
        assertEquals(sessionData.file, state.file)
    }

    // Case 2
    // 1. Set eudiRQESUi.getSessionData() function ti return null, indicating no file being selected.
    // Expected Result:
    // 1. The function should return an instance of `EudiRqesGetSelectedFilePartialState.Failure`.
    @Test
    fun `Given Case 2, When getSelectedFile is called, Then the expected result is returned`() {
        // Arrange
        whenever(eudiRQESUi.getSessionData())
            .thenReturn(null)

        // Act
        val result = rqesController.getSelectedFile()

        // Assert
        assertTrue(result is EudiRqesGetSelectedFilePartialState.Failure)
    }

    // Case 3
    // 1. Simulate the `eudiRQESUi.getSessionData()` function throwing an exception.
    // 2. Ensure the exception includes a specific error message that can be used for validation.
    // Expected Result:
    // 1. The function should return an instance of `EudiRqesGetSelectedFilePartialState.Failure`.
    // 2. The failure state should contain the error message from the thrown exception.
    @Test
    fun `Given Case 3, When getSelectedFile is called, Then the expected result is returned`() {
        // Arrange
        whenever(eudiRQESUi.getSessionData())
            .thenThrow(mockedExceptionWithMessage)

        // Act
        val result = rqesController.getSelectedFile()

        // Assert
        assertTrue(result is EudiRqesGetSelectedFilePartialState.Failure)
        val failureState = result as EudiRqesGetSelectedFilePartialState.Failure
        assertEquals(mockedExceptionWithMessage.message, failureState.error.message)
    }

    // Case 4:
    // 1. This test case simulates a scenario where no file is selected (the `file` property is null).
    // 2. The localized error message for `GenericErrorDocumentNotFound` is mocked
    // Expected Result:
    // 1. The function returns an instance of `EudiRqesGetSelectedFilePartialState.Failure`.
    // 2. The failure state contains the expected error message from the resource provider.
    @Test
    fun `Given Case 4, When getSelectedFile is called, Then the expected result is returned`() {
        // Arrange
        whenever(eudiRQESUi.getSessionData())
            .thenReturn(sessionData)
        whenever(sessionData.file)
            .thenReturn(null)
        whenever(resourceProvider.getLocalizedString(LocalizableKey.GenericErrorDocumentNotFound))
            .thenReturn(mockedDocumentNotFoundMessage)

        // Act
        val result = rqesController.getSelectedFile()

        // Assert
        assertTrue(result is EudiRqesGetSelectedFilePartialState.Failure)
        val failureState = result as EudiRqesGetSelectedFilePartialState.Failure
        assertEquals(
            resourceProvider.getLocalizedString(LocalizableKey.GenericErrorDocumentNotFound),
            failureState.error.message
        )
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
        val expectedError =
            EudiRQESUiError(title = mockedGenericErrorTitle, message = mockedGenericErrorMessage)
        assertEquals(
            expectedError.message,
            (result as EudiRqesGetQtspsPartialState.Failure).error.message,
        )
    }
    //endregion

    //region setSelectedQtsp
    // Case 1
    // 1. Mock the RQES service configuration to simulate a successful setup of the service.
    // 2. Mock the `qtspData` to represent the selected QTSP.
    // 3. Ensure `eudiRQESUi.getSessionData()` returns the current selection.
    // Expected Result:
    // 1. The function should return an instance of `EudiRqesSetSelectedQtspPartialState.Success`.
    // 2. The success state should include a non-null service object, representing the created RQES service.
    @Test
    fun `Given Case 1, When setSelectedQtsp is called, Then the expected result is returned`() {
        // Arrange
        whenever(eudiRQESUi.getEudiRQESUiConfig()).thenReturn(eudiRQESUiConfig)
        whenever(eudiRQESUi.getSessionData()).thenReturn(sessionData)
        mockQTSPData(qtspData = qtspData)

        // Act
        val result = rqesController.setSelectedQtsp(qtspData)

        // Assert
        assertTrue(result is EudiRqesSetSelectedQtspPartialState.Success)
        assertNotNull((result as EudiRqesSetSelectedQtspPartialState.Success).service)
    }

    // Case 2
    // 1. The `getSessionData` method in `eudiRQESUi` throws an exception with specific message.
    // Expected Result:
    // 1. The function should return an instance of `EudiRqesSetSelectedQtspPartialState.Failure`.
    // 2. The failure state should confirm that the exception is caught and the failure is handled appropriately.
    @Test
    fun `Given Case 2, When setSelectedQtsp is called, Then the expected result is returned`() {
        // Arrange
        whenever(eudiRQESUi.getEudiRQESUiConfig()).thenReturn(eudiRQESUiConfig)
        whenever(eudiRQESUi.getSessionData()).thenThrow(mockedExceptionWithMessage)
        mockQTSPData(qtspData = qtspData)

        // Act
        val result = rqesController.setSelectedQtsp(qtspData)

        // Assert
        assertTrue(result is EudiRqesSetSelectedQtspPartialState.Failure)
    }
    //endregion

    //region getSelectedQtsp
    // Case 1
    // 1. Mock `eudiRQESUi.getSessionData()` to return `sessionData`
    // 2. Mock `sessionData.qtsp` to return `qtspData`, representing the selected QTSP.
    // Expected Result:
    // 1. The function should return an instance of `EudiRqesGetSelectedQtspPartialState.Success`.
    // 2. The success state should include the `qtsp` value matching the mocked `qtspData`.
    @Test
    fun `Given Case 1, When getSelectedQtsp is called, Then the expected result is returned`() {
        // Arrange
        whenever(eudiRQESUi.getSessionData())
            .thenReturn(sessionData)
        whenever(sessionData.qtsp)
            .thenReturn(qtspData)

        // Act
        val result = rqesController.getSelectedQtsp()

        // Assert
        assertTrue(result is EudiRqesGetSelectedQtspPartialState.Success)
        assertEquals(qtspData, (result as EudiRqesGetSelectedQtspPartialState.Success).qtsp)
    }

    // Case 2
    // 1. The `qtsp` field of the current selection is mocked to have a null value.
    // 2. The localized string for the generic QTSP not found error message is mocked.
    // Expected Result:
    // 1. The function should return an instance of `EudiRqesGetSelectedQtspPartialState.Failure`.
    // 2. The failure state should include an error message indicating that the QTSP could not be found.
    @Test
    fun `Given Case 2, When getSelectedQtsp is called, Then the expected result is returned`() {
        // Arrange
        whenever(eudiRQESUi.getSessionData())
            .thenReturn(sessionData)
        whenever(sessionData.qtsp)
            .thenReturn(null)
        whenever(resourceProvider.getLocalizedString(LocalizableKey.GenericErrorQtspNotFound))
            .thenReturn(mockedQtspNotFound)

        // Act
        val result = rqesController.getSelectedQtsp()

        // Assert
        assertTrue(result is EudiRqesGetSelectedQtspPartialState.Failure)
        assertEquals(
            resourceProvider.getLocalizedString(LocalizableKey.GenericErrorQtspNotFound),
            (result as EudiRqesGetSelectedQtspPartialState.Failure).error.message
        )
    }

    // Case 3
    // 1. When the `eudiRQESUi.getSessionData()` function is called, an exception with specific
    // message is thrown.
    // Expected Result:
    // 1. The function should return an instance of `EudiRqesGetSelectedQtspPartialState.Failure`.
    // 2. The failure state should include the error message from the exception.
    @Test
    fun `Given Case 3, When getSelectedQtsp is called, Then the expected result is returned`() {
        // Arrange
        whenever(eudiRQESUi.getSessionData())
            .thenThrow(mockedExceptionWithMessage)

        // Act
        val result = rqesController.getSelectedQtsp()

        // Assert
        assertTrue(result is EudiRqesGetSelectedQtspPartialState.Failure)
        assertEquals(
            mockedExceptionWithMessage.message,
            (result as EudiRqesGetSelectedQtspPartialState.Failure).error.message
        )
    }
    //endregion

    //region signDocuments
    // Case 1
    // 1. Mock `credentialAuthorized.signDocuments()` to return a successful result.
    // Expected Result:
    // 1. The function should return an instance of `EudiRqesSignDocumentsPartialState.Success`.
    // 2. The success state should indicate that the signing operation was completed successfully.
    @Test
    fun `Given Case 1, When signDocuments is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            whenever(credentialAuthorized.signDocuments())
                .thenReturn(Result.success(signedDocuments))

            // Act
            val result = rqesController.signDocuments(credentialAuthorized)

            // Assert
            assertTrue(result is EudiRqesSignDocumentsPartialState.Success)
        }

    // Case 2
    // 1. The `signDocuments` function throws an exception with a specific message.
    // Expected Result:
    // 1. The function should return an instance of `EudiRqesSignDocumentsPartialState.Failure`.
    // 2. The failure state should include the error message from the thrown exception.
    @Test
    fun `Given Case 2, When signDocuments throws an exception, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            whenever(credentialAuthorized.signDocuments())
                .thenThrow(mockedExceptionWithMessage)

            // Act
            val result = rqesController.signDocuments(credentialAuthorized)

            // Assert
            assertTrue(result is EudiRqesSignDocumentsPartialState.Failure)
            assertEquals(
                mockedGenericServiceErrorMessage,
                (result as EudiRqesSignDocumentsPartialState.Failure).error.message
            )
        }
    //endregion

    //region authorizeCredential
    // Case 1
    // 1. Mock `eudiRQESUi.getSessionData()`
    // 2. Mock `sessionData.authorizationCode` to return a mocked authorization code.
    // 3. Mock `eudiRQESUi.getAuthorizedService()` to return a mock of `rqesServiceAuthorized`.
    // 4. Mock `rqesServiceAuthorized.authorizeCredential()` to return a successful result
    // Expected Result:
    // 1. The function should return an instance of `EudiRqesAuthorizeCredentialPartialState.Success`.
    // 2. The success state should include the `credentialAuthorized` as the authorized credential.
    @Test
    fun `Given Case 1, When authorizeCredential is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            mockSessionData()
            whenever(sessionData.authorizationCode).thenReturn(mockedAuthorizationCode)
            whenever(eudiRQESUi.getAuthorizedService()).thenReturn(rqesServiceAuthorized)
            mockAuthorizeCredentialResultSuccess()

            // Act
            val result = rqesController.authorizeCredential()

            // Assert
            assertTrue(result is EudiRqesAuthorizeCredentialPartialState.Success)
            assertEquals(
                credentialAuthorized,
                (result as EudiRqesAuthorizeCredentialPartialState.Success).authorizedCredential,
            )
        }

    // Case 2
    // 1. Mock eudiRQESUi.getSessionData() to return a valid sessionData.
    // 2. Mock eudiRQESUi.getAuthorizedService() to return null.
    // Expected Result:
    // 1. The function should return an instance of `EudiRqesAuthorizeCredentialPartialState.Failure`.
    // 2. The failure state should be triggered.
    @Test
    fun `Given Case 2, When authorizeCredential is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            mockSessionData()
            whenever(eudiRQESUi.getAuthorizedService())
                .thenReturn(null)

            // Act
            val result = rqesController.authorizeCredential()

            // Assert
            assertTrue(result is EudiRqesAuthorizeCredentialPartialState.Failure)
        }

    // Case 3
    // 1. Mock `eudiRQESUi.getSessionData()` to return a valid `sessionData`.
    // 2. Mock `sessionData.authorizationCode` to return a mocked authorization code.
    // 3. Mock `eudiRQESUi.getAuthorizedService()` to return a mock of `rqesServiceAuthorized`.
    // 4. Mock `rqesServiceAuthorized.authorizeCredential()` to throw a `RuntimeException` with message.
    // Expected Result:
    // 1. The function should return an instance of `EudiRqesAuthorizeCredentialPartialState.Failure`.
    // 2. The failure state should include the error message "Authorization failed".
    @Test
    fun `Given Case 3, When authorizeCredential is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            mockSessionData()
            whenever(sessionData.authorizationCode).thenReturn(mockedAuthorizationCode)
            whenever(eudiRQESUi.getAuthorizedService()).thenReturn(rqesServiceAuthorized)
            whenever(
                rqesServiceAuthorized.authorizeCredential(AuthorizationCode(mockedAuthorizationCode)),
            ).thenThrow(mockedExceptionWithMessage)

            // Act
            val result = rqesController.authorizeCredential()

            // Assert
            assertTrue(result is EudiRqesAuthorizeCredentialPartialState.Failure)
            assertEquals(
                mockedGenericServiceErrorMessage,
                (result as EudiRqesAuthorizeCredentialPartialState.Failure).error.message,
            )
        }
    //endregion

    //region getServiceAuthorizationUrl
    // Case 1
    // 1. Mock `rqesService.getServiceAuthorizationUrl()` to return a `Result.success` with
    // an authorization URL.
    // 2. Ensure the mocked URL matches the expected format and can be converted to a URI.
    // Expected Result:
    // 1. The function should return an instance of `EudiRqesGetServiceAuthorizationUrlPartialState.Success`.
    // 2. The `authorizationUrl` in the success state should match the expected mocked URL converted to a URI.
    @Test
    fun `Given Case 1, When getServiceAuthorizationUrl is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val authorizationResult = HttpsUrl(mockedAuthorizationHttpsUrl).getOrThrow()
            whenever(rqesService.getServiceAuthorizationUrl()).thenReturn(
                Result.success(authorizationResult),
            )

            // Act
            val result = rqesController.getServiceAuthorizationUrl(rqesService)

            // Assert
            assertTrue(result is EudiRqesGetServiceAuthorizationUrlPartialState.Success)
            assertEquals(
                authorizationResult.value.toString().toUriOrEmpty(),
                (result as EudiRqesGetServiceAuthorizationUrlPartialState.Success).authorizationUrl,
            )
        }

    // Case 2
    // 1. Mock rqesService getServiceAuthorizationUrl() to throw an exception with error message.
    // Expected Result:
    // 1. The function should return an instance of `EudiRqesGetServiceAuthorizationUrlPartialState.Failure`.
    // 2. The `error.message` in the failure state should match the exception message.
    @Test
    fun `Given Case 2, When getServiceAuthorizationUrl is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            whenever(rqesService.getServiceAuthorizationUrl())
                .thenThrow(mockedExceptionWithMessage)

            // Act
            val result = rqesController.getServiceAuthorizationUrl(rqesService)

            // Assert
            assertTrue(result is EudiRqesGetServiceAuthorizationUrlPartialState.Failure)
            assertEquals(
                mockedGenericServiceErrorMessage,
                (result as EudiRqesGetServiceAuthorizationUrlPartialState.Failure).error.message,
            )
        }
    //endregion

    //region setAuthorizedService
    // Case 1
    // Expected Result:
    // 1. The setAuthorizedService on eudiRQESUi should be called exactly once with a rqesServiceAuthorized argument.
    @Test
    fun `Given Case 1, When setAuthorizedService is called, Then the expected result is returned`() {
        // Act
        rqesController.setAuthorizedService(rqesServiceAuthorized)

        // Assert
        verify(eudiRQESUi, times(1))
            .setAuthorizedService(rqesServiceAuthorized)
    }
    //endregion

    //region authorizeService
    // Case 1
    // 1. Mock RQES current selection.
    // 2. Mock `eudiRQESUi.getRqesService()` to return `rqesService`.
    // 3. Mock `sessionData.authorizationCode` to return `mockedAuthorizationCode`.
    // 4. Mock `rqesService.authorizeService` to return a successful result with `rqesServiceAuthorized`.
    // Expected Result:
    // 1. The result should be of type `EudiRqesAuthorizeServicePartialState.Success`.
    // 2. The `authorizedService` property in the success state should match `rqesServiceAuthorized`.
    @Test
    fun `Given Case 1, When authorizeService is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            mockSessionData()
            whenever(eudiRQESUi.getRqesService()).thenReturn(rqesService)
            whenever(sessionData.authorizationCode).thenReturn(mockedAuthorizationCode)
            whenever(
                rqesService.authorizeService(AuthorizationCode(mockedAuthorizationCode)),
            ).thenReturn(Result.success(rqesServiceAuthorized))

            // Act
            val result = rqesController.authorizeService()

            // Assert
            assertTrue(result is EudiRqesAuthorizeServicePartialState.Success)
            assertEquals(
                rqesServiceAuthorized,
                (result as EudiRqesAuthorizeServicePartialState.Success).authorizedService,
            )
        }

    // Case 2
    // 1. Mock `eudiRQESUi.getRqesService()` to return `null`, indicating no available service.
    // 2. Mock the RQES current selection.
    // Expected Result:
    // 1. The result should be of type `EudiRqesAuthorizeServicePartialState.Failure`.
    // 2. The `error.message` property in the failure state should match `mockedGenericErrorMessage`.
    @Test
    fun `Given Case 2, When authorizeService is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            whenever(eudiRQESUi.getRqesService()).thenReturn(null)
            mockSessionData()

            // Act
            val result = rqesController.authorizeService()

            // Assert
            assertTrue(result is EudiRqesAuthorizeServicePartialState.Failure)
            assertEquals(
                mockedGenericErrorMessage,
                (result as EudiRqesAuthorizeServicePartialState.Failure).error.message,
            )
        }

    // Case 3
    // 1. Mock `eudiRQESUi.getRqesService()`.
    // 2. Mock `sessionData.authorizationCode` to return `mockedAuthorizationCode`.
    // 3. Mock `rqesService.authorizeService()` to throw an exception.
    // 4. Mock the current selection.
    // Expected Result:
    // 1. The result should be of type `EudiRqesAuthorizeServicePartialState.Failure`.
    // 2. The `error.message` property in the failure state should match `mockedExceptionWithMessage.message`.
    @Test
    fun `Given Case 3, When authorizeService is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            whenever(eudiRQESUi.getRqesService()).thenReturn(rqesService)
            whenever(sessionData.authorizationCode).thenReturn(mockedAuthorizationCode)
            whenever(
                rqesService.authorizeService(AuthorizationCode(mockedAuthorizationCode)),
            ).thenThrow(mockedExceptionWithMessage)
            mockSessionData()

            // Act
            val result = rqesController.authorizeService()

            // Assert
            assertTrue(result is EudiRqesAuthorizeServicePartialState.Failure)
            assertEquals(
                mockedGenericServiceErrorMessage,
                (result as EudiRqesAuthorizeServicePartialState.Failure).error.message,
            )
        }
    //endregion

    //region getAvailableCertificates
    // Case 1
    // 1. Create a mocked certificate data object, `mockedCertificateData`, containing a name and `credentialInfo`.
    // 2. Prepare a mocked list of CertificateData.
    // 3. Extract the list of credentials from the certificates list.
    // 4. Stub `rqesServiceAuthorized.listCredentials()` to return a successful result.
    // 5. Stub `resourceProvider.getLocalizedString()` to return a localized certificate name for each certificate, based on its index in the list.
    // Expected Result:
    // 1. The result should be of type `EudiRqesGetCertificatesPartialState.Success`.
    // 2. The `certificates` property in the success state should match `mockedCertificatesList`.
    @Test
    fun `Given Case 1, When getAvailableCertificates is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val mockedCertificateData =
                CertificateData(name = mockedCertificateName, certificate = credentialInfo)
            val mockedCertificatesList = listOf(mockedCertificateData)
            val mockCredentialInfoList = mockedCertificatesList.map { it.certificate }
            mockCertificatesDefaultNames(certificatesDataList = mockedCertificatesList)
            whenever(rqesServiceAuthorized.listCredentials())
                .thenReturn(Result.success(mockCredentialInfoList))

            // Act
            val result = rqesController.getAvailableCertificates(rqesServiceAuthorized)

            // Assert
            assertTrue(result is EudiRqesGetCertificatesPartialState.Success)
            assertEquals(
                mockedCertificatesList,
                (result as EudiRqesGetCertificatesPartialState.Success).certificates,
            )
        }

    // Case 2
    // 1. Mock `rqesServiceAuthorized.listCredentials()` to return a successful result with an empty list.
    // 2. Simulating a "no certificates found" scenario.
    // Expected Result:
    // 1. The result should be of type `EudiRqesGetCertificatesPartialState.Failure`.
    // 2. The `error.message` property in the failure state should match `mockedCertificatesNotFoundMessage`.
    @Test
    fun `Given Case 2, When getAvailableCertificates is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            whenever(rqesServiceAuthorized.listCredentials())
                .thenReturn(Result.success(emptyList()))
            mockNoCertificatesFoundMessage()

            // Act
            val result = rqesController.getAvailableCertificates(rqesServiceAuthorized)

            // Assert
            assertTrue(result is EudiRqesGetCertificatesPartialState.Failure)
            assertEquals(
                mockedCertificatesNotFoundMessage,
                (result as EudiRqesGetCertificatesPartialState.Failure).error.message,
            )
        }

    // Case 3
    // 1. Mock `rqesServiceAuthorized.listCredentials()` to throw an exception.
    // Expected Result:
    // 1. The result should be of type `EudiRqesGetCertificatesPartialState.Failure`.
    // 2. The error message property in the failure state should match message of the mocked exception.
    @Test
    fun `Given Case 3, When getAvailableCertificates is called, Then the expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            whenever(rqesServiceAuthorized.listCredentials())
                .thenThrow(mockedExceptionWithMessage)

            // Act
            val result = rqesController.getAvailableCertificates(rqesServiceAuthorized)

            // Assert
            assertTrue(result is EudiRqesGetCertificatesPartialState.Failure)
            assertEquals(
                mockedGenericServiceErrorMessage,
                (result as EudiRqesGetCertificatesPartialState.Failure).error.message,
            )
        }
    //endregion

    //region getRemoteOrLocalFile
    // Case 1
    // 1. Mock eudiRQESUi.getSessionData() to return session data with a non-null file.
    // Expected Result:
    // 1. The function should return Success with the file (delegating to getSelectedFile()).
    @Test
    fun `Given Case 1, When getRemoteOrLocalFile is called, Then Success state with the selected file is returned`() =
        coroutineRule.runTest {
            // Arrange
            whenever(eudiRQESUi.getSessionData()).thenReturn(sessionData)
            whenever(sessionData.file).thenReturn(documentData)

            // Act
            val result = rqesController.getRemoteOrLocalFile()

            // Assert
            assertTrue(result is EudiRqesGetSelectedFilePartialState.Success)
            assertEquals(documentData, (result as EudiRqesGetSelectedFilePartialState.Success).file)
        }

    // Case 2
    // 1. Mock eudiRQESUi.getSessionData() to return session data with both file and remoteUrl null.
    // 2. Mock resourceProvider.getLocalizedString() for GenericErrorDocumentNotFound.
    // Expected Result:
    // 1. The function should return Failure with the "document not found" error message.
    @Test
    fun `Given Case 2, When getRemoteOrLocalFile is called, Then Failure with document not found is returned`() =
        coroutineRule.runTest {
            // Arrange
            whenever(eudiRQESUi.getSessionData()).thenReturn(sessionData)
            whenever(sessionData.file).thenReturn(null)
            whenever(sessionData.remoteUrl).thenReturn(null)
            whenever(resourceProvider.getLocalizedString(LocalizableKey.GenericErrorDocumentNotFound))
                .thenReturn(mockedDocumentNotFoundMessage)

            // Act
            val result = rqesController.getRemoteOrLocalFile()

            // Assert
            assertTrue(result is EudiRqesGetSelectedFilePartialState.Failure)
            assertEquals(
                mockedDocumentNotFoundMessage,
                (result as EudiRqesGetSelectedFilePartialState.Failure).error.message
            )
        }

    // Case 3
    // 1. Mock eudiRQESUi.getSessionData() to throw an exception with a message.
    // Expected Result:
    // 1. The function should return Failure via the getOrElse fallback path.
    @Test
    fun `Given Case 3, When getRemoteOrLocalFile is called, Then Failure via getOrElse is returned`() =
        coroutineRule.runTest {
            // Arrange
            whenever(eudiRQESUi.getSessionData()).thenThrow(mockedExceptionWithMessage)

            // Act
            val result = rqesController.getRemoteOrLocalFile()

            // Assert
            assertTrue(result is EudiRqesGetSelectedFilePartialState.Failure)
            assertEquals(
                mockedExceptionWithMessage.message,
                (result as EudiRqesGetSelectedFilePartialState.Failure).error.message
            )
        }

    // Case 4
    // 1. sessionData.file is null and sessionData.remoteUrl is non-null.
    // 2. documentRetrievalConfig.impl is null (NoValidation).
    // 3. The DocumentRetrievalService construction succeeds but resolveDocument throws because
    //    the URL is unreachable in a test environment.
    // Expected Result:
    // 1. The runCatching catches the exception and the function returns Failure via getOrElse.
    @Test
    fun `Given Case 4, When getRemoteOrLocalFile is called with remoteUrl and null impl, Then Failure is returned`() =
        coroutineRule.runTest {
            // Arrange
            whenever(eudiRQESUi.getSessionData()).thenReturn(sessionData)
            whenever(sessionData.file).thenReturn(null)
            whenever(sessionData.remoteUrl).thenReturn(Uri.parse("https://example.org/unreachable"))
            whenever(eudiRQESUi.getEudiRQESUiConfig()).thenReturn(eudiRQESUiConfigForRetrieval)
            whenever(eudiRQESUiConfigForRetrieval.documentRetrievalConfig)
                .thenReturn(eu.europa.ec.eudi.rqesui.infrastructure.config.DocumentRetrievalConfig.NoValidation)
            whenever(resourceProvider.getDownloadsCache())
                .thenReturn(RuntimeEnvironment.getApplication().cacheDir)

            // Act
            val result = rqesController.getRemoteOrLocalFile()

            // Assert
            assertTrue(result is EudiRqesGetSelectedFilePartialState.Failure)
        }

    // Case 5
    // 1. sessionData.remoteUrl is non-null and documentRetrievalConfig.impl is non-null
    //    (taking the X509CertificateImpl branch of supportedClientIdSchemes).
    // 2. The DocumentRetrievalService is mocked via mockConstruction so resolveDocument returns
    //    a successful outcome with a single resolved document.
    // Expected Result:
    // 1. The function should return Success with the resolved document data.
    @Test
    fun `Given Case 5, When getRemoteOrLocalFile is called with remoteUrl and a single resolved document, Then Success is returned`() =
        coroutineRule.runTest {
            // Arrange
            val context = RuntimeEnvironment.getApplication().applicationContext
            val resolvedFile = File(context.cacheDir, "remote_$mockedDocumentName").apply {
                writeBytes(ByteArray(8))
            }
            val remoteUri = Uri.parse("https://example.org/test-doc")

            whenever(eudiRQESUi.getSessionData()).thenReturn(sessionData)
            whenever(sessionData.file).thenReturn(null)
            whenever(sessionData.remoteUrl).thenReturn(remoteUri)
            whenever(eudiRQESUi.getEudiRQESUiConfig()).thenReturn(eudiRQESUiConfigForRetrieval)
            val trustImpl = org.mockito.kotlin.mock<eu.europa.ec.eudi.documentretrieval.X509CertificateTrust>()
            whenever(eudiRQESUiConfigForRetrieval.documentRetrievalConfig)
                .thenReturn(
                    eu.europa.ec.eudi.rqesui.infrastructure.config.DocumentRetrievalConfig.X509CertificateImpl(
                        impl = trustImpl
                    )
                )
            whenever(resourceProvider.getDownloadsCache()).thenReturn(context.cacheDir)

            val resolvedDocument = org.mockito.kotlin.mock<eu.europa.ec.eudi.rqes.core.documentRetrieval.ResolvedDocument>()
            whenever(resolvedDocument.file).thenReturn(resolvedFile)

            val outcome = org.mockito.kotlin.mock<ResolutionOutcome>()
            whenever(outcome.resolvedDocuments).thenReturn(listOf(resolvedDocument))

            val implClass = Class.forName(
                "eu.europa.ec.eudi.rqes.core.documentRetrieval.DocumentRetrievalServiceImpl"
            )
            @Suppress("UNCHECKED_CAST")
            org.mockito.Mockito.mockConstruction(
                implClass as Class<eu.europa.ec.eudi.rqes.core.documentRetrieval.DocumentRetrievalService>
            ) { mock, _ ->
                kotlinx.coroutines.runBlocking {
                    whenever(mock.resolveDocument(any())).thenReturn(Result.success(outcome))
                }
            }.use {

                    // Act
                    val result = rqesController.getRemoteOrLocalFile()

                    // Assert
                    assertTrue(result is EudiRqesGetSelectedFilePartialState.Success)
                }
        }

    // Case 6
    // 1. resolveDocument returns an outcome with multiple resolved documents.
    // Expected Result:
    // 1. The function should return Failure with the "multiple documents not supported" error.
    @Test
    fun `Given Case 6, When getRemoteOrLocalFile is called and resolveDocument returns multiple documents, Then Failure is returned`() =
        coroutineRule.runTest {
            // Arrange
            val context = RuntimeEnvironment.getApplication().applicationContext
            val file1 = File(context.cacheDir, "f1.pdf").apply { writeBytes(ByteArray(8)) }
            val file2 = File(context.cacheDir, "f2.pdf").apply { writeBytes(ByteArray(8)) }

            whenever(eudiRQESUi.getSessionData()).thenReturn(sessionData)
            whenever(sessionData.file).thenReturn(null)
            whenever(sessionData.remoteUrl).thenReturn(Uri.parse("https://example.org/test-doc"))
            whenever(eudiRQESUi.getEudiRQESUiConfig()).thenReturn(eudiRQESUiConfigForRetrieval)
            whenever(eudiRQESUiConfigForRetrieval.documentRetrievalConfig)
                .thenReturn(eu.europa.ec.eudi.rqesui.infrastructure.config.DocumentRetrievalConfig.NoValidation)
            whenever(resourceProvider.getDownloadsCache()).thenReturn(context.cacheDir)
            whenever(resourceProvider.getLocalizedString(LocalizableKey.GenericErrorDocumentMultipleNotSupported))
                .thenReturn("Multiple documents are not supported")

            val rd1 = org.mockito.kotlin.mock<eu.europa.ec.eudi.rqes.core.documentRetrieval.ResolvedDocument>()
            whenever(rd1.file).thenReturn(file1)
            val rd2 = org.mockito.kotlin.mock<eu.europa.ec.eudi.rqes.core.documentRetrieval.ResolvedDocument>()
            whenever(rd2.file).thenReturn(file2)

            val outcome = org.mockito.kotlin.mock<ResolutionOutcome>()
            whenever(outcome.resolvedDocuments).thenReturn(listOf(rd1, rd2))

            val implClass = Class.forName(
                "eu.europa.ec.eudi.rqes.core.documentRetrieval.DocumentRetrievalServiceImpl"
            )
            @Suppress("UNCHECKED_CAST")
            org.mockito.Mockito.mockConstruction(
                implClass as Class<eu.europa.ec.eudi.rqes.core.documentRetrieval.DocumentRetrievalService>
            ) { mock, _ ->
                kotlinx.coroutines.runBlocking {
                    whenever(mock.resolveDocument(any())).thenReturn(Result.success(outcome))
                }
            }.use {

                    // Act
                    val result = rqesController.getRemoteOrLocalFile()

                    // Assert
                    assertTrue(result is EudiRqesGetSelectedFilePartialState.Failure)
                    assertEquals(
                        "Multiple documents are not supported",
                        (result as EudiRqesGetSelectedFilePartialState.Failure).error.message
                    )
                }
        }

    // Case 7
    // 1. resolveDocument returns an outcome with an empty resolved documents list.
    // Expected Result:
    // 1. The function should return Failure with the "document not found" error from the
    //    fall-through path after the remoteUrl let block.
    @Test
    fun `Given Case 7, When getRemoteOrLocalFile is called and resolveDocument returns empty, Then Failure is returned`() =
        coroutineRule.runTest {
            // Arrange
            val context = RuntimeEnvironment.getApplication().applicationContext

            whenever(eudiRQESUi.getSessionData()).thenReturn(sessionData)
            whenever(sessionData.file).thenReturn(null)
            whenever(sessionData.remoteUrl).thenReturn(Uri.parse("https://example.org/test-doc"))
            whenever(eudiRQESUi.getEudiRQESUiConfig()).thenReturn(eudiRQESUiConfigForRetrieval)
            whenever(eudiRQESUiConfigForRetrieval.documentRetrievalConfig)
                .thenReturn(eu.europa.ec.eudi.rqesui.infrastructure.config.DocumentRetrievalConfig.NoValidation)
            whenever(resourceProvider.getDownloadsCache()).thenReturn(context.cacheDir)
            whenever(resourceProvider.getLocalizedString(LocalizableKey.GenericErrorDocumentNotFound))
                .thenReturn(mockedDocumentNotFoundMessage)

            val outcome = org.mockito.kotlin.mock<ResolutionOutcome>()
            whenever(outcome.resolvedDocuments).thenReturn(emptyList())

            val implClass = Class.forName(
                "eu.europa.ec.eudi.rqes.core.documentRetrieval.DocumentRetrievalServiceImpl"
            )
            @Suppress("UNCHECKED_CAST")
            org.mockito.Mockito.mockConstruction(
                implClass as Class<eu.europa.ec.eudi.rqes.core.documentRetrieval.DocumentRetrievalService>
            ) { mock, _ ->
                kotlinx.coroutines.runBlocking {
                    whenever(mock.resolveDocument(any())).thenReturn(Result.success(outcome))
                }
            }.use {

                    // Act
                    val result = rqesController.getRemoteOrLocalFile()

                    // Assert
                    assertTrue(result is EudiRqesGetSelectedFilePartialState.Failure)
                    assertEquals(
                        mockedDocumentNotFoundMessage,
                        (result as EudiRqesGetSelectedFilePartialState.Failure).error.message
                    )
                }
        }
    //endregion

    //region getAuthorizedService
    // Case 1
    // 1. Mock eudiRQESUi.getAuthorizedService() to return a non-null authorized service.
    // Expected Result:
    // 1. The function delegates to eudiRQESUi.getAuthorizedService() and returns the same instance.
    @Test
    fun `Given Case 1, When getAuthorizedService is called, Then the underlying service is returned`() {
        // Arrange
        whenever(eudiRQESUi.getAuthorizedService()).thenReturn(rqesServiceAuthorized)

        // Act
        val result = rqesController.getAuthorizedService()

        // Assert
        assertEquals(rqesServiceAuthorized, result)
    }

    // Case 2
    // 1. Mock eudiRQESUi.getAuthorizedService() to return null.
    // Expected Result:
    // 1. The function delegates to eudiRQESUi.getAuthorizedService() and returns null.
    @Test
    fun `Given Case 2, When getAuthorizedService is called, Then null is returned`() {
        // Arrange
        whenever(eudiRQESUi.getAuthorizedService()).thenReturn(null)

        // Act
        val result = rqesController.getAuthorizedService()

        // Assert
        assertNull(result)
    }
    //endregion

    //region getCredentialAuthorizationUrl
    // Case 1
    // 1. Mock eudiRQESUi.getSessionData() to return sessionData with a null file.
    // 2. Mock resourceProvider.getLocalizedString() for GenericErrorDocumentNotFound.
    // Expected Result:
    // 1. The function should return Failure with the "document not found" error.
    @Test
    fun `Given Case 1, When getCredentialAuthorizationUrl is called, Then Failure with document not found is returned`() =
        coroutineRule.runTest {
            // Arrange
            whenever(eudiRQESUi.getSessionData()).thenReturn(sessionData)
            whenever(sessionData.file).thenReturn(null)
            val certificateData =
                CertificateData(name = mockedCertificateName, certificate = credentialInfo)
            whenever(resourceProvider.getLocalizedString(LocalizableKey.GenericErrorDocumentNotFound))
                .thenReturn(mockedDocumentNotFoundMessage)

            // Act
            val result = rqesController.getCredentialAuthorizationUrl(
                authorizedService = rqesServiceAuthorized,
                certificateData = certificateData
            )

            // Assert
            assertTrue(result is EudiRqesGetCredentialAuthorizationUrlPartialState.Failure)
            assertEquals(
                mockedDocumentNotFoundMessage,
                (result as EudiRqesGetCredentialAuthorizationUrlPartialState.Failure).error.message
            )
        }

    // Case 2
    // 1. Mock eudiRQESUi.getSessionData() to throw an exception.
    // Expected Result:
    // 1. The function should return Failure via the getOrElse fallback path with the service error message.
    @Test
    fun `Given Case 2, When getCredentialAuthorizationUrl is called and an exception is thrown, Then Failure is returned`() =
        coroutineRule.runTest {
            // Arrange
            whenever(eudiRQESUi.getSessionData()).thenThrow(mockedExceptionWithMessage)
            val certificateData =
                CertificateData(name = mockedCertificateName, certificate = credentialInfo)

            // Act
            val result = rqesController.getCredentialAuthorizationUrl(
                authorizedService = rqesServiceAuthorized,
                certificateData = certificateData
            )

            // Assert
            assertTrue(result is EudiRqesGetCredentialAuthorizationUrlPartialState.Failure)
            assertEquals(
                mockedGenericServiceErrorMessage,
                (result as EudiRqesGetCredentialAuthorizationUrlPartialState.Failure).error.message
            )
        }

    // Case 3
    // 1. Mock a real-context based scenario where the file is present in session data.
    // 2. Use a file:// URI to a real existing file on disk so uriToFile can copy from it.
    // 3. Mock authorizedService.getCredentialAuthorizationUrl() to return a valid HttpsUrl.
    // Expected Result:
    // 1. The function returns Success with the parsed authorization Uri.
    @Test
    fun `Given Case 3, When getCredentialAuthorizationUrl is called with a valid file, Then Success is returned`() =
        coroutineRule.runTest {
            // Arrange
            val context = RuntimeEnvironment.getApplication().applicationContext
            whenever(resourceProvider.provideContext()).thenReturn(context)
            // Create a real file on disk and use its file:// URI so the contentResolver can open it.
            val sourceFile = File(context.cacheDir, "source_$mockedDocumentName").apply {
                writeBytes(ByteArray(8))
            }
            val realDocumentData = DocumentData(
                documentName = mockedDocumentName,
                uri = Uri.fromFile(sourceFile)
            )
            whenever(eudiRQESUi.getSessionData()).thenReturn(sessionData)
            whenever(sessionData.file).thenReturn(realDocumentData)

            val certificateData =
                CertificateData(name = mockedCertificateName, certificate = credentialInfo)
            val httpsUrl = HttpsUrl(mockedAuthorizationHttpsUrl).getOrThrow()
            whenever(
                rqesServiceAuthorized.getCredentialAuthorizationUrl(any(), any(), anyOrNull())
            ).thenReturn(Result.success(httpsUrl))

            // Act
            val result = rqesController.getCredentialAuthorizationUrl(
                authorizedService = rqesServiceAuthorized,
                certificateData = certificateData
            )

            // Assert
            assertTrue(result is EudiRqesGetCredentialAuthorizationUrlPartialState.Success)
            assertEquals(
                httpsUrl.value.toString().toUriOrEmpty(),
                (result as EudiRqesGetCredentialAuthorizationUrlPartialState.Success).authorizationUrl
            )
        }

    // Case 4
    // 1. File is present in session data.
    // 2. authorizedService.getCredentialAuthorizationUrl() returns a Result.failure causing getOrThrow to throw.
    // Expected Result:
    // 1. The function should return Failure via the getOrElse path with the service error message.
    @Test
    fun `Given Case 4, When getCredentialAuthorizationUrl is called and authorized service fails, Then Failure is returned`() =
        coroutineRule.runTest {
            // Arrange
            val context = RuntimeEnvironment.getApplication().applicationContext
            whenever(resourceProvider.provideContext()).thenReturn(context)
            File(context.cacheDir, mockedDocumentName).apply { createNewFile() }
            val realDocumentData = DocumentData(
                documentName = mockedDocumentName,
                uri = Uri.parse(mockedLocalFileUri)
            )
            whenever(eudiRQESUi.getSessionData()).thenReturn(sessionData)
            whenever(sessionData.file).thenReturn(realDocumentData)

            val certificateData =
                CertificateData(name = mockedCertificateName, certificate = credentialInfo)
            whenever(
                rqesServiceAuthorized.getCredentialAuthorizationUrl(any(), any(), anyOrNull())
            ).thenReturn(Result.failure(mockedExceptionWithMessage))

            // Act
            val result = rqesController.getCredentialAuthorizationUrl(
                authorizedService = rqesServiceAuthorized,
                certificateData = certificateData
            )

            // Assert
            assertTrue(result is EudiRqesGetCredentialAuthorizationUrlPartialState.Failure)
            assertEquals(
                mockedGenericServiceErrorMessage,
                (result as EudiRqesGetCredentialAuthorizationUrlPartialState.Failure).error.message
            )
        }
    //endregion

    //region saveSignedDocuments
    // Case 1
    // 1. Provide an empty SignedDocuments map.
    // Expected Result:
    // 1. The function should return Failure because savedDocuments is empty.
    @Test
    fun `Given Case 1, When saveSignedDocuments is called with empty documents, Then Failure is returned`() =
        coroutineRule.runTest {
            // Arrange
            val emptySignedDocuments = newSignedDocuments(emptyMap())
            whenever(eudiRQESUi.getRemoteResolutionOutcome()).thenReturn(null)

            // Act
            val result = rqesController.saveSignedDocuments(
                originalDocumentName = mockedDocumentName,
                signedDocuments = emptySignedDocuments
            )

            // Assert
            assertTrue(result is EudiRqesSaveSignedDocumentsPartialState.Failure)
            assertEquals(
                mockedGenericErrorMessage,
                (result as EudiRqesSaveSignedDocumentsPartialState.Failure).error.message
            )
        }

    // Case 2
    // 1. Provide a non-empty SignedDocuments map.
    // 2. Mock FileProvider.getUriForFile to return a shareable Uri.
    // 3. Mock getRemoteResolutionOutcome to return null (local flow).
    // Expected Result:
    // 1. The function should return Success with isRemote=false and null redirect Uri.
    @Test
    fun `Given Case 2, When saveSignedDocuments is called with non-empty documents and no remote outcome, Then Success isRemote false is returned`() =
        coroutineRule.runTest {
            // Arrange
            val context = RuntimeEnvironment.getApplication().applicationContext
            whenever(resourceProvider.provideContext()).thenReturn(context)
            val signedFile = File(context.cacheDir, "signed_$mockedDocumentName")
            val nonEmptySignedDocs = newSignedDocuments(mapOf(mockedDocumentName to signedFile))
            whenever(eudiRQESUi.getRemoteResolutionOutcome()).thenReturn(null)

            mockStatic(FileProvider::class.java).use { mocked ->
                mocked.`when`<Uri> {
                    FileProvider.getUriForFile(eq(context), any(), eq(signedFile))
                }.thenReturn(shareableUri)

                // Act
                val result = rqesController.saveSignedDocuments(
                    originalDocumentName = mockedDocumentName,
                    signedDocuments = nonEmptySignedDocs
                )

                // Assert
                assertTrue(result is EudiRqesSaveSignedDocumentsPartialState.Success)
                val success = result as EudiRqesSaveSignedDocumentsPartialState.Success
                assertEquals(false, success.isRemote)
                assertNull(success.redirectUri)
                assertEquals(mapOf(mockedDocumentName to shareableUri), success.savedDocuments)
            }
        }

    // Case 3
    // 1. Provide a non-empty SignedDocuments map.
    // 2. Mock FileProvider.getUriForFile to return a shareable Uri.
    // 3. Mock getRemoteResolutionOutcome to return a resolution outcome which dispatches Accepted.
    // Expected Result:
    // 1. The function should return Success with isRemote=true and the redirect Uri from outcome.
    @Test
    fun `Given Case 3, When saveSignedDocuments is called and remote outcome is Accepted, Then Success isRemote true with redirectUri is returned`() =
        coroutineRule.runTest {
            // Arrange
            val context = RuntimeEnvironment.getApplication().applicationContext
            whenever(resourceProvider.provideContext()).thenReturn(context)
            val signedFile = File(context.cacheDir, "signed_$mockedDocumentName")
            val nonEmptySignedDocs = newSignedDocuments(mapOf(mockedDocumentName to signedFile))
            whenever(eudiRQESUi.getRemoteResolutionOutcome()).thenReturn(resolutionOutcome)
            val redirectUri = URI.create(mockedAuthorizationUrl)
            whenever(resolutionOutcome.dispatch(nonEmptySignedDocs))
                .thenReturn(DispatchOutcome.Accepted(redirectURI = redirectUri))

            mockStatic(FileProvider::class.java).use { mocked ->
                mocked.`when`<Uri> {
                    FileProvider.getUriForFile(eq(context), any(), eq(signedFile))
                }.thenReturn(shareableUri)

                // Act
                val result = rqesController.saveSignedDocuments(
                    originalDocumentName = mockedDocumentName,
                    signedDocuments = nonEmptySignedDocs
                )

                // Assert
                assertTrue(result is EudiRqesSaveSignedDocumentsPartialState.Success)
                val success = result as EudiRqesSaveSignedDocumentsPartialState.Success
                assertEquals(true, success.isRemote)
                assertNotNull(success.redirectUri)
            }
        }

    // Case 4
    // 1. Provide a non-empty SignedDocuments map.
    // 2. Mock FileProvider.getUriForFile to return a shareable Uri.
    // 3. Mock getRemoteResolutionOutcome to return a resolution outcome which dispatches Rejected.
    // Expected Result:
    // 1. The function should return Failure.
    @Test
    fun `Given Case 4, When saveSignedDocuments is called and remote outcome is Rejected, Then Failure is returned`() =
        coroutineRule.runTest {
            // Arrange
            val context = RuntimeEnvironment.getApplication().applicationContext
            whenever(resourceProvider.provideContext()).thenReturn(context)
            val signedFile = File(context.cacheDir, "signed_$mockedDocumentName")
            val nonEmptySignedDocs = newSignedDocuments(mapOf(mockedDocumentName to signedFile))
            whenever(eudiRQESUi.getRemoteResolutionOutcome()).thenReturn(resolutionOutcome)
            whenever(resolutionOutcome.dispatch(nonEmptySignedDocs))
                .thenReturn(DispatchOutcome.Rejected)

            mockStatic(FileProvider::class.java).use { mocked ->
                mocked.`when`<Uri> {
                    FileProvider.getUriForFile(eq(context), any(), eq(signedFile))
                }.thenReturn(shareableUri)

                // Act
                val result = rqesController.saveSignedDocuments(
                    originalDocumentName = mockedDocumentName,
                    signedDocuments = nonEmptySignedDocs
                )

                // Assert
                assertTrue(result is EudiRqesSaveSignedDocumentsPartialState.Failure)
                assertEquals(
                    mockedGenericErrorMessage,
                    (result as EudiRqesSaveSignedDocumentsPartialState.Failure).error.message
                )
            }
        }

    // Case 5
    // 1. Provide a non-empty SignedDocuments map.
    // 2. Mock resourceProvider.provideContext() to throw an exception with a message.
    // Expected Result:
    // 1. The function should return Failure via the getOrElse fallback with the exception's localized message.
    @Test
    fun `Given Case 5, When saveSignedDocuments throws an exception, Then Failure with exception message is returned`() =
        coroutineRule.runTest {
            // Arrange
            val context = RuntimeEnvironment.getApplication().applicationContext
            val signedFile = File(context.cacheDir, "signed_$mockedDocumentName")
            val nonEmptySignedDocs = newSignedDocuments(mapOf(mockedDocumentName to signedFile))
            whenever(resourceProvider.provideContext()).thenThrow(mockedExceptionWithMessage)

            // Act
            val result = rqesController.saveSignedDocuments(
                originalDocumentName = mockedDocumentName,
                signedDocuments = nonEmptySignedDocs
            )

            // Assert
            assertTrue(result is EudiRqesSaveSignedDocumentsPartialState.Failure)
            assertEquals(
                mockedExceptionWithMessage.message,
                (result as EudiRqesSaveSignedDocumentsPartialState.Failure).error.message
            )
        }

    // Case 6
    // 1. Provide a non-empty SignedDocuments map.
    // 2. Mock resourceProvider.provideContext() to throw an exception without a message.
    // Expected Result:
    // 1. The function should return Failure via the getOrElse fallback with the generic error message.
    @Test
    fun `Given Case 6, When saveSignedDocuments throws exception without message, Then Failure with generic message is returned`() =
        coroutineRule.runTest {
            // Arrange
            val context = RuntimeEnvironment.getApplication().applicationContext
            val signedFile = File(context.cacheDir, "signed_$mockedDocumentName")
            val nonEmptySignedDocs = newSignedDocuments(mapOf(mockedDocumentName to signedFile))
            whenever(resourceProvider.provideContext()).thenThrow(mockedExceptionWithNoMessage)

            // Act
            val result = rqesController.saveSignedDocuments(
                originalDocumentName = mockedDocumentName,
                signedDocuments = nonEmptySignedDocs
            )

            // Assert
            assertTrue(result is EudiRqesSaveSignedDocumentsPartialState.Failure)
            assertEquals(
                mockedGenericErrorMessage,
                (result as EudiRqesSaveSignedDocumentsPartialState.Failure).error.message
            )
        }
    //endregion

    //region elvis fallback paths for exception with no message
    // Case 5
    // 1. Exception without message is thrown by getSessionData inside getSelectedFile.
    // Expected Result:
    // 1. The function returns Failure with the generic error message (elvis fallback).
    @Test
    fun `Given Case 5, When getSelectedFile is called and exception has no message, Then generic error message is used`() {
        // Arrange
        whenever(eudiRQESUi.getSessionData()).thenThrow(mockedExceptionWithNoMessage)

        // Act
        val result = rqesController.getSelectedFile()

        // Assert
        assertTrue(result is EudiRqesGetSelectedFilePartialState.Failure)
        assertEquals(
            mockedGenericErrorMessage,
            (result as EudiRqesGetSelectedFilePartialState.Failure).error.message
        )
    }

    // Case 6
    // 1. Exception without message is thrown by getSessionData inside getSelectedQtsp.
    // Expected Result:
    // 1. The function returns Failure with the generic error message (elvis fallback).
    @Test
    fun `Given Case 6, When getSelectedQtsp is called and exception has no message, Then generic error message is used`() {
        // Arrange
        whenever(eudiRQESUi.getSessionData()).thenThrow(mockedExceptionWithNoMessage)

        // Act
        val result = rqesController.getSelectedQtsp()

        // Assert
        assertTrue(result is EudiRqesGetSelectedQtspPartialState.Failure)
        assertEquals(
            mockedGenericErrorMessage,
            (result as EudiRqesGetSelectedQtspPartialState.Failure).error.message
        )
    }
    //endregion

    //region saveSignedDocuments - Accepted with null redirectURI
    // Case 7
    // 1. Remote outcome is Accepted with a null redirectURI.
    // Expected Result:
    // 1. Success with isRemote=true and null redirectUri (covers the safe-call ?. chain).
    @Test
    fun `Given Case 7, When saveSignedDocuments Accepted outcome has null redirectURI, Then Success with null redirectUri is returned`() =
        coroutineRule.runTest {
            // Arrange
            val context = RuntimeEnvironment.getApplication().applicationContext
            whenever(resourceProvider.provideContext()).thenReturn(context)
            val signedFile = File(context.cacheDir, "signed_$mockedDocumentName")
            val nonEmptySignedDocs = newSignedDocuments(mapOf(mockedDocumentName to signedFile))
            whenever(eudiRQESUi.getRemoteResolutionOutcome()).thenReturn(resolutionOutcome)
            whenever(resolutionOutcome.dispatch(nonEmptySignedDocs))
                .thenReturn(DispatchOutcome.Accepted(redirectURI = null))

            mockStatic(FileProvider::class.java).use { mocked ->
                mocked.`when`<Uri> {
                    FileProvider.getUriForFile(eq(context), any(), eq(signedFile))
                }.thenReturn(shareableUri)

                // Act
                val result = rqesController.saveSignedDocuments(
                    originalDocumentName = mockedDocumentName,
                    signedDocuments = nonEmptySignedDocs
                )

                // Assert
                assertTrue(result is EudiRqesSaveSignedDocumentsPartialState.Success)
                val success = result as EudiRqesSaveSignedDocumentsPartialState.Success
                assertEquals(true, success.isRemote)
                assertNull(success.redirectUri)
            }
        }
    //endregion

    //region saveSignedDocuments - URI toString returns null edge case
    // Case 8
    // 1. DispatchOutcome.Accepted's redirectURI is a URI whose toString() returns null
    //    (mocked since java.net.URI is normally guaranteed non-null toString).
    // Expected Result:
    // 1. The safe-call chain `outcome.redirectURI?.toString()?.toUri()` resolves to null on the
    //    second `?.` step, yielding Success with null redirectUri.
    @Test
    fun `Given Case 8, When saveSignedDocuments and URI toString returns null, Then null redirectUri Success is returned`() =
        coroutineRule.runTest {
            // Arrange
            val context = RuntimeEnvironment.getApplication().applicationContext
            whenever(resourceProvider.provideContext()).thenReturn(context)
            val signedFile = File(context.cacheDir, "signed_$mockedDocumentName")
            val nonEmptySignedDocs = newSignedDocuments(mapOf(mockedDocumentName to signedFile))
            whenever(eudiRQESUi.getRemoteResolutionOutcome()).thenReturn(resolutionOutcome)

            val mockedRedirectURI = org.mockito.kotlin.mock<URI>()
            // Force URI.toString() to return null to cover the second safe-call branch.
            org.mockito.Mockito.doReturn(null).`when`(mockedRedirectURI).toString()
            whenever(resolutionOutcome.dispatch(nonEmptySignedDocs))
                .thenReturn(DispatchOutcome.Accepted(redirectURI = mockedRedirectURI))

            mockStatic(FileProvider::class.java).use { mocked ->
                mocked.`when`<Uri> {
                    FileProvider.getUriForFile(eq(context), any(), eq(signedFile))
                }.thenReturn(shareableUri)

                // Act
                val result = rqesController.saveSignedDocuments(
                    originalDocumentName = mockedDocumentName,
                    signedDocuments = nonEmptySignedDocs
                )

                // Assert
                assertTrue(result is EudiRqesSaveSignedDocumentsPartialState.Success)
                val success = result as EudiRqesSaveSignedDocumentsPartialState.Success
                assertEquals(true, success.isRemote)
                assertNull(success.redirectUri)
            }
        }
    //endregion

    //region setSelectedQtsp - outer getOrElse elvis fallback
    // Case 5
    // 1. Exception without message is thrown by getSessionData (called inside the runCatching body
    //    when constructing copy(qtsp = qtspData)).
    // Expected Result:
    // 1. The function returns Failure with the generic error message (outer getOrElse elvis fallback).
    @Test
    fun `Given Case 5, When setSelectedQtsp outer getOrElse runs with no-message exception, Then generic error is returned`() {
        // Arrange
        whenever(eudiRQESUi.getEudiRQESUiConfig()).thenReturn(eudiRQESUiConfig)
        whenever(eudiRQESUi.getSessionData()).thenThrow(mockedExceptionWithNoMessage)
        mockQTSPData(qtspData = qtspData)

        // Act
        val result = rqesController.setSelectedQtsp(qtspData)

        // Assert
        assertTrue(result is EudiRqesSetSelectedQtspPartialState.Failure)
        assertEquals(
            mockedGenericErrorMessage,
            (result as EudiRqesSetSelectedQtspPartialState.Failure).error.message
        )
    }
    //endregion

    //region setSelectedQtsp - createRqesService Failure path
    // Case 3
    // 1. Mock resourceProvider.getSignedDocumentsCache() to throw an exception while createRqesService
    //    is being executed (after the session data is set successfully).
    // Expected Result:
    // 1. The function should return Failure with the EudiRqesCreateServicePartialState.Failure mapped into
    //    an EudiRqesSetSelectedQtspPartialState.Failure carrying the exception's message.
    @Test
    fun `Given Case 3, When setSelectedQtsp triggers createRqesService failure, Then SetSelectedQtsp Failure is returned`() {
        // Arrange
        whenever(eudiRQESUi.getSessionData()).thenReturn(sessionData)
        mockQTSPData(qtspData = qtspData)
        whenever(resourceProvider.getSignedDocumentsCache())
            .thenThrow(mockedExceptionWithMessage)

        // Act
        val result = rqesController.setSelectedQtsp(qtspData)

        // Assert
        assertTrue(result is EudiRqesSetSelectedQtspPartialState.Failure)
        assertEquals(
            mockedExceptionWithMessage.message,
            (result as EudiRqesSetSelectedQtspPartialState.Failure).error.message
        )
    }

    // Case 4
    // 1. Mock resourceProvider.getSignedDocumentsCache() to throw an exception without a message.
    // Expected Result:
    // 1. The function should return Failure with the generic error message via the fallback.
    @Test
    fun `Given Case 4, When setSelectedQtsp triggers createRqesService failure with no message, Then generic error is returned`() {
        // Arrange
        whenever(eudiRQESUi.getSessionData()).thenReturn(sessionData)
        mockQTSPData(qtspData = qtspData)
        whenever(resourceProvider.getSignedDocumentsCache())
            .thenThrow(mockedExceptionWithNoMessage)

        // Act
        val result = rqesController.setSelectedQtsp(qtspData)

        // Assert
        assertTrue(result is EudiRqesSetSelectedQtspPartialState.Failure)
        assertEquals(
            mockedGenericErrorMessage,
            (result as EudiRqesSetSelectedQtspPartialState.Failure).error.message
        )
    }
    //endregion

    // region mock data
    private fun mockQTSPData(qtspData: QtspData) {
        with(qtspData) {
            whenever(this.name).thenReturn(mockedQtspName)
            whenever(this.endpoint).thenReturn(mockedQtspEndpoint.toUriOrEmpty())
            whenever(this.tsaUrl).thenReturn(mockedTsaUrl)
            whenever(this.clientId).thenReturn(mockedClientId)
            whenever(this.clientSecret).thenReturn(mockedClientSecret)
            whenever(this.authFlowRedirectionURI).thenReturn(URI.create(mockedUri))
            whenever(this.hashAlgorithm).thenReturn(HashAlgorithmOID.SHA_256)
        }
    }

    private suspend fun mockAuthorizeCredentialResultSuccess() {
        whenever(rqesServiceAuthorized.authorizeCredential(AuthorizationCode(code = mockedAuthorizationCode)))
            .thenReturn(Result.success(credentialAuthorized))
    }

    private fun mockSessionData() {
        whenever(eudiRQESUi.getSessionData()).thenReturn(sessionData)
    }

    private fun mockNoCertificatesFoundMessage() {
        whenever(resourceProvider.getLocalizedString(LocalizableKey.GenericErrorCertificatesNotFound))
            .thenReturn(mockedCertificatesNotFoundMessage)
    }

    private fun newSignedDocuments(entries: Map<String, File>): SignedDocuments {
        // The SignedDocuments constructor is internal in the rqes-core library, so we use reflection
        // to construct it for our tests.
        val ctor = SignedDocuments::class.java.getDeclaredConstructor(Map::class.java)
        ctor.isAccessible = true
        return ctor.newInstance(entries)
    }

    private fun mockCertificatesDefaultNames(certificatesDataList: List<CertificateData>) {
        certificatesDataList.forEachIndexed { index, _ ->
            whenever(
                resourceProvider.getLocalizedString(
                    LocalizableKey.Certificate,
                    listOf((index + 1).toString()),
                ),
            ).thenReturn("Certificate ${index + 1}")
        }
    }
    //endregion
}
