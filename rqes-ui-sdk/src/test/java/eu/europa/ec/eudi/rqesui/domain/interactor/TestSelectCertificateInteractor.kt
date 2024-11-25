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

package eu.europa.ec.eudi.rqesui.domain.interactor

import eu.europa.ec.eudi.rqes.core.RQESService
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesAuthorizeServicePartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetCertificatesPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetCredentialAuthorizationUrlPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.RqesController
import eu.europa.ec.eudi.rqesui.domain.entities.error.EudiRQESUiError
import eu.europa.ec.eudi.rqesui.domain.extension.toUri
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.CertificateData
import eu.europa.ec.eudi.rqesui.infrastructure.provider.ResourceProvider
import eu.europa.ec.eudi.rqesui.util.CoroutineTestRule
import eu.europa.ec.eudi.rqesui.util.mockedAuthorizationUrl
import eu.europa.ec.eudi.rqesui.util.mockedExceptionWithMessage
import eu.europa.ec.eudi.rqesui.util.mockedGenericErrorMessage
import eu.europa.ec.eudi.rqesui.util.mockedPlainFailureMessage
import eu.europa.ec.eudi.rqesui.util.runTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TestSelectCertificateInteractor {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @Mock
    private lateinit var eudiController: RqesController

    @Mock
    private lateinit var resourceProvider: ResourceProvider

    @Mock
    private lateinit var rqesServiceAuthorized: RQESService.Authorized

    @Mock
    private lateinit var certificateData: CertificateData

    private lateinit var closeable: AutoCloseable

    private lateinit var interactor: SelectCertificateInteractor

    @Before
    fun setUp() {
        closeable = MockitoAnnotations.openMocks(this)
        interactor = SelectCertificateInteractorImpl(resourceProvider, eudiController)
        whenever(resourceProvider.genericErrorMessage())
            .thenReturn(mockedGenericErrorMessage)
    }

    @After
    fun after() {
        closeable.close()
    }

    //region getSelectedFile
    // Case 1:
    // 1. Interactor's getSelectedFile is called.
    // Case 1 Expected Result:
    // 1. eudiController's getSelectedFile is called exactly once.
    @Test
    fun `Verify that getSelectedFile on the interactor calls getSelectedFile on the eudiController`() {
        // When
        interactor.getSelectedFile()

        // Then
        verify(eudiController, times(1))
            .getSelectedFile()
    }
    //endregion

    //region authorizeServiceAndFetchCertificates
    // Case 1: Testing when both service authorization and fetching certificates are successful
    // Case 1 Expected Result:
    // 1. The interactor should return a success result that contains a list of certificates.
    // 2. The returned result should be of type `SelectCertificateInteractorAuthorizeServiceAndFetchCertificatesPartialState.Success`.
    // 3. The `eudiController`'s `setAuthorizedService` method should be called once with `rqesServiceAuthorized`,
    @Test
    fun `Given Case 1, When authorizeServiceAndFetchCertificates is called, Then Case 1 expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val listOfCertificates = listOf(certificateData)
            mockAuthorizeServiceCall(
                response = EudiRqesAuthorizeServicePartialState.Success(
                    authorizedService = rqesServiceAuthorized
                )
            )
            mockGetAvailableCertificatesCall(
                response = EudiRqesGetCertificatesPartialState.Success(
                    certificates = listOfCertificates
                )
            )

            // Act
            val result = interactor.authorizeServiceAndFetchCertificates()

            // Assert
            assertEquals(
                SelectCertificateInteractorAuthorizeServiceAndFetchCertificatesPartialState.Success(
                    certificates = listOfCertificates
                ),
                result
            )
            verify(eudiController, times(1))
                .setAuthorizedService(rqesServiceAuthorized)
        }

    // Case 2: Testing when service authorization fails
    // Case 2 Expected Result:
    // 1. The interactor should return a failure result when the service authorization fails.
    // 2. The returned result should be of type `SelectCertificateInteractorAuthorizeServiceAndFetchCertificatesPartialState.Failure`.
    // 3. The failure should contain the mocked error from the service authorization failure.
    // 4. The eudiController's `authorizeService` method should be called exactly once
    @Test
    fun `Given Case 2, When authorizeServiceAndFetchCertificates is called, Then Case 2 expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val mockError = EudiRQESUiError(message = mockedPlainFailureMessage)
            mockAuthorizeServiceCall(
                response = EudiRqesAuthorizeServicePartialState.Failure(
                    error = mockError
                )
            )

            // Act
            val result = interactor.authorizeServiceAndFetchCertificates()

            // Assert
            assertEquals(
                SelectCertificateInteractorAuthorizeServiceAndFetchCertificatesPartialState.Failure(
                    error = mockError
                ),
                result
            )
            verify(eudiController, times(1))
                .authorizeService()
        }

    // Case 3: Testing when an exception is thrown during the service authorization process
    // Case 3 Expected Result:
    // 1. The interactor should return a failure result when the service authorization throws an exception.
    // 2. The returned result should be of type `SelectCertificateInteractorAuthorizeServiceAndFetchCertificatesPartialState.Failure`.
    // 3. The failure should contain the thrown exception.
    // 4. The error message in the returned failure result should match the message of the thrown exception.
    @Test
    fun `Given Case 3, When authorizeServiceAndFetchCertificates is called, Then Case 3 expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            whenever(eudiController.authorizeService())
                .thenThrow(mockedExceptionWithMessage)

            // Act
            val result = interactor.authorizeServiceAndFetchCertificates()

            // Assert
            assertTrue(result is SelectCertificateInteractorAuthorizeServiceAndFetchCertificatesPartialState.Failure)
            assertEquals(
                mockedExceptionWithMessage.message,
                (result as SelectCertificateInteractorAuthorizeServiceAndFetchCertificatesPartialState.Failure).error.message
            )
        }

    // Case 4: Testing when fetching certificates fails
    // Case 4 Expected Result:
    // 1. The interactor should return a failure result when fetching certificates fails.
    // 2. The returned result should be of type `SelectCertificateInteractorAuthorizeServiceAndFetchCertificatesPartialState.Failure`.
    // 3. The failure should contain the error from `EudiRqesGetCertificatesPartialState.Failure`.
    @Test
    fun `Given Case 4, When authorizeServiceAndFetchCertificates is called, Then Case 4 expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val mockError = EudiRQESUiError(message = mockedPlainFailureMessage)
            mockAuthorizeServiceCall(
                response = EudiRqesAuthorizeServicePartialState.Success(
                    authorizedService = rqesServiceAuthorized
                )
            )
            mockGetAvailableCertificatesCall(
                response = EudiRqesGetCertificatesPartialState.Failure(
                    error = mockError
                )
            )

            // Act
            val result = interactor.authorizeServiceAndFetchCertificates()

            // Assert
            assertEquals(
                SelectCertificateInteractorAuthorizeServiceAndFetchCertificatesPartialState.Failure(
                    error = mockError
                ),
                result
            )
        }
    //endregion

    //region getCredentialAuthorizationUrl
    // Case 1: Testing when getCredentialAuthorizationUrl successfully returns an authorization URL
    // Case 1 Expected Result:
    // 1. The interactor should call getCredentialAuthorizationUrl and return a success response containing the authorization URL.
    // 2. The returned result should be of type `EudiRqesGetCredentialAuthorizationUrlPartialState.Success`.
    // 3. The authorization Uri returned should match the mocked `mockedAuthorizationUrl` converted to Uri.
    @Test
    fun `Given Case 1, When getCredentialAuthorizationUrl is called, Then Case 1 expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val successResponse =
                EudiRqesGetCredentialAuthorizationUrlPartialState.Success(
                    authorizationUrl = mockedAuthorizationUrl.toUri()
                )

            whenever(eudiController.getAuthorizedService())
                .thenReturn(rqesServiceAuthorized)
            mockGetCredentialAuthorizationUrlCall(response = successResponse)

            // Act
            val result = interactor.getCredentialAuthorizationUrl(certificate = certificateData)

            // Assert
            assertEquals(successResponse, result)
        }

    // Case 2: Testing when getCredentialAuthorizationUrl fails due to an error
    // Case 2 Expected Result:
    // 1. The interactor should call getCredentialAuthorizationUrl and return a failure response with a `mockedPlainFailureMessage`.
    // 2. The returned result should be of type `EudiRqesGetCredentialAuthorizationUrlPartialState.Failure`.
    // 3. The error in the returned result should match the `mockFailureError` passed in the failure response.
    @Test
    fun `Given Case 2, When getCredentialAuthorizationUrl is called, Then Case 2 expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val failureError = EudiRQESUiError(
                message = mockedPlainFailureMessage
            )
            val failureResponse =
                EudiRqesGetCredentialAuthorizationUrlPartialState.Failure(error = failureError)

            whenever(eudiController.getAuthorizedService())
                .thenReturn(rqesServiceAuthorized)
            mockGetCredentialAuthorizationUrlCall(
                response = failureResponse
            )

            // Act
            val result = interactor.getCredentialAuthorizationUrl(certificate = certificateData)

            // Assert
            assertTrue(result is EudiRqesGetCredentialAuthorizationUrlPartialState.Failure)
            assertEquals(
                failureError,
                (result as EudiRqesGetCredentialAuthorizationUrlPartialState.Failure).error
            )
        }

    // Case 3: Testing when getCredentialAuthorizationUrl throws an exception
    // Case 3 Expected Result:
    // 1. The interactor should call getCredentialAuthorizationUrl and handle the exception thrown.
    // 2. The returned result should be of type `EudiRqesGetCredentialAuthorizationUrlPartialState.Failure`.
    // 3. The error message in the returned result should match the message of the thrown exception.
    @Test
    fun `Given Case 3, When getCredentialAuthorizationUrl is called, Then Case 3 expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            whenever(eudiController.getAuthorizedService())
                .thenReturn(rqesServiceAuthorized)
            whenever(
                eudiController.getCredentialAuthorizationUrl(
                    authorizedService = rqesServiceAuthorized,
                    certificateData = certificateData
                )
            ).thenThrow(mockedExceptionWithMessage)

            // Act
            val result = interactor.getCredentialAuthorizationUrl(certificate = certificateData)

            // Assert
            assertTrue(result is EudiRqesGetCredentialAuthorizationUrlPartialState.Failure)
            assertEquals(
                mockedExceptionWithMessage.message,
                (result as EudiRqesGetCredentialAuthorizationUrlPartialState.Failure).error.message
            )
        }

    // Case 4:
    // Testing when the `getCredentialAuthorizationUrl` method is called, and the `getAuthorizedService`
    // returns `null`. This simulates a case where no authorized service is available, triggering a fallback
    // to a generic error message.
    // Case 4 Expected Result:
    // 1. The interactor should return a failure response of type `EudiRqesGetCredentialAuthorizationUrlPartialState.Failure`.
    // 2. The error message in the failure response should match the `mockedGenericErrorMessage`.
    @Test
    fun `Given Case 4, When getCredentialAuthorizationUrl is called, Then Case 4 expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            whenever(eudiController.getAuthorizedService())
                .thenReturn(null)

            // Act
            val result = interactor.getCredentialAuthorizationUrl(certificate = certificateData)

            // Assert
            assertTrue(result is EudiRqesGetCredentialAuthorizationUrlPartialState.Failure)
            assertEquals(
                mockedGenericErrorMessage,
                (result as EudiRqesGetCredentialAuthorizationUrlPartialState.Failure).error.message
            )
        }

    // Case 5 Description:
    // Testing when the `getCredentialAuthorizationUrl` method is called and the `getAuthorizedService`
    // returns a valid service but the `getCredentialAuthorizationUrl` method itself returns `null`.
    // This simulates a case where the service does not provide a valid authorization URL.
    // Case 5:Expected Result:
    // 1. The interactor should return a failure response of type `EudiRqesGetCredentialAuthorizationUrlPartialState.Failure`.
    // 2. The error message in the failure response should match the `mockedGenericErrorMessage`.
    @Test
    fun `Given Case 5, When getCredentialAuthorizationUrl is called, Then Case 5 expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            whenever(eudiController.getAuthorizedService())
                .thenReturn(rqesServiceAuthorized)
            whenever(
                eudiController.getCredentialAuthorizationUrl(
                    authorizedService = rqesServiceAuthorized,
                    certificateData = certificateData
                )
            ).thenReturn(null)

            // Act
            val result = interactor.getCredentialAuthorizationUrl(certificate = certificateData)

            // Assert
            assertTrue(result is EudiRqesGetCredentialAuthorizationUrlPartialState.Failure)
            assertEquals(
                mockedGenericErrorMessage,
                (result as EudiRqesGetCredentialAuthorizationUrlPartialState.Failure).error.message
            )
        }
    //endregion getCredentialAuthorizationUrl

    //region helper functions
    private suspend fun mockAuthorizeServiceCall(response: EudiRqesAuthorizeServicePartialState) {
        whenever(eudiController.authorizeService()).thenReturn(response)
    }

    private suspend fun mockGetAvailableCertificatesCall(response: EudiRqesGetCertificatesPartialState) {
        whenever(eudiController.getAvailableCertificates(rqesServiceAuthorized)).thenReturn(response)
    }

    private suspend fun mockGetCredentialAuthorizationUrlCall(response: EudiRqesGetCredentialAuthorizationUrlPartialState) {
        whenever(
            eudiController.getCredentialAuthorizationUrl(
                authorizedService = rqesServiceAuthorized,
                certificateData = certificateData
            )
        ).thenReturn(response)
    }
    //endregion
}

