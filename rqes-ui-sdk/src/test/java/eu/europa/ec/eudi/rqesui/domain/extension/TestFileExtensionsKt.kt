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

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import eu.europa.ec.eudi.rqesui.util.mockedExceptionWithMessage
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mockStatic
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TestFileExtensionsKt {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var fileUri: Uri

    private lateinit var closeable: AutoCloseable

    private val packageName = "eu.europa.ec.eudi.testapp"
    private val expectedAuthority = "$packageName.rqes.library.provider"

    @Before
    fun setUp() {
        closeable = MockitoAnnotations.openMocks(this)
        whenever(context.packageName).thenReturn(packageName)
    }

    @After
    fun after() {
        closeable.close()
    }

    //region toShareableUri
    // Case 1
    // 1. FileProvider.getUriForFile returns a valid Uri for the given file and context.
    // Expected Result:
    // 1. The function should return a successful Result containing the same Uri.
    @Test
    fun `When toShareableUri is called and FileProvider returns a Uri, Then success result with that Uri is returned`() {
        // Arrange
        val file = File("test_file.pdf")
        mockStatic(FileProvider::class.java).use { mocked ->
            mocked.`when`<Uri> {
                FileProvider.getUriForFile(
                    eq(context),
                    eq(expectedAuthority),
                    eq(file)
                )
            }.thenReturn(fileUri)

            // Act
            val result = file.toShareableUri(context)

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(fileUri, result.getOrThrow())
        }
    }

    // Case 2
    // 1. FileProvider.getUriForFile throws an exception.
    // Expected Result:
    // 1. The function should return a failure Result wrapping the exception.
    @Test
    fun `When toShareableUri is called and FileProvider throws, Then failure result is returned`() {
        // Arrange
        val file = File("test_file.pdf")
        mockStatic(FileProvider::class.java).use { mocked ->
            mocked.`when`<Uri> {
                FileProvider.getUriForFile(
                    eq(context),
                    eq(expectedAuthority),
                    eq(file)
                )
            }.thenThrow(mockedExceptionWithMessage)

            // Act
            val result = file.toShareableUri(context)

            // Assert
            assertTrue(result.isFailure)
            assertEquals(mockedExceptionWithMessage.message, result.exceptionOrNull()?.message)
        }
    }
    //endregion
}
