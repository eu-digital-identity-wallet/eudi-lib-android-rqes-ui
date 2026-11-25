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

package eu.europa.ec.eudi.rqesui.domain.serializer

import android.net.Uri
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.DocumentData
import eu.europa.ec.eudi.rqesui.presentation.entities.config.OptionsSelectionScreenState
import eu.europa.ec.eudi.rqesui.presentation.entities.config.OptionsSelectionUiConfig
import eu.europa.ec.eudi.rqesui.presentation.entities.config.ViewDocumentUiConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Round-trip coverage for [UiSerializerImpl]. Robolectric is required because the
 * impl Base64-encodes via `android.util.Base64`, which is unavailable on a plain JVM.
 */
@RunWith(RobolectricTestRunner::class)
class TestUiSerializer {

    private val serializer: UiSerializer = UiSerializerImpl()

    /**
     * A [UiSerializable] that is deliberately NOT `@Serializable` — used to exercise
     * the runCatching failure path in [UiSerializerImpl.toBase64] (kotlinx's
     * `serializer(Class<*>)` lookup throws for non-Serializable classes).
     */
    private class NonSerializableConfig : UiSerializable {
        companion object Parser : UiSerializableParser {
            override val serializedKeyName = "nonSerializableConfig"
        }
    }

    @Test
    fun `Given a QtspSelection config, When it is encoded and decoded, Then it roundtrips`() {
        // Given
        val original = OptionsSelectionUiConfig(
            optionsSelectionScreenState = OptionsSelectionScreenState.QtspSelection,
        )

        // When
        val encoded = serializer.toBase64(original, OptionsSelectionUiConfig.Parser)

        // Then
        assertNotNull(encoded)

        // When
        val decoded = serializer.fromBase64(
            encoded,
            OptionsSelectionUiConfig::class.java,
            OptionsSelectionUiConfig.Parser,
        )

        // Then
        assertEquals(original, decoded)
    }

    @Test
    fun `Given a CertificateSelection config, When it is encoded and decoded, Then it roundtrips`() {
        // Given
        val original = OptionsSelectionUiConfig(
            optionsSelectionScreenState = OptionsSelectionScreenState.CertificateSelection,
        )

        // When
        val encoded = serializer.toBase64(original, OptionsSelectionUiConfig.Parser)
        val decoded = serializer.fromBase64(
            encoded,
            OptionsSelectionUiConfig::class.java,
            OptionsSelectionUiConfig.Parser,
        )

        // Then
        assertEquals(original, decoded)
    }

    @Test
    fun `Given a ViewDocumentUiConfig with a Uri, When it is encoded and decoded, Then it roundtrips and preserves the Uri`() {
        // Given
        val uri = Uri.parse("content://com.example.docs/1")
        val original = ViewDocumentUiConfig(
            isSigned = true,
            documentData = DocumentData(documentName = "contract.pdf", uri = uri),
        )

        // When
        val encoded = serializer.toBase64(original, ViewDocumentUiConfig.Parser)

        // Then
        assertNotNull(encoded)

        // When
        val decoded = serializer.fromBase64(
            encoded,
            ViewDocumentUiConfig::class.java,
            ViewDocumentUiConfig.Parser,
        )

        // Then
        assertEquals(original, decoded)
        assertEquals(uri, decoded?.documentData?.uri)
    }

    @Test
    fun `Given a null payload, When fromBase64 is called, Then null is returned`() {
        // When
        val decoded = serializer.fromBase64(
            null,
            OptionsSelectionUiConfig::class.java,
            OptionsSelectionUiConfig.Parser,
        )

        // Then
        assertNull(decoded)
    }

    @Test
    fun `Given malformed base64, When fromBase64 is called, Then null is returned`() {
        // When
        val decoded = serializer.fromBase64(
            "!!not%base64\$\$",
            OptionsSelectionUiConfig::class.java,
            OptionsSelectionUiConfig.Parser,
        )

        // Then
        assertNull(decoded)
    }

    /**
     * The payload `Z2FyYmFnZQ==` is "garbage" base64-encoded.
     */
    @Test
    fun `Given valid base64 with invalid JSON, When fromBase64 is called, Then null is returned`() {
        // When
        val decoded = serializer.fromBase64(
            "Z2FyYmFnZQ==",
            OptionsSelectionUiConfig::class.java,
            OptionsSelectionUiConfig.Parser,
        )

        // Then
        assertNull(decoded)
    }

    /**
     * `serializer(Class<*>)` throws SerializationException for non-Serializable classes;
     * UiSerializerImpl wraps it in runCatching and surfaces null.
     */
    @Test
    fun `Given a UiSerializable that is not Serializable, When toBase64 is called, Then null is returned`() {
        // When
        val encoded = serializer.toBase64(
            NonSerializableConfig(),
            NonSerializableConfig.Parser,
        )

        // Then
        assertNull(encoded)
    }
}
