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

package eu.europa.ec.eudi.rqesui.domain.serializer.kserializer

import android.net.Uri
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Robolectric is required because [Uri.parse] needs the Android runtime.
 */
@RunWith(RobolectricTestRunner::class)
class TestUriSerializer {

    private val json = Json

    @Test
    fun `Given an https URI, When it is encoded and decoded, Then it roundtrips losslessly`() {
        // Given
        val uri = Uri.parse("https://example.com/path?query=value#frag")

        // When
        val encoded = json.encodeToString(UriSerializer, uri)
        val decoded = json.decodeFromString(UriSerializer, encoded)

        // Then
        assertEquals(uri, decoded)
    }

    @Test
    fun `Given a content URI, When it is encoded and decoded, Then it roundtrips losslessly`() {
        // Given
        val uri = Uri.parse("content://com.example.provider/documents/42")

        // When
        val encoded = json.encodeToString(UriSerializer, uri)
        val decoded = json.decodeFromString(UriSerializer, encoded)

        // Then
        assertEquals(uri, decoded)
    }

    @Test
    fun `Given a file URI, When it is encoded and decoded, Then it roundtrips losslessly`() {
        // Given
        val uri = Uri.parse("file:///data/local/tmp/document.pdf")

        // When
        val encoded = json.encodeToString(UriSerializer, uri)
        val decoded = json.decodeFromString(UriSerializer, encoded)

        // Then
        assertEquals(uri, decoded)
    }

    @Test
    fun `Given a URI, When it is encoded, Then the serialized form is a JSON string`() {
        // Given
        val uri = Uri.parse("https://example.com/")

        // When
        val encoded = json.encodeToString(UriSerializer, uri)

        // Then
        assertEquals("\"https://example.com/\"", encoded)
    }
}
