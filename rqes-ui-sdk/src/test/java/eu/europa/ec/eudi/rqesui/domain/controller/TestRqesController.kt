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

import eu.europa.ec.eudi.rqes.AuthorizationCode
import eu.europa.ec.eudi.rqes.CredentialInfo
import eu.europa.ec.eudi.rqes.HashAlgorithmOID
import eu.europa.ec.eudi.rqes.HttpsUrl
import eu.europa.ec.eudi.rqes.core.RQESService
import eu.europa.ec.eudi.rqes.core.SignedDocuments
import eu.europa.ec.eudi.rqesui.domain.entities.error.EudiRQESUiError
import eu.europa.ec.eudi.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.eudi.rqesui.domain.extension.toUri
import eu.europa.ec.eudi.rqesui.infrastructure.EudiRQESUi
import eu.europa.ec.eudi.rqesui.infrastructure.config.EudiRQESUiConfig
import eu.europa.ec.eudi.rqesui.infrastructure.config.RqesServiceConfig
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.CertificateData
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.QtspData
import eu.europa.ec.eudi.rqesui.infrastructure.provider.ResourceProvider
import eu.europa.ec.eudi.rqesui.util.CoroutineTestRule
import eu.europa.ec.eudi.rqesui.util.mockedAuthorizationCode
import eu.europa.ec.eudi.rqesui.util.mockedAuthorizationHttpsUrl
import eu.europa.ec.eudi.rqesui.util.mockedCertificateName
import eu.europa.ec.eudi.rqesui.util.mockedCertificatesNotFoundMessage
import eu.europa.ec.eudi.rqesui.util.mockedClientId
import eu.europa.ec.eudi.rqesui.util.mockedClientSecret
import eu.europa.ec.eudi.rqesui.util.mockedDocumentNotFoundMessage
import eu.europa.ec.eudi.rqesui.util.mockedExceptionWithMessage
import eu.europa.ec.eudi.rqesui.util.mockedExceptionWithNoMessage
import eu.europa.ec.eudi.rqesui.util.mockedGenericErrorMessage
import eu.europa.ec.eudi.rqesui.util.mockedGenericServiceErrorMessage
import eu.europa.ec.eudi.rqesui.util.mockedQtspEndpoint
import eu.europa.ec.eudi.rqesui.util.mockedQtspName
import eu.europa.ec.eudi.rqesui.util.mockedQtspNotFound
import eu.europa.ec.eudi.rqesui.util.mockedScaUrl
import eu.europa.ec.eudi.rqesui.util.mockedUri
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
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.URI
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
    private lateinit var rqesServiceConfig: RqesServiceConfig

    @Mock
    private lateinit var rqesServiceAuthorized: RQESService.Authorized

    @Mock
    private lateinit var credentialAuthorized: RQESService.CredentialAuthorized

    @Mock
    private lateinit var signedDocuments: SignedDocuments

    @Mock
    private lateinit var credentialInfo: CredentialInfo

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
        whenever(resourceProvider.genericServiceErrorMessage())
            .thenReturn(mockedGenericServiceErrorMessage)
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
        val expectedError = EudiRQESUiError(message = mockedGenericErrorMessage)
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
        mockRQESServiceConfig(eudiRQESUi = eudiRQESUi)
        mockQTSPData(qtspData = qtspData)

        // Act
        val result = rqesController.setSelectedQtsp(qtspData)

        // Assert
        assertTrue(result is EudiRqesSetSelectedQtspPartialState.Success)
        assertNotNull((result as EudiRqesSetSelectedQtspPartialState.Success).service)
    }

    // Case 2
    // 1. A failure scenario is simulated where the RQES service configuration is null.
    // 2. The `qtspData` are mocked to represent the QTSP being selected.
    // 3. The `eudiRQESUi.getSessionData()` function provides the current selection.
    // Expected Result:
    // 1. The function returns an instance of `EudiRqesSetSelectedQtspPartialState.Failure`.
    // 2. The failure state indicates an unsuccessful service setup due to a missing RQES service configuration.
    @Test
    fun `Given Case 2, When setSelectedQtsp is called, Then the expected result is returned`() {
        // Arrange
        whenever(eudiRQESUi.getEudiRQESUiConfig()).thenReturn(eudiRQESUiConfig)
        whenever(eudiRQESUi.getSessionData()).thenReturn(sessionData)
        whenever(eudiRQESUiConfig.rqesServiceConfig).thenReturn(null)
        mockQTSPData(qtspData = qtspData)

        // Act
        val result = rqesController.setSelectedQtsp(qtspData)

        // Assert
        assertTrue(result is EudiRqesSetSelectedQtspPartialState.Failure)
    }

    // Case 3
    // 1. The `getSessionData` method in `eudiRQESUi` throws an exception with specific message.
    // Expected Result:
    // 1. The function should return an instance of `EudiRqesSetSelectedQtspPartialState.Failure`.
    // 2. The failure state should confirm that the exception is caught and the failure is handled appropriately.
    @Test
    fun `Given Case 3, When setSelectedQtsp is called, Then the expected result is returned`() {
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
                authorizationResult.value.toString().toUri(),
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

    // region mock data
    private fun mockQTSPData(qtspData: QtspData) {
        with(qtspData) {
            whenever(this.name).thenReturn(mockedQtspName)
            whenever(this.endpoint).thenReturn(mockedQtspEndpoint.toUri())
            whenever(this.scaUrl).thenReturn(mockedScaUrl)
        }
    }

    private fun mockRQESServiceConfig(eudiRQESUi: EudiRQESUi) {
        whenever(eudiRQESUi.getEudiRQESUiConfig()).thenReturn(eudiRQESUiConfig)
        whenever(eudiRQESUi.getSessionData()).thenReturn(sessionData)
        whenever(eudiRQESUiConfig.rqesServiceConfig).thenReturn(rqesServiceConfig)
        whenever(rqesServiceConfig.clientId).thenReturn(mockedClientId)
        whenever(rqesServiceConfig.clientSecret).thenReturn(mockedClientSecret)
        whenever(rqesServiceConfig.authFlowRedirectionURI).thenReturn(
            URI.create(mockedUri),
        )
        whenever(rqesServiceConfig.hashAlgorithm).thenReturn(HashAlgorithmOID.SHA_256)
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
