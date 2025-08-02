/*
 * Copyright (c) 2025 European Commission
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
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.junit.Test

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
    fun `When getFileName is called, Then assert the expected file name is returned`() {
        // Arrange
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(
            contentResolver.query(
                fileUri, null, null, null, null
            )
        ).thenReturn(cursor)

        whenever(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)).thenReturn(0)
        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.getString(0)).thenReturn(mockedDocumentName)

        // Act
        val fileNameResult = fileUri.getFileName(context).getOrThrow()

        // Assert
        assertEquals(mockedDocumentName, fileNameResult)
        verify(cursor).close()
    }

    @Test
    fun `When getFileName is called and file name is null, Then assert failure result is returned`() {
        // Arrange
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(
            contentResolver.query(
                fileUri, null, null, null, null
            )
        ).thenReturn(cursor)

        whenever(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)).thenReturn(0)
        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.getString(0)).thenReturn(null)

        // Act
        val fileNameResult = fileUri.getFileName(context)

        // Assert
        assertTrue(fileNameResult.isFailure)
        val exception = fileNameResult.exceptionOrNull()
        assertTrue(exception is EudiRQESUiError)
        verify(cursor).close()
    }
    //endregion
}