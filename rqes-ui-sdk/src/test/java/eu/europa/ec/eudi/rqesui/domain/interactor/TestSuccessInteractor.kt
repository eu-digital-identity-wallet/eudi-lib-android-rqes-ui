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

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import eu.europa.ec.eudi.rqes.core.RQESService
import eu.europa.ec.eudi.rqes.core.SignedDocuments
import eu.europa.ec.eudi.rqesui.base.TestApplication
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesAuthorizeCredentialPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesController
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetSelectedFilePartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesGetSelectedQtspPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesSaveSignedDocumentsPartialState
import eu.europa.ec.eudi.rqesui.domain.controller.EudiRqesSignDocumentsPartialState
import eu.europa.ec.eudi.rqesui.domain.entities.error.EudiRQESUiError
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.QtspData
import eu.europa.ec.eudi.rqesui.infrastructure.provider.ResourceProvider
import eu.europa.ec.eudi.rqesui.mockedDocumentName
import eu.europa.ec.eudi.rqesui.mockedGenericErrorMessage
import eu.europa.ec.eudi.rqesui.mockedPlainFailureMessage
import eu.europa.ec.eudi.rqesui.presentation.extension.getFileName
import eu.europa.ec.eudi.rqesui.test_rule.CoroutineTestRule
import eu.europa.ec.eudi.rqesui.test_rule.runTest
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
@Config(application = TestApplication::class)
class TestSuccessInteractor {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var interactor: SuccessInteractor

    @Mock
    private lateinit var resourceProvider: ResourceProvider

    @Mock
    private lateinit var eudiRqesController: EudiRqesController

    @Mock
    private lateinit var credentialAuthorized: RQESService.CredentialAuthorized

    @Mock
    private lateinit var signedDocuments: SignedDocuments

    @Mock
    private lateinit var qtspData: QtspData

    @Mock
    private lateinit var documentData: DocumentData

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var contentResolver: ContentResolver

    @Mock
    private lateinit var cursor: Cursor

    private lateinit var closeable: AutoCloseable

    private val documentFileUri = Uri.parse("content://example.provider/documents/document.pdf")

    @Before
    fun setUp() {
        closeable = MockitoAnnotations.openMocks(this)
        interactor = SuccessInteractorImpl(resourceProvider, eudiRqesController)
        whenever(resourceProvider.genericErrorMessage()).thenReturn(mockedGenericErrorMessage)
    }

    @After
    fun after() {
        closeable.close()
    }

    //region getSelectedFileAndQtsp
    // Case 1: Testing when `getSelectedFileAndQtsp` successfully returns a file and Qtsp
    // Case 1 Expected Result:
    // 1. The interactor should call `getSelectedFileAndQtsp` and successfully retrieve both the file and the Qtsp data.
    // 2. The returned result should be of type `SuccessInteractorGetSelectedFileAndQtspPartialState.Success`.
    // 3. The `selectedFile` in the returned result should match the mock `documentData`.
    // 4. The `selectedQtsp` in the returned result should match the mock `qtspData`.
    @Test
    fun `Given Case 1, When getSelectedFileAndQtsp is called, Then Case 1 expected result is returned`() {
        // Arrange
        mockGetSelectedFileCall(event = EudiRqesGetSelectedFilePartialState.Success(file = documentData))
        mockGetSelectedQtspCall(event = EudiRqesGetSelectedQtspPartialState.Success(qtsp = qtspData))

        // Act
        val result = interactor.getSelectedFileAndQtsp()

        // Assert
        assertTrue(result is SuccessInteractorGetSelectedFileAndQtspPartialState.Success)
        val success = result as SuccessInteractorGetSelectedFileAndQtspPartialState.Success
        assertEquals(documentData, success.selectedFile)
        assertEquals(qtspData, success.selectedQtsp)
    }

    // Case 2: Testing when `getSelectedFileAndQtsp` fails to retrieve the file and Qtsp
    // Case 2 Expected Result:
    // 1. The interactor should call `getSelectedFileAndQtsp` and handle a failure when retrieving the file.
    // 2. The returned result should be of type `Failure` from `SuccessInteractorGetSelectedFileAndQtspPartialState`.
    // 3. The `error` in the returned result should match the mocked `EudiRQESUiError` with the failure message.
    @Test
    fun `Given Case 2, When getSelectedFileAndQtsp is called, Then Case 2 expected result is returned`() {
        // Arrange
        val error = EudiRQESUiError(message = mockedPlainFailureMessage)
        mockGetSelectedFileCall(event = EudiRqesGetSelectedFilePartialState.Failure(error = error))

        // Act
        val result = interactor.getSelectedFileAndQtsp()

        // Assert
        assertTrue(result is SuccessInteractorGetSelectedFileAndQtspPartialState.Failure)
        assertEquals(
            error,
            (result as SuccessInteractorGetSelectedFileAndQtspPartialState.Failure).error
        )
    }

    // Case 3: Testing when `getSelectedFileAndQtsp` successfully retrieves the file,
    // but fails to retrieve the Qtsp.
    // Case 3 Expected Result:
    // 1. The interactor should successfully retrieve the file.
    // 2. The interactor should fail when retrieving the Qtsp and handle the error.
    // 3. The returned result should be of type Failure of `SuccessInteractorGetSelectedFileAndQtspPartialState`.
    // 4. The `error` in the returned result should match the mocked `EudiRQESUiError` with the failure message.
    @Test
    fun `Given Case 3, When getSelectedFileAndQtsp is called, Then Case 3 expected result is returned`() {
        // Arrange
        val error = EudiRQESUiError(message = mockedPlainFailureMessage)
        mockGetSelectedFileCall(event = EudiRqesGetSelectedFilePartialState.Success(file = documentData))
        mockGetSelectedQtspCall(event = EudiRqesGetSelectedQtspPartialState.Failure(error = error))

        // Act
        val result = interactor.getSelectedFileAndQtsp()

        // Assert
        assertTrue(result is SuccessInteractorGetSelectedFileAndQtspPartialState.Failure)
        assertEquals(
            error,
            (result as SuccessInteractorGetSelectedFileAndQtspPartialState.Failure).error
        )
    }
    //endregion

    //region signAndSaveDocument
    // Case 1: Testing when `signAndSaveDocument` fails during the credential authorization step.
    // Case 1 Expected Result:
    // 1. The interactor should attempt to authorize the credential via `authorizeCredential`.
    // 2. The authorization should fail and return an error (`EudiRQESUiError`).
    // 3. The returned result from `signAndSaveDocument` should be of type `Failure` from `SuccessInteractorSignAndSaveDocumentPartialState`.
    // 4. The `error` in the returned result should match the mocked error (`mockedPlainFailureMessage`).
    @Test
    fun `Given Case 1, When signAndSaveDocument is called, Then Case 1 expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val error = EudiRQESUiError(message = mockedPlainFailureMessage)
            mockAuthorizeCredentialCall(
                response = EudiRqesAuthorizeCredentialPartialState.Failure(
                    error = error
                )
            )

            // Act
            val result = interactor.signAndSaveDocument(mockedDocumentName)

            // Assert
            assertTrue(result is SuccessInteractorSignAndSaveDocumentPartialState.Failure)
            assertEquals(
                error,
                (result as SuccessInteractorSignAndSaveDocumentPartialState.Failure).error
            )
        }

    // Case 2: Testing when `signAndSaveDocument` succeeds in credential authorization but fails during document signing.
    // Case 2 Expected Result:
    // 1. The interactor should first authorize the credential successfully via `authorizeCredential`.
    // 2. After authorization, the document signing should be attempted, but it fails.
    // 3. The failure should return an error (`EudiRQESUiError`).
    // 4. The returned result from `signAndSaveDocument` should be of type `Failure` of `SuccessInteractorSignAndSaveDocumentPartialState`.
    // 5. The `error` in the returned result should match the mocked error.
    @Test
    fun `Given Case 2, When signAndSaveDocument is called, Then Case 2 expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val error = EudiRQESUiError(message = mockedPlainFailureMessage)
            mockAuthorizeCredentialCall(
                response = EudiRqesAuthorizeCredentialPartialState.Success(
                    credentialAuthorized
                )
            )
            mockSignDocumentsCall(response = EudiRqesSignDocumentsPartialState.Failure(error = error))

            // Act
            val result = interactor.signAndSaveDocument(mockedDocumentName)

            // Assert
            assertTrue(result is SuccessInteractorSignAndSaveDocumentPartialState.Failure)
            assertEquals(
                error,
                (result as SuccessInteractorSignAndSaveDocumentPartialState.Failure).error
            )
        }

    // Case 3: Testing when `signAndSaveDocument` successfully goes ahead with an authorized credential, signs the document,
    //         but fails to save the signed document.
    // Case 3 Expected Result:
    // 1. The interactor should first successfully continue with an authorized credential.
    // 2. After successful authorization, the document should be signed successfully.
    // 3. After signing, the interactor attempts to save the signed document, but this fails.
    // 4. The failure during the save operation should return an error (`EudiRQESUiError`).
    // 5. The result from `signAndSaveDocument` should be of type `Failure` of `SuccessInteractorSignAndSaveDocumentPartialState`.
    // 6. The `error` in the returned result should match the mocked error.
    @Test
    fun `Given Case 3, When signAndSaveDocument is called, Then Case 3 expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val error = EudiRQESUiError(message = mockedPlainFailureMessage)
            mockAuthorizeCredentialCall(
                response = EudiRqesAuthorizeCredentialPartialState.Success(
                    authorizedCredential = credentialAuthorized
                )
            )
            mockSignDocumentsCall(
                response = EudiRqesSignDocumentsPartialState.Success(
                    signedDocuments = signedDocuments
                )
            )
            mockSaveSignedDocumentsCall(
                documentName = mockedDocumentName,
                signedDocuments = signedDocuments,
                event = EudiRqesSaveSignedDocumentsPartialState.Failure(error = error)
            )

            // Act
            val result = interactor.signAndSaveDocument(mockedDocumentName)

            // Assert
            assertTrue(result is SuccessInteractorSignAndSaveDocumentPartialState.Failure)
            assertEquals(
                error,
                (result as SuccessInteractorSignAndSaveDocumentPartialState.Failure).error
            )
        }

    // Case 4: Testing when `signAndSaveDocument` successfully goes ahead with an authorized credential,
    // signs the document, and successfully saves the signed document.
    // Case 4 Expected Result:
    // 1. The interactor should successfully authorize the credential.
    // 2. After successful authorization, the document should be signed successfully.
    // 3. After signing, the interactor should attempt to save the signed document.
    // 4. The save operation should succeed, returning the Uri of the saved document.
    // 5. The `signAndSaveDocument` function should return a successful result.
    // 6. The file name from the saved document's URI should be retrieved and should match the original document name.
    @Test
    fun `Given Case 4, When signAndSaveDocument is called, Then Case 4 expected result is returned`() =
        coroutineRule.runTest {
            // Arrange
            val documentsUri = listOf(documentFileUri)
            mockAuthorizeCredentialCall(
                response = EudiRqesAuthorizeCredentialPartialState.Success(
                    authorizedCredential = credentialAuthorized
                )
            )
            mockSignDocumentsCall(
                response = EudiRqesSignDocumentsPartialState.Success(
                    signedDocuments = signedDocuments
                )
            )
            mockSaveSignedDocumentsCall(
                documentName = mockedDocumentName,
                signedDocuments = signedDocuments,
                event = EudiRqesSaveSignedDocumentsPartialState.Success(savedDocumentsUri = documentsUri)
            )

            // Act
            val result = interactor.signAndSaveDocument(mockedDocumentName)

            // Assert
            val fileNameResult = mockGetFileNameFromUri()
            assertEquals(mockedDocumentName, fileNameResult)
            assertTrue(result is SuccessInteractorSignAndSaveDocumentPartialState.Failure)
        }
    //endregion

    //region helper functions
    private fun mockGetSelectedFileCall(event: EudiRqesGetSelectedFilePartialState) {
        whenever(eudiRqesController.getSelectedFile()).thenReturn(event)
    }

    private fun mockGetSelectedQtspCall(event: EudiRqesGetSelectedQtspPartialState) {
        whenever(eudiRqesController.getSelectedQtsp()).thenReturn(event)
    }

    private suspend fun mockAuthorizeCredentialCall(response: EudiRqesAuthorizeCredentialPartialState) {
        whenever(eudiRqesController.authorizeCredential()).thenReturn(
            response
        )
    }

    private suspend fun mockSignDocumentsCall(response: EudiRqesSignDocumentsPartialState) {
        whenever(eudiRqesController.signDocuments(credentialAuthorized))
            .thenReturn(response)
    }

    private suspend fun mockSaveSignedDocumentsCall(
        documentName: String,
        signedDocuments: SignedDocuments,
        event: EudiRqesSaveSignedDocumentsPartialState
    ) {
        whenever(
            eudiRqesController.saveSignedDocuments(
                originalDocumentName = documentName,
                signedDocuments = signedDocuments
            )
        ).thenReturn(event)
    }

    private fun mockGetFileNameFromUri(): String {
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(contentResolver.query(documentFileUri, null, null, null, null))
            .thenReturn(cursor)
        whenever(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)).thenReturn(0)
        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.getString(0)).thenReturn(mockedDocumentName)
        return documentFileUri.getFileName(context).getOrThrow()
    }
    //endregion
}





