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
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetSelectedFilePartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetSelectedQtspPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.RqesController
import eu.europa.ec.eudi.rqesui.domain.entities.error.EudiRQESUiError
import eu.europa.ec.eudi.rqesui.domain.extension.toUriOrEmpty
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.CertificateData
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.QtspData
import eu.europa.ec.eudi.rqesui.infrastructure.provider.ResourceProvider
import eu.europa.ec.eudi.rqesui.util.CoroutineTestRule
import eu.europa.ec.eudi.rqesui.util.mockedAuthorizationUrl
import eu.europa.ec.eudi.rqesui.util.mockedExceptionWithMessage
import eu.europa.ec.eudi.rqesui.util.mockedGenericErrorMessage
import eu.europa.ec.eudi.rqesui.util.mockedGenericErrorTitle
import eu.europa.ec.eudi.rqesui.util.mockedPlainFailureMessage
import eu.europa.ec.eudi.rqesui.util.runTest
import junit.framework.TestCase.assertEquals
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

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TestOptionsSelectionInteractor {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @Mock
    private lateinit var eudiController: RqesController

    @Mock
    private lateinit var resourceProvider: ResourceProvider

    @Mock
    private lateinit var rqesService: RQESService

    @Mock
    private lateinit var rqesServiceAuthorized: RQESService.Authorized

    @Mock
    private lateinit var qtspData: QtspData

    @Mock
    private lateinit var certificateData: CertificateData

    @Mock
    private lateinit var documentData: DocumentData

    private lateinit var closeable: AutoCloseable
    private lateinit var interactor: OptionsSelectionInteractor

    @Before
    fun setUp() {
        closeable = MockitoAnnotations.openMocks(this)
        interactor = OptionsSelectionInteractorImpl(
            eudiRqesController = eudiController,
            resourceProvider = resourceProvider
        )
        whenever(resourceProvider.genericErrorMessage())
            .thenReturn(mockedGenericErrorMessage)
        whenever(resourceProvider.genericErrorTitle())
            .thenReturn(mockedGenericErrorTitle)
    }

    @After
    fun after() {
        closeable.close()
    }

    //region getQtsps
    @Test
    fun `When getQtsps is called, Then getQtsps of the eudiController is called`() {
        // When
        interactor.getQtsps()

        // Then
        verify(eudiController, times(1))
            .getQtsps()
    }
    //endregion

    //region getRemoteOrLocalFile
    @Test
    fun `When getSelectedFile is called, Then controller getSelectedFile is called`() {
        coroutineRule.runTest {
            // When
            interactor.getRemoteOrLocalFile()

            // Then
            verify(eudiController, times(1))
                .getRemoteOrLocalFile()
        }
    }
    //endregion

    //region updateQtspUserSelection
    @Test
    fun `When updateQtspUserSelection is called, Then controller setSelectedQtsp is called`() {
        // When
        interactor.updateQtspUserSelection(qtspData)

        // Then
        verify(eudiController, times(1))
            .setSelectedQtsp(qtspData)
    }
    //endregion

    //region getServiceAuthorizationUrl
    @Test
    fun `When getServiceAuthorizationUrl is called, Then controller getServiceAuthorizationUrl is called`() =
        coroutineRule.runTest {
            // When
            interactor.getServiceAuthorizationUrl(rqesService)

            // Then
            verify(eudiController, times(1))
                .getServiceAuthorizationUrl(rqesService)
        }
    //endregion

    //region authorizeServiceAndFetchCertificates
    @Test
    fun `Given service authorization and certificate fetch succeed, When authorizeServiceAndFetchCertificates is called, Then return Success`() =
        coroutineRule.runTest {
            // Arrange
            val certificateDataList = listOf(certificateData)
            mockAuthorizeServiceCall(
                EudiRqesAuthorizeServicePartialState.Success(authorizedService = rqesServiceAuthorized)
            )
            mockGetAvailableCertificatesCall(
                EudiRqesGetCertificatesPartialState.Success(certificates = certificateDataList)
            )

            // Act
            val result = interactor.authorizeServiceAndFetchCertificates()

            // Assert
            assertTrue(result is OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState.Success)
            assertEquals(
                certificateDataList,
                (result as OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState.Success).certificates
            )
            verify(eudiController).setAuthorizedService(rqesServiceAuthorized)
        }

    @Test
    fun `Given that service authorization fails, When authorizeServiceAndFetchCertificates is called, Then return Failure`() =
        coroutineRule.runTest {
            // Arrange
            val error = EudiRQESUiError(
                title = mockedGenericErrorTitle,
                message = mockedPlainFailureMessage
            )
            mockAuthorizeServiceCall(
                EudiRqesAuthorizeServicePartialState.Failure(error)
            )

            // Act
            val result = interactor.authorizeServiceAndFetchCertificates()

            // Assert
            assertTrue(result is OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState.Failure)
            assertEquals(
                error,
                (result as OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState.Failure).error
            )
        }

    @Test
    fun `Given service authorization fails with exception thrown, When authorizeServiceAndFetchCertificates is called, Then Case 3 expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            whenever(eudiController.authorizeService())
                .thenThrow(mockedExceptionWithMessage)

            // Act
            val result = interactor.authorizeServiceAndFetchCertificates()

            // Assert
            assertTrue(result is OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState.Failure)
            assertEquals(
                mockedExceptionWithMessage.message,
                (result as OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState.Failure).error.message
            )
        }

    @Test
    fun `Given service authorization succeeds but certificate fetch fails, When authorizeServiceAndFetchCertificates is called, Then return Failure`() =
        coroutineRule.runTest {
            // Arrange
            val error = EudiRQESUiError(
                title = mockedGenericErrorTitle,
                message = mockedPlainFailureMessage
            )
            mockAuthorizeServiceCall(
                EudiRqesAuthorizeServicePartialState.Success(authorizedService = rqesServiceAuthorized)
            )
            mockGetAvailableCertificatesCall(
                EudiRqesGetCertificatesPartialState.Failure(error = error)
            )

            // Act
            val result = interactor.authorizeServiceAndFetchCertificates()

            // Assert
            assertTrue(result is OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState.Failure)
            assertEquals(
                error,
                (result as OptionsSelectionInteractorAuthorizeServiceAndFetchCertificatesPartialState.Failure).error
            )
        }
    //endregion

    //region getSelectedQtsp
    @Test
    fun `Given both file and qtsp are selected, When getSelectedFileAndQtsp is called, Then return Success`() {
        // Arrange
        whenever(eudiController.getSelectedFile())
            .thenReturn(EudiRqesGetSelectedFilePartialState.Success(file = documentData))
        whenever(eudiController.getSelectedQtsp())
            .thenReturn(EudiRqesGetSelectedQtspPartialState.Success(qtsp = qtspData))

        // Act
        val result = interactor.getSelectedQtsp()

        // Assert
        assertTrue(result is OptionsSelectionInteractorGetSelectedQtspPartialState.Success)
        with(result as OptionsSelectionInteractorGetSelectedQtspPartialState.Success) {
            assertEquals(qtspData, selectedQtsp)
        }
    }

    @Test
    fun `Given selected file retrieval fails, When getSelectedFileAndQtsp is called, Then return Failure`() {
        // Arrange
        val error =
            EudiRQESUiError(title = mockedGenericErrorTitle, message = mockedGenericErrorMessage)
        whenever(eudiController.getSelectedFile())
            .thenReturn(EudiRqesGetSelectedFilePartialState.Failure(error))

        // Act
        val result = interactor.getSelectedQtsp()

        // Assert
        assertTrue(result is OptionsSelectionInteractorGetSelectedQtspPartialState.Failure)
        assertEquals(
            error.message,
            (result as OptionsSelectionInteractorGetSelectedQtspPartialState.Failure).error.message
        )
    }

    @Test
    fun `Given selected QTSP retrieval fails, When getSelectedFileAndQtsp is called, Then return Failure`() {
        // Arrange
        whenever(eudiController.getSelectedFile())
            .thenReturn(EudiRqesGetSelectedFilePartialState.Success(file = documentData))
        val failureError =
            EudiRQESUiError(title = mockedGenericErrorTitle, message = mockedPlainFailureMessage)
        whenever(eudiController.getSelectedQtsp())
            .thenReturn(EudiRqesGetSelectedQtspPartialState.Failure(error = failureError))

        // Act
        val result = interactor.getSelectedQtsp()

        // Assert
        assertTrue(result is OptionsSelectionInteractorGetSelectedQtspPartialState.Failure)
        assertEquals(
            failureError,
            (result as OptionsSelectionInteractorGetSelectedQtspPartialState.Failure).error
        )
    }
    //endregion

    //region getCredentialAuthorizationUrl
    @Test
    fun `Given authorized service exists, When getCredentialAuthorizationUrl called, Then return Success`() =
        coroutineRule.runTest {
            // Arrange
            val successResponse = EudiRqesGetCredentialAuthorizationUrlPartialState.Success(
                authorizationUrl = mockedAuthorizationUrl.toUriOrEmpty()
            )
            whenever(eudiController.getAuthorizedService()).thenReturn(rqesServiceAuthorized)
            mockGetCredentialAuthorizationUrlCall(successResponse)

            // Act
            val result = interactor.getCredentialAuthorizationUrl(certificateData)

            // Assert
            assertEquals(successResponse, result)
        }

    @Test
    fun `Given getAuthorizedService returns null, When getCredentialAuthorizationUrl called, Then return Failure with generic message`() =
        coroutineRule.runTest {
            // Arrange
            whenever(eudiController.getAuthorizedService())
                .thenReturn(null)
            whenever(resourceProvider.genericErrorMessage())
                .thenReturn(mockedGenericErrorMessage)

            // Act
            val result = interactor.getCredentialAuthorizationUrl(certificateData)

            // Assert
            assertTrue(result is EudiRqesGetCredentialAuthorizationUrlPartialState.Failure)
            assertEquals(
                mockedGenericErrorMessage,
                (result as EudiRqesGetCredentialAuthorizationUrlPartialState.Failure).error.message
            )
        }

    @Test
    fun `Given getCredentialAuthorizationUrl returns Failure, When getCredentialAuthorizationUrl is called, Then Failure state is returned`() =
        coroutineRule.runTest {
            // Arrange
            val expectedError = EudiRQESUiError(
                title = mockedGenericErrorTitle,
                message = mockedPlainFailureMessage
            )
            val failureResponse = EudiRqesGetCredentialAuthorizationUrlPartialState.Failure(
                error = expectedError
            )

            whenever(eudiController.getAuthorizedService())
                .thenReturn(rqesServiceAuthorized)
            whenever(
                eudiController.getCredentialAuthorizationUrl(
                    authorizedService = rqesServiceAuthorized,
                    certificateData = certificateData
                )
            ).thenReturn(failureResponse)

            // Act
            val result = interactor.getCredentialAuthorizationUrl(certificateData)

            // Assert
            assertTrue(result is EudiRqesGetCredentialAuthorizationUrlPartialState.Failure)
            assertEquals(
                expectedError,
                (result as EudiRqesGetCredentialAuthorizationUrlPartialState.Failure).error
            )
        }
    //endregion

    //region Helper Functions
    private suspend fun mockAuthorizeServiceCall(response: EudiRqesAuthorizeServicePartialState) {
        whenever(eudiController.authorizeService()).thenReturn(response)
    }

    private suspend fun mockGetAvailableCertificatesCall(response: EudiRqesGetCertificatesPartialState) {
        whenever(eudiController.getAvailableCertificates(rqesServiceAuthorized))
            .thenReturn(response)
    }

    private suspend fun mockGetCredentialAuthorizationUrlCall(
        response: EudiRqesGetCredentialAuthorizationUrlPartialState
    ) {
        whenever(
            eudiController.getCredentialAuthorizationUrl(
                authorizedService = rqesServiceAuthorized,
                certificateData = certificateData
            )
        ).thenReturn(response)
    }
    //endregion
}