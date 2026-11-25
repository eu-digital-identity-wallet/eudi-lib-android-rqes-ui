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

package eu.europa.ec.eudi.rqesui.domain.extension

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import eu.europa.ec.eudi.rqesui.domain.entities.error.EudiRQESUiError
import eu.europa.ec.eudi.rqesui.util.mockedDocumentName
import eu.europa.ec.eudi.rqesui.util.mockedExceptionWithMessage
import eu.europa.ec.eudi.rqesui.util.mockedExceptionWithNoMessage
import eu.europa.ec.eudi.rqesui.util.mockedUri
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mockStatic
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TestUriExtensionsKt {

    @Mock
    private lateinit var fileUri: Uri

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var contentResolver: ContentResolver

    @Mock
    private lateinit var cursor: Cursor

    private lateinit var closeable: AutoCloseable

    @Before
    fun setUp() {
        closeable = MockitoAnnotations.openMocks(this)
    }

    @After
    fun after() {
        closeable.close()
    }

    //region getFileName
    @Test
    fun `Given a Uri with a file name, When getFileName is called, Then the expected file name is returned`() {
        // Given
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(
            contentResolver.query(
                fileUri, null, null, null, null
            )
        ).thenReturn(cursor)

        whenever(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)).thenReturn(0)
        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.getString(0)).thenReturn(mockedDocumentName)

        // When
        val fileNameResult = fileUri.getFileName(context).getOrThrow()

        // Then
        assertEquals(mockedDocumentName, fileNameResult)
        verify(cursor).close()
    }

    @Test
    fun `Given the file name is null, When getFileName is called, Then failure result is returned`() {
        // Given
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(
            contentResolver.query(
                fileUri, null, null, null, null
            )
        ).thenReturn(cursor)

        whenever(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)).thenReturn(0)
        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.getString(0)).thenReturn(null)

        // When
        val fileNameResult = fileUri.getFileName(context)

        // Then
        assertTrue(fileNameResult.isFailure)
        val exception = fileNameResult.exceptionOrNull()
        assertTrue(exception is EudiRQESUiError)
        verify(cursor).close()
    }

    @Test
    fun `Given contentResolver query returns a null cursor, When getFileName is called, Then failure result is returned`() {
        // Given
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(
            contentResolver.query(
                fileUri, null, null, null, null
            )
        ).thenReturn(null)

        // When
        val fileNameResult = fileUri.getFileName(context)

        // Then
        assertTrue(fileNameResult.isFailure)
        val exception = fileNameResult.exceptionOrNull()
        assertTrue(exception is EudiRQESUiError)
    }
    //endregion

    //region decode
    // Case 1
    // 1. A valid Uri is decoded successfully via Uri.decode.
    // Expected Result:
    // 1. The function should return a successful Result containing the decoded Uri.
    @Test
    fun `Given a valid Uri, When decode is called, Then success result with decoded Uri is returned`() {
        // Given
        val uri = Uri.parse(mockedUri)

        // When
        val result = uri.decode()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(mockedUri, result.getOrThrow().toString())
    }

    // Case 2
    // 1. The Uri.decode static call throws an exception with a localized message.
    // Expected Result:
    // 1. The function should return a failure Result with EudiRQESUiError containing the exception's message.
    @Test
    fun `Given Uri decode throws with message, When decode is called, Then failure with that message is returned`() {
        mockStatic(Uri::class.java).use { mocked ->
            // Given
            mocked.`when`<String> { Uri.decode(fileUri.toString()) }
                .thenThrow(mockedExceptionWithMessage)

            // When
            val result = fileUri.decode()

            // Then
            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertTrue(exception is EudiRQESUiError)
            assertEquals(mockedExceptionWithMessage.message, (exception as EudiRQESUiError).message)
            assertEquals("Uri Decoder", exception.title)
        }
    }

    // Case 3
    // 1. The Uri.decode static call throws an exception with no localized message.
    // Expected Result:
    // 1. The function should return a failure Result with EudiRQESUiError containing the fallback message.
    @Test
    fun `Given Uri decode throws without message, When decode is called, Then failure with fallback message is returned`() {
        mockStatic(Uri::class.java).use { mocked ->
            // Given
            mocked.`when`<String> { Uri.decode(fileUri.toString()) }
                .thenThrow(mockedExceptionWithNoMessage)

            // When
            val result = fileUri.decode()

            // Then
            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertTrue(exception is EudiRQESUiError)
            assertEquals("Unable to decode uri", (exception as EudiRQESUiError).message)
        }
    }
    //endregion
}