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

import android.net.Uri
import eu.europa.ec.eudi.rqesui.domain.entities.localization.LocalizableKey.Companion.ARGUMENTS_SEPARATOR
import eu.europa.ec.eudi.rqesui.util.mockedExceptionWithMessage
import eu.europa.ec.eudi.rqesui.util.mockedInvalidUri
import junit.framework.TestCase.assertEquals
import org.junit.runner.RunWith
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TestStringExtensionsKt {

    //region localizationFormatWithArgs
    @Test
    fun `When localizationFormatWithArgs is called with empty args, Then initial string is returned`() {
        // Act
        val formattedString = textForLocalization.localizationFormatWithArgs()

        // Assert
        assertEquals(textForLocalization, formattedString)
    }

    @Test
    fun `When localizationFormatWithArgs is called with valid args, Then formatted string is returned`() {
        // Arrange
        val textWithArgSeparator = "$textFollowedByArgSeparator$ARGUMENTS_SEPARATOR"
        val args = listOf(mockedStringArgument)

        // Act
        val formattedString =
            textWithArgSeparator.localizationFormatWithArgs(args, ARGUMENTS_SEPARATOR)

        // Assert
        assertEquals("$textFollowedByArgSeparator${args.first()}", formattedString)
    }
    //endregion

    //region encodeToBase64
    @Test
    fun `When encodeToBase64 is called, Then base64 encoded string is returned`() {
        // Act
        val encodedString = base64DecodedString.encodeToBase64()

        // Assert
        assertEquals(base64String, encodedString)
    }
    //endregion

    //region decodeFromBase64
    @Test
    fun `When decodeFromBase64 is called with a valid Base64 string, Then original string is returned`() {
        // Act
        val decodedString = base64String.decodeFromBase64()

        // Assert
        assertEquals(base64DecodedString, decodedString)
    }
    //endregion

    //region toUri
    @Test
    fun `When toUri is called and exception is thrown, Then EMPTY Uri is returned`() {
        // Arrange
        mockStatic(Uri::class.java).use { _ ->
            whenever(Uri.parse(mockedInvalidUri))
                .thenThrow(mockedExceptionWithMessage)

            // Act
            val result = mockedInvalidUri.toUri()

            // Assert
            assertEquals(Uri.EMPTY, result)
        }
    }
    //endregion

    //region mock data
    private val base64String = "dGVzdCBtZXNzYWdl"
    private val base64DecodedString = "test message"
    private val textFollowedByArgSeparator = "test message followed by arg separator:"
    private val mockedStringArgument = "mockedStringArgument"
    private val textForLocalization = "text for localization"
    //endregion
}