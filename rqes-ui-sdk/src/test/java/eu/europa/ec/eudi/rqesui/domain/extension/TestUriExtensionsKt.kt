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
import kotlin.test.Test

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