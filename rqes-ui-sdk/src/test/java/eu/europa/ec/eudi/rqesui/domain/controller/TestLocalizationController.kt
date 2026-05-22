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

package eu.europa.ec.eudi.rqesui.domain.controller

import eu.europa.ec.eudi.rqesui.domain.entities.error.EudiRQESUiError
import eu.europa.ec.eudi.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.eudi.rqesui.infrastructure.config.EudiRQESUiConfig
import eu.europa.ec.eudi.rqesui.util.mockedLocalizedText
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class TestLocalizationController {
    @Mock
    private lateinit var config: EudiRQESUiConfig

    private lateinit var controller: LocalizationController

    private lateinit var closeable: AutoCloseable

    @Before
    fun setUp() {
        closeable = MockitoAnnotations.openMocks(this)
        controller = LocalizationControllerImpl(config = testConfiguration())
    }

    @After
    fun after() {
        closeable.close()
    }

    //region get()
    // Case 1
    // Function get() of the controller is called with a localizable key as argument.
    // Case 1 Expected Result:
    // 1. The defaultTranslation string is returned
    @Test
    fun `Given Case 1, When get() is called, Then Case 1 expected result is returned`() {
        // Arrange
        val localizableKey = LocalizableKey.SignDocument

        // Act
        val result = controller.get(localizableKey)

        // Assert
        assertEquals(localizableKey.defaultTranslation(), result)
    }

    // Case 2
    // When get() is called with a specific localizable key, the controller should return
    // the corresponding localized text.
    // Case 2 Expected Result:
    // 1. The mocked localized text for the given key should be returned.
    // 2. The test checks that the controller correctly retrieves the translation from the configuration.
    @Test
    fun `Given Case 2, When get() is called, Then Case 2 expected result is returned`() {
        // Arrange
        val localizableKey = LocalizableKey.SignDocument
        val config =
            testConfiguration(
                translations =
                    mapOf("en" to mapOf(localizableKey to mockedLocalizedText)),
            )
        val controller = LocalizationControllerImpl(config)

        // Act
        val result = controller.get(localizableKey)

        // Assert
        assertEquals(mockedLocalizedText, result)
    }

    // Case 3
    // When get() is called with a key that does not exist in the translations configuration,
    // it should throw an `EudiRQESUiError`.
    // Case 3 Expected Result:
    // 1. An exception of type `EudiRQESUiError` should be thrown when attempting to retrieve a missing translation.
    @Test
    fun `Given Case 3, When get() is called, Then Case 3 expected result is returned`() {
        // Arrange
        val config = testConfiguration(translations = emptyMap())
        val controller = LocalizationControllerImpl(config)

        // Act
        val exception =
            assertThrows(EudiRQESUiError::class.java) {
                controller.get(LocalizableKey.entries.first())
                // or when attempting to get any non-existent Localizable key
            }

        // Assert
        assertTrue(exception is EudiRQESUiError)
    }

    // Case 4
    // When get() is called with translations configured for a language different from the device locale,
    // the controller should fall back to the default translation of the key.
    // Case 4 Expected Result:
    // 1. The default translation of the key is returned (fallback path via elvis operator).
    @Test
    fun `Given Case 4, When get() is called, Then default translation is returned`() {
        // Arrange: the test environment locale is "en"; we configure only "fr" translations,
        // so config.translations[language] (i.e. config.translations["en"]) returns null,
        // triggering the elvis fallback to defaultTranslation().
        val localizableKey = LocalizableKey.SignDocument
        val config = testConfiguration(
            translations = mapOf("fr" to mapOf(localizableKey to mockedLocalizedText))
        )
        val controller = LocalizationControllerImpl(config)

        // Act
        val result = controller.get(localizableKey)

        // Assert
        assertEquals(localizableKey.defaultTranslation(), result)
    }

    // Case 5
    // When get() is called with a language that exists in translations but the specific key is missing,
    // the controller should fall back to the default translation.
    // Case 5 Expected Result:
    // 1. The default translation of the missing key is returned (fallback path via elvis operator
    //    on the inner ?.get(key) call).
    @Test
    fun `Given Case 5, When get() is called for a missing key, Then default translation is returned`() {
        // Arrange: the test environment locale is "en"; the "en" map exists but does not include SignDocument.
        val configuredKey = LocalizableKey.Cancel
        val requestedKey = LocalizableKey.SignDocument
        val config = testConfiguration(
            translations = mapOf("en" to mapOf(configuredKey to mockedLocalizedText))
        )
        val controller = LocalizationControllerImpl(config)

        // Act
        val result = controller.get(requestedKey)

        // Assert
        assertEquals(requestedKey.defaultTranslation(), result)
    }
    //endregion

    //region configuration for test
    private fun testConfiguration(
        translations: Map<String, Map<LocalizableKey, String>> =
            mapOf(
                "en" to LocalizableKey.entries.associateWith { it.defaultTranslation() },
            ),
    ): EudiRQESUiConfig =
        config.apply {
            whenever(this.translations).thenReturn(translations)
        }
    //endregion
}
