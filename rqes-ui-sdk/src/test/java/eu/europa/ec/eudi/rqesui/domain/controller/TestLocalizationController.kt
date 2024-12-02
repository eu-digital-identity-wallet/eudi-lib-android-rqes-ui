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
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import kotlin.test.Test

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
        val localizableKey = LocalizableKey.Sign
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
