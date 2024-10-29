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

package eu.europa.ec.rqesui.infrastructure.theme.values

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import eu.europa.ec.rqesui.infrastructure.theme.ThemeManager
import eu.europa.ec.rqesui.infrastructure.theme.templates.ThemeColorsTemplate

private val isInDarkMode: Boolean
    get() {
        return ThemeManager.instance.set.isInDarkMode
    }

internal class ThemeColors {
    companion object {
        private const val white: Long = 0xFFFFFFFF
        private const val black: Long = 0xFF000000

        private const val EudiRQESUi_theme_light_primary: Long = 0xFF2A5FD9
        private const val EudiRQESUi_theme_light_secondary: Long = 0xFFD6D9F9
        private const val EudiRQESUi_theme_light_error: Long = 0xFFDA2C27
        const val EudiRQESUi_theme_light_background: Long = white
        private const val EudiRQESUi_theme_light_onSurface: Long = 0xFF1D1B20
        private const val EudiRQESUi_theme_light_onSurfaceVariant: Long = 0xFF49454F
        internal const val EudiRQESUi_theme_light_success: Long = 0xFF55953B
        internal const val EudiRQESUi_theme_light_warning: Long = 0xFFF39626
        internal const val EudiRQESUi_theme_light_textPrimaryDark: Long = black
        internal const val EudiRQESUi_theme_light_primaryVariant: Long = 0x0F2A5ED9
        internal const val EudiRQESUi_theme_light_successVariant: Long = 0xFFE4EEE7
        internal const val EudiRQESUi_theme_light_devider: Long = 0xFFD9D9D9

        private const val EudiRQESUi_theme_dark_primary: Long = 0xFFB4C5FF
        private const val EudiRQESUi_theme_dark_secondary: Long = white
        private const val EudiRQESUi_theme_dark_error: Long = 0xFFFFB4AA
        const val EudiRQESUi_theme_dark_background: Long = black
        private const val EudiRQESUi_theme_dark_onSurface: Long = 0xFFCAC5CC
        private const val EudiRQESUi_theme_dark_onSurfaceVariant: Long = 0xFFCBC4D0
        internal const val EudiRQESUi_theme_dark_success: Long = 0xFF93D875
        internal const val EudiRQESUi_theme_dark_warning: Long = 0xFFFFC288
        internal const val EudiRQESUi_theme_dark_textPrimaryDark: Long = 0xFFC6C6C6
        internal const val EudiRQESUi_theme_dark_primaryVariant: Long = 0x0FB4C5FF
        internal const val EudiRQESUi_theme_dark_successVariant: Long = 0xFF57615B
        internal const val EudiRQESUi_theme_dark_devider: Long = 0xFFD9D9D9

        internal const val EudiRQESUi_theme_light_onSuccess: Long = white
        internal const val EudiRQESUi_theme_dark_onSuccess: Long = 0xFF0E3900

        private const val EudiRQESUi_theme_light_onPrimary: Long = white
        private const val EudiRQESUi_theme_light_primaryContainer: Long = 0xFF2A5FD9
        private const val EudiRQESUi_theme_light_onPrimaryContainer: Long = white
        private const val EudiRQESUi_theme_light_onSecondary: Long = white
        private const val EudiRQESUi_theme_light_secondaryContainer: Long = 0xFFDBDEFE
        private const val EudiRQESUi_theme_light_onSecondaryContainer: Long = 0xFF41455F
        private const val EudiRQESUi_theme_light_tertiary: Long = EudiRQESUi_theme_light_secondary
        private const val EudiRQESUi_theme_light_onTertiary: Long =
            EudiRQESUi_theme_light_onSecondary
        private const val EudiRQESUi_theme_light_tertiaryContainer: Long =
            EudiRQESUi_theme_light_secondaryContainer
        private const val EudiRQESUi_theme_light_onTertiaryContainer: Long =
            EudiRQESUi_theme_light_onSecondaryContainer
        private const val EudiRQESUi_theme_light_errorContainer: Long = 0xFFDA2C27
        private const val EudiRQESUi_theme_light_onError: Long = white
        private const val EudiRQESUi_theme_light_onErrorContainer: Long = white
        private const val EudiRQESUi_theme_light_onBackground: Long = black
        private const val EudiRQESUi_theme_light_surface: Long = white
        private const val EudiRQESUi_theme_light_surfaceVariant: Long = 0xFFDFE1F3
        private const val EudiRQESUi_theme_light_outline: Long = 0xFF737685
        private const val EudiRQESUi_theme_light_inverseOnSurface: Long = 0xFFF4F0EF
        private const val EudiRQESUi_theme_light_inverseSurface: Long = 0xFF313030
        private const val EudiRQESUi_theme_light_inversePrimary: Long = 0xFFB4C5FF
        private const val EudiRQESUi_theme_light_surfaceTint: Long = EudiRQESUi_theme_light_surface
        private const val EudiRQESUi_theme_light_outlineVariant: Long = 0xFFC3C6D6
        private const val EudiRQESUi_theme_light_scrim: Long = black

        private const val EudiRQESUi_theme_dark_onPrimary: Long = 0xFF002A77
        private const val EudiRQESUi_theme_dark_primaryContainer: Long = 0xFF1A55CF
        private const val EudiRQESUi_theme_dark_onPrimaryContainer: Long = white
        private const val EudiRQESUi_theme_dark_onSecondary: Long = 0xFF2B2F47
        private const val EudiRQESUi_theme_dark_secondaryContainer: Long = 0xFFCFD2F2
        private const val EudiRQESUi_theme_dark_onSecondaryContainer: Long = 0xFF3A3E57
        private const val EudiRQESUi_theme_dark_tertiary: Long = EudiRQESUi_theme_dark_secondary
        private const val EudiRQESUi_theme_dark_onTertiary: Long = EudiRQESUi_theme_dark_onSecondary
        private const val EudiRQESUi_theme_dark_tertiaryContainer: Long =
            EudiRQESUi_theme_dark_secondaryContainer
        private const val EudiRQESUi_theme_dark_onTertiaryContainer: Long =
            EudiRQESUi_theme_dark_onSecondaryContainer
        private const val EudiRQESUi_theme_dark_errorContainer: Long = 0xFFCD2220
        private const val EudiRQESUi_theme_dark_onError: Long = 0xFF690004
        private const val EudiRQESUi_theme_dark_onErrorContainer: Long = white
        private const val EudiRQESUi_theme_dark_onBackground: Long = white
        private const val EudiRQESUi_theme_dark_surface: Long = black
        private const val EudiRQESUi_theme_dark_surfaceVariant: Long = 0xFF434654
        private const val EudiRQESUi_theme_dark_outline: Long = 0xFF8D909F
        private const val EudiRQESUi_theme_dark_inverseOnSurface: Long = 0xFF313030
        private const val EudiRQESUi_theme_dark_inverseSurface: Long = 0xFFE5E2E1
        private const val EudiRQESUi_theme_dark_inversePrimary: Long = 0xFF1B55CF
        private const val EudiRQESUi_theme_dark_surfaceTint: Long = EudiRQESUi_theme_dark_surface
        private const val EudiRQESUi_theme_dark_outlineVariant: Long = 0xFF434654
        private const val EudiRQESUi_theme_dark_scrim: Long = white

        val lightColors = ThemeColorsTemplate(
            primary = EudiRQESUi_theme_light_primary,
            onPrimary = EudiRQESUi_theme_light_onPrimary,
            primaryContainer = EudiRQESUi_theme_light_primaryContainer,
            onPrimaryContainer = EudiRQESUi_theme_light_onPrimaryContainer,
            secondary = EudiRQESUi_theme_light_secondary,
            onSecondary = EudiRQESUi_theme_light_onSecondary,
            secondaryContainer = EudiRQESUi_theme_light_secondaryContainer,
            onSecondaryContainer = EudiRQESUi_theme_light_onSecondaryContainer,
            tertiary = EudiRQESUi_theme_light_tertiary,
            onTertiary = EudiRQESUi_theme_light_onTertiary,
            tertiaryContainer = EudiRQESUi_theme_light_tertiaryContainer,
            onTertiaryContainer = EudiRQESUi_theme_light_onTertiaryContainer,
            error = EudiRQESUi_theme_light_error,
            errorContainer = EudiRQESUi_theme_light_errorContainer,
            onError = EudiRQESUi_theme_light_onError,
            onErrorContainer = EudiRQESUi_theme_light_onErrorContainer,
            background = EudiRQESUi_theme_light_background,
            onBackground = EudiRQESUi_theme_light_onBackground,
            surface = EudiRQESUi_theme_light_surface,
            onSurface = EudiRQESUi_theme_light_onSurface,
            surfaceVariant = EudiRQESUi_theme_light_surfaceVariant,
            onSurfaceVariant = EudiRQESUi_theme_light_onSurfaceVariant,
            outline = EudiRQESUi_theme_light_outline,
            inverseOnSurface = EudiRQESUi_theme_light_inverseOnSurface,
            inverseSurface = EudiRQESUi_theme_light_inverseSurface,
            inversePrimary = EudiRQESUi_theme_light_inversePrimary,
            surfaceTint = EudiRQESUi_theme_light_surfaceTint,
            outlineVariant = EudiRQESUi_theme_light_outlineVariant,
            scrim = EudiRQESUi_theme_light_scrim,
        )

        val darkColors = ThemeColorsTemplate(
            primary = EudiRQESUi_theme_dark_primary,
            onPrimary = EudiRQESUi_theme_dark_onPrimary,
            primaryContainer = EudiRQESUi_theme_dark_primaryContainer,
            onPrimaryContainer = EudiRQESUi_theme_dark_onPrimaryContainer,
            secondary = EudiRQESUi_theme_dark_secondary,
            onSecondary = EudiRQESUi_theme_dark_onSecondary,
            secondaryContainer = EudiRQESUi_theme_dark_secondaryContainer,
            onSecondaryContainer = EudiRQESUi_theme_dark_onSecondaryContainer,
            tertiary = EudiRQESUi_theme_dark_tertiary,
            onTertiary = EudiRQESUi_theme_dark_onTertiary,
            tertiaryContainer = EudiRQESUi_theme_dark_tertiaryContainer,
            onTertiaryContainer = EudiRQESUi_theme_dark_onTertiaryContainer,
            error = EudiRQESUi_theme_dark_error,
            errorContainer = EudiRQESUi_theme_dark_errorContainer,
            onError = EudiRQESUi_theme_dark_onError,
            onErrorContainer = EudiRQESUi_theme_dark_onErrorContainer,
            background = EudiRQESUi_theme_dark_background,
            onBackground = EudiRQESUi_theme_dark_onBackground,
            surface = EudiRQESUi_theme_dark_surface,
            onSurface = EudiRQESUi_theme_dark_onSurface,
            surfaceVariant = EudiRQESUi_theme_dark_surfaceVariant,
            onSurfaceVariant = EudiRQESUi_theme_dark_onSurfaceVariant,
            outline = EudiRQESUi_theme_dark_outline,
            inverseOnSurface = EudiRQESUi_theme_dark_inverseOnSurface,
            inverseSurface = EudiRQESUi_theme_dark_inverseSurface,
            inversePrimary = EudiRQESUi_theme_dark_inversePrimary,
            surfaceTint = EudiRQESUi_theme_dark_surfaceTint,
            outlineVariant = EudiRQESUi_theme_dark_outlineVariant,
            scrim = EudiRQESUi_theme_dark_scrim,
        )

        val primary: Color
            get() = if (isInDarkMode) {
                Color(EudiRQESUi_theme_dark_primary)
            } else {
                Color(EudiRQESUi_theme_light_primary)
            }

        val secondary: Color
            get() = if (isInDarkMode) {
                Color(EudiRQESUi_theme_dark_secondary)
            } else {
                Color(EudiRQESUi_theme_light_secondary)
            }

        val error: Color
            get() = if (isInDarkMode) {
                Color(EudiRQESUi_theme_dark_error)
            } else {
                Color(EudiRQESUi_theme_light_error)
            }

        val background: Color
            get() = if (isInDarkMode) {
                Color(EudiRQESUi_theme_dark_background)
            } else {
                Color(EudiRQESUi_theme_light_background)
            }

        val onSurface: Color
            get() = if (isInDarkMode) {
                Color(EudiRQESUi_theme_dark_onSurface)
            } else {
                Color(EudiRQESUi_theme_light_onSurface)
            }

        val onSurfaceVariant: Color
            get() = if (isInDarkMode) {
                Color(EudiRQESUi_theme_dark_onSurfaceVariant)
            } else {
                Color(EudiRQESUi_theme_light_onSurfaceVariant)
            }

        val success: Color
            get() = if (isInDarkMode) {
                Color(EudiRQESUi_theme_dark_success)
            } else {
                Color(EudiRQESUi_theme_light_success)
            }

        val onSuccess: Color
            get() = if (isInDarkMode) {
                Color(EudiRQESUi_theme_dark_onSuccess)
            } else {
                Color(EudiRQESUi_theme_light_onSuccess)
            }

        val warning: Color
            get() = if (isInDarkMode) {
                Color(EudiRQESUi_theme_dark_warning)
            } else {
                Color(EudiRQESUi_theme_light_warning)
            }

        val textPrimaryDark: Color
            get() = if (isInDarkMode) {
                Color(EudiRQESUi_theme_dark_textPrimaryDark)
            } else {
                Color(EudiRQESUi_theme_light_textPrimaryDark)
            }

        val primaryVariant: Color
            get() = if (isInDarkMode) {
                Color(EudiRQESUi_theme_dark_primaryVariant)
            } else {
                Color(EudiRQESUi_theme_light_primaryVariant)
            }

        val successVariant: Color
            get() = if (isInDarkMode) {
                Color(EudiRQESUi_theme_dark_successVariant)
            } else {
                Color(EudiRQESUi_theme_light_successVariant)
            }

        val devider: Color
            get() = if (isInDarkMode) {
                Color(EudiRQESUi_theme_dark_devider)
            } else {
                Color(EudiRQESUi_theme_light_devider)
            }
    }
}

val ColorScheme.success: Color
    @Composable get() = if (isSystemInDarkTheme()) {
        Color(ThemeColors.EudiRQESUi_theme_dark_success)
    } else {
        Color(ThemeColors.EudiRQESUi_theme_light_success)
    }

val ColorScheme.onSuccess: Color
    @Composable get() = if (isSystemInDarkTheme()) {
        Color(ThemeColors.EudiRQESUi_theme_dark_onSuccess)
    } else {
        Color(ThemeColors.EudiRQESUi_theme_light_onSuccess)
    }

val ColorScheme.warning: Color
    @Composable get() = if (isSystemInDarkTheme()) {
        Color(ThemeColors.EudiRQESUi_theme_dark_warning)
    } else {
        Color(ThemeColors.EudiRQESUi_theme_light_warning)
    }

val ColorScheme.textPrimaryDark: Color
    @Composable get() = if (isSystemInDarkTheme()) {
        Color(ThemeColors.EudiRQESUi_theme_dark_textPrimaryDark)
    } else {
        Color(ThemeColors.EudiRQESUi_theme_light_textPrimaryDark)
    }

val ColorScheme.primaryVariant: Color
    @Composable get() = if (isSystemInDarkTheme()) {
        Color(ThemeColors.EudiRQESUi_theme_dark_primaryVariant)
    } else {
        Color(ThemeColors.EudiRQESUi_theme_light_primaryVariant)
    }

val ColorScheme.successVariant: Color
    @Composable get() = if (isSystemInDarkTheme()) {
        Color(ThemeColors.EudiRQESUi_theme_dark_successVariant)
    } else {
        Color(ThemeColors.EudiRQESUi_theme_light_successVariant)
    }

val ColorScheme.devider: Color
    @Composable get() = if (isSystemInDarkTheme()) {
        Color(ThemeColors.EudiRQESUi_theme_dark_devider)
    } else {
        Color(ThemeColors.EudiRQESUi_theme_light_devider)
    }