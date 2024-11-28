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

import eu.europa.ec.eudi.rqesui.util.getMockedContext
import eu.europa.ec.eudi.rqesui.util.mockedDefaultPreferenceValue
import eu.europa.ec.eudi.rqesui.util.mockedPreferenceStringValue
import eu.europa.ec.eudi.rqesui.util.mockedPreferencesKey
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TestPreferencesController {
    private lateinit var preferencesController: PreferencesControllerImpl

    private lateinit var closeable: AutoCloseable

    @Before
    fun setUp() {
        closeable = MockitoAnnotations.openMocks(this)
        preferencesController = PreferencesControllerImpl(context = getMockedContext())
    }

    @After
    fun after() {
        closeable.close()
    }

    // region getBool
    // Case 1: Test setting and getting a boolean value
    // Expected Result:
    // 1. The boolean value should be set correctly and the correct value should be retrieved.
    @Test
    fun `Given Case 1, When setBool and getBool are called, Then the correct boolean value is returned`() {
        // Arrange
        val preferenceValue = true
        preferencesController.setBool(mockedPreferencesKey, preferenceValue)

        // Act
        val result = preferencesController.getBool(mockedPreferencesKey, false)

        // Assert
        assertEquals(preferenceValue, result)
    }
    //endregion

    //region getString
    // Case 1: Test setting and getting a string value
    // Expected Result:
    // 1. The string value should be set correctly, and the correct value should be retrieved.
    @Test
    fun `Given Case 1, When setString and getString are called, Then the correct string value is returned`() {
        // Arrange
        preferencesController.setString(mockedPreferencesKey, mockedPreferenceStringValue)

        // Act
        val result = preferencesController.getString(mockedPreferencesKey, "")

        // Assert
        assertEquals(mockedPreferenceStringValue, result)
    }

    // Case 2: Test getting a string with a default value
    // Expected Result:
    // 1. If the key does not exist, the default value should be returned.
    @Test
    fun `Given Case 2, When getString with default value is called, Then the default value is returned`() {
        // Act
        val result = preferencesController.getString(mockedPreferencesKey, mockedDefaultPreferenceValue)

        // Assert
        assertEquals(mockedDefaultPreferenceValue, result)
    }

    //region contains
    // Case 1: Test specific key being contained
    // Expected Result:
    // 1. After calling setString for a given key and value, `contains` should return true.
    @Test
    fun `Given Case 1, When contains is called, Then false should be returned`() {
        // Arrange
        preferencesController.setString(mockedPreferencesKey, mockedPreferenceStringValue)

        // Act
        val result = preferencesController.contains(mockedPreferencesKey)

        // Assert
        assertTrue(result)
    }
    //endregion

    //region clear
    // Case 1: Test clearing all preferences
    // Expected Result:
    // 1. All preferences should be cleared, and subsequent attempts to access any key should return false.
    @Test
    fun `Given Case 1, When clear() is called, Then all preferences are cleared`() {
        // Arrange
        preferencesController.setString(mockedPreferencesKey, mockedPreferenceStringValue)

        // Act
        preferencesController.clear()

        // Assert
        assertFalse(preferencesController.contains(mockedPreferencesKey))
    }

    // Case 2: Test removing a specific key
    // Expected Result:
    // 1. The specified key should be removed, and subsequent attempts to access it should return false.
    @Test
    fun `Given Case 2, When remove specific key is called, Then the key is removed and not found`() {
        // Arrange
        preferencesController.setString(mockedPreferencesKey, mockedPreferenceStringValue)

        // Act
        preferencesController.clear(mockedPreferencesKey)

        // Assert
        val result = preferencesController.contains(mockedPreferencesKey)
        assertFalse(result)
    }
    //endregion

    //region getInt
    // Case 1
    // Expected Result:
    // 1. The integer value should be set correctly, and the correct value should be retrieved.
    @Test
    fun `Given Case 1, When setInt and getInt are called, Then the correct integer value is returned`() {
        // Arrange
        val preferenceValue = 100
        preferencesController.setInt(mockedPreferencesKey, preferenceValue)

        // Act
        val result = preferencesController.getInt(mockedPreferencesKey, 0)

        // Assert
        assertEquals(preferenceValue, result)
    }
    //endregion

    //region getLong
    // Case 1: Test setting and getting a long value
    // Expected Result:
    // 1. The long value should be set correctly, and the correct value should be retrieved.
    @Test
    fun `Given Case 1, When setLong and getLong are called, Then the correct long value is returned`() {
        // Arrange
        val preferenceLongValue = 100L
        preferencesController.setLong(mockedPreferencesKey, preferenceLongValue)

        // Act
        val result = preferencesController.getLong(mockedPreferencesKey, 0)

        // Assert
        assertEquals(preferenceLongValue, result)
    }
    //endregion
}
