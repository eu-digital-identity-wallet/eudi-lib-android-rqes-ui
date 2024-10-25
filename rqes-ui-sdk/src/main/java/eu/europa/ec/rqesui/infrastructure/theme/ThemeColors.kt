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

package eu.europa.ec.rqesui.infrastructure.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val isInDarkMode: Boolean
    get() {
        return false
    }

class ThemeColors {
    companion object {
        private const val white: Long = 0xFFFFFFFF
        private const val black: Long = 0xFF000000

        internal const val eudiw_theme_light_textPrimaryDark: Long = 0xDE000000
        internal const val eudiw_theme_light_textSecondaryDark: Long = 0x8A000000
        internal const val eudiw_theme_light_textDisabledDark: Long = 0x61000000
        internal const val eudiw_theme_light_backgroundDefault: Long = 0xFFF5F5F5

        internal const val eudiw_theme_dark_textPrimaryDark: Long = 0xDEFFFFFF
        internal const val eudiw_theme_dark_textSecondaryDark: Long = 0x8AFFFFFF
        internal const val eudiw_theme_dark_textDisabledDark: Long = 0xFF646670
        internal const val eudiw_theme_dark_backgroundDefault: Long = 0xFF44474F

        const val eudiw_theme_light_onSurface: Long = 0xFF1D1B20
        const val eudiw_theme_dark_onSurface: Long = white

        const val theme_light_background: Long = 0xFFF7FAFF
        const val theme_dark_background: Long = black

        const val theme_light_divider: Long = 0xFFD9D9D9
        const val theme_dark_divider: Long = 0xFFD9D9D9
    }
}


val ColorScheme.textPrimaryDark: Color
    @Composable get() = if (isSystemInDarkTheme()) {
        Color(ThemeColors.eudiw_theme_dark_textPrimaryDark)
    } else {
        Color(ThemeColors.eudiw_theme_light_textPrimaryDark)
    }

val ColorScheme.textSecondaryDark: Color
    @Composable get() = if (isSystemInDarkTheme()) {
        Color(ThemeColors.eudiw_theme_dark_textSecondaryDark)
    } else {
        Color(ThemeColors.eudiw_theme_light_textSecondaryDark)
    }

val ColorScheme.textDisabledDark: Color
    @Composable get() = if (isSystemInDarkTheme()) {
        Color(ThemeColors.eudiw_theme_dark_textDisabledDark)
    } else {
        Color(ThemeColors.eudiw_theme_light_textDisabledDark)
    }


val ColorScheme.backgroundDefault: Color
    @Composable get() = if (isSystemInDarkTheme()) {
        Color(ThemeColors.eudiw_theme_dark_backgroundDefault)
    } else {
        Color(ThemeColors.eudiw_theme_light_backgroundDefault)
    }

val ColorScheme.dividerDefault: Color
    @Composable get() = if (isSystemInDarkTheme()) {
        Color(ThemeColors.theme_dark_divider)
    } else {
        Color(ThemeColors.theme_light_divider)
    }

