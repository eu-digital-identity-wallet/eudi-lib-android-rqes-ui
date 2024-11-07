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

        // Light theme base colors palette.
        private const val EudiRQESUi_theme_light_primary: Long = 0xFF2A5FD9
        private const val EudiRQESUi_theme_light_onPrimary: Long = white
        private const val EudiRQESUi_theme_light_primaryContainer: Long = 0xFFEADDFF
        private const val EudiRQESUi_theme_light_onPrimaryContainer: Long = 0xFF21005D
        private const val EudiRQESUi_theme_light_secondary: Long = 0xFFD6D9F9
        private const val EudiRQESUi_theme_light_onSecondary: Long = 0xF1D192B
        private const val EudiRQESUi_theme_light_secondaryContainer: Long = 0xFFE8DEF8
        private const val EudiRQESUi_theme_light_onSecondaryContainer: Long = 0xFF1D192B
        private const val EudiRQESUi_theme_light_tertiary: Long = 0xFFE4EEE7
        private const val EudiRQESUi_theme_light_onTertiary: Long = 0xFF1D192B
        private const val EudiRQESUi_theme_light_tertiaryContainer: Long = 0xFFFFD8E4
        private const val EudiRQESUi_theme_light_onTertiaryContainer: Long = 0xFF31111D
        private const val EudiRQESUi_theme_light_error: Long = 0xFFB3261E
        private const val EudiRQESUi_theme_light_onError: Long = white
        private const val EudiRQESUi_theme_light_errorContainer: Long = 0xFFF9DEDC
        private const val EudiRQESUi_theme_light_onErrorContainer: Long = 0xFF410E0B
        private const val EudiRQESUi_theme_light_surface: Long = 0xFFF7FAFF
        private const val EudiRQESUi_theme_light_onSurface: Long = 0xFF1D1B20
        private const val EudiRQESUi_theme_light_background: Long = EudiRQESUi_theme_light_surface
        private const val EudiRQESUi_theme_light_onBackground: Long = EudiRQESUi_theme_light_onSurface
        private const val EudiRQESUi_theme_light_surfaceVariant: Long = 0xFFF5DED8
        private const val EudiRQESUi_theme_light_onSurfaceVariant: Long = 0xFF49454F
        private const val EudiRQESUi_theme_light_outline: Long = 0xFF79747E
        private const val EudiRQESUi_theme_light_outlineVariant: Long = 0xFFCAC4D0
        private const val EudiRQESUi_theme_light_scrim: Long = black
        private const val EudiRQESUi_theme_light_inverseSurface: Long = 0xFF322F35
        private const val EudiRQESUi_theme_light_inverseOnSurface: Long = 0xFFF5EFF7
        private const val EudiRQESUi_theme_light_inversePrimary: Long = 0xFFD0BCFF
        private const val EudiRQESUi_theme_light_surfaceDim: Long = 0xFFE2E8F3
        private const val EudiRQESUi_theme_light_surfaceBright: Long = 0xFFFEF7FF
        private const val EudiRQESUi_theme_light_surfaceContainerLowest: Long = white
        private const val EudiRQESUi_theme_light_surfaceContainerLow: Long = 0xFFF7F2FA
        private const val EudiRQESUi_theme_light_surfaceContainer: Long = 0xFFEBF1FD
        private const val EudiRQESUi_theme_light_surfaceContainerHigh: Long = 0xFFECE6F0
        private const val EudiRQESUi_theme_light_surfaceContainerHighest: Long = 0xFFE6E0E9
        private const val EudiRQESUi_theme_light_surfaceTint: Long = EudiRQESUi_theme_light_surface

        // Light theme extra colors palette.
        internal const val EudiRQESUi_theme_light_success: Long = 0xFF55953B
        internal const val EudiRQESUi_theme_light_warning: Long = 0xFFAB5200
        internal const val EudiRQESUi_theme_light_divider: Long = 0xFFD9D9D9

        // Dark theme base colors palette.
        private const val EudiRQESUi_theme_dark_primary: Long = 0xFFFFB5A0
        private const val EudiRQESUi_theme_dark_onPrimary: Long = 0xFF561F0F
        private const val EudiRQESUi_theme_dark_primaryContainer: Long = 0xFF723523
        private const val EudiRQESUi_theme_dark_onPrimaryContainer: Long = 0xFFFFDBD1
        private const val EudiRQESUi_theme_dark_secondary: Long = 0xFFE7BDB2
        private const val EudiRQESUi_theme_dark_onSecondary: Long = 0xFF442A22
        private const val EudiRQESUi_theme_dark_secondaryContainer: Long = 0xFF5D4037
        private const val EudiRQESUi_theme_dark_onSecondaryContainer: Long = 0xFFFFDBD1
        private const val EudiRQESUi_theme_dark_tertiary: Long = 0xFFD8C58D
        private const val EudiRQESUi_theme_dark_onTertiary: Long = 0xFF3B2F05
        private const val EudiRQESUi_theme_dark_tertiaryContainer: Long = 0xFF534619
        private const val EudiRQESUi_theme_dark_onTertiaryContainer: Long = 0xFFF5E1A7
        private const val EudiRQESUi_theme_dark_error: Long = 0xFFFFB4AB
        private const val EudiRQESUi_theme_dark_onError: Long = 0xFF690005
        private const val EudiRQESUi_theme_dark_errorContainer: Long = 0xFF93000A
        private const val EudiRQESUi_theme_dark_onErrorContainer: Long = 0xFFFFDAD6
        private const val EudiRQESUi_theme_dark_surface: Long = black
        private const val EudiRQESUi_theme_dark_onSurface: Long = white
        private const val EudiRQESUi_theme_dark_background: Long = EudiRQESUi_theme_dark_surface
        private const val EudiRQESUi_theme_dark_onBackground: Long = EudiRQESUi_theme_dark_onSurface
        private const val EudiRQESUi_theme_dark_surfaceVariant: Long = 0xFF53433F
        private const val EudiRQESUi_theme_dark_onSurfaceVariant: Long = 0xFFD8C2BC
        private const val EudiRQESUi_theme_dark_outline: Long = 0xFFA08C87
        private const val EudiRQESUi_theme_dark_outlineVariant: Long = 0xFF53433F
        private const val EudiRQESUi_theme_dark_scrim: Long = 0xFF000000
        private const val EudiRQESUi_theme_dark_inverseSurface: Long = 0xFFF1DFDA
        private const val EudiRQESUi_theme_dark_inverseOnSurface: Long = 0xFF392E2B
        private const val EudiRQESUi_theme_dark_inversePrimary: Long = 0xFF8F4C38
        private const val EudiRQESUi_theme_dark_surfaceDim: Long = 0xFF1A110F
        private const val EudiRQESUi_theme_dark_surfaceBright: Long = 0xFF423734
        private const val EudiRQESUi_theme_dark_surfaceContainerLowest: Long = 0xFF140C0A
        private const val EudiRQESUi_theme_dark_surfaceContainerLow: Long = 0xFF231917
        private const val EudiRQESUi_theme_dark_surfaceContainer: Long = 0xFF271D1B
        private const val EudiRQESUi_theme_dark_surfaceContainerHigh: Long = 0xFF322825
        private const val EudiRQESUi_theme_dark_surfaceContainerHighest: Long = 0xFF3D322F
        private const val EudiRQESUi_theme_dark_surfaceTint: Long = EudiRQESUi_theme_dark_surface

        // Dark theme extra colors palette.
        internal const val EudiRQESUi_theme_dark_success: Long = 0xFF55953B
        internal const val EudiRQESUi_theme_dark_warning: Long = 0xFFAB5200
        internal const val EudiRQESUi_theme_dark_divider: Long = 0xFFD9D9D9

        internal const val EudiRQESUi_theme_light_background_preview: Long = EudiRQESUi_theme_light_surface
        internal const val EudiRQESUi_theme_dark_background_preview: Long = EudiRQESUi_theme_dark_surface

        internal val lightColors = ThemeColorsTemplate(
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
            surfaceBright = EudiRQESUi_theme_light_surfaceBright,
            surfaceDim = EudiRQESUi_theme_light_surfaceDim,
            surfaceContainer = EudiRQESUi_theme_light_surfaceContainer,
            surfaceContainerHigh = EudiRQESUi_theme_light_surfaceContainerHigh,
            surfaceContainerHighest = EudiRQESUi_theme_light_surfaceContainerHighest,
            surfaceContainerLow = EudiRQESUi_theme_light_surfaceContainerLow,
            surfaceContainerLowest = EudiRQESUi_theme_light_surfaceContainerLowest,
        )

        internal val darkColors = ThemeColorsTemplate(
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
            surfaceBright = EudiRQESUi_theme_dark_surfaceBright,
            surfaceDim = EudiRQESUi_theme_dark_surfaceDim,
            surfaceContainer = EudiRQESUi_theme_dark_surfaceContainer,
            surfaceContainerHigh = EudiRQESUi_theme_dark_surfaceContainerHigh,
            surfaceContainerHighest = EudiRQESUi_theme_dark_surfaceContainerHighest,
            surfaceContainerLow = EudiRQESUi_theme_dark_surfaceContainerLow,
            surfaceContainerLowest = EudiRQESUi_theme_dark_surfaceContainerLowest,
        )

        internal val success: Color
            get() = if (isInDarkMode) {
                Color(EudiRQESUi_theme_dark_success)
            } else {
                Color(EudiRQESUi_theme_light_success)
            }

        internal val warning: Color
            get() = if (isInDarkMode) {
                Color(EudiRQESUi_theme_dark_warning)
            } else {
                Color(EudiRQESUi_theme_light_warning)
            }

        internal val divider: Color
            get() = if (isInDarkMode) {
                Color(EudiRQESUi_theme_dark_divider)
            } else {
                Color(EudiRQESUi_theme_light_divider)
            }
    }
}

internal val ColorScheme.success: Color
    @Composable get() = if (isSystemInDarkTheme()) {
        Color(ThemeColors.EudiRQESUi_theme_dark_success)
    } else {
        Color(ThemeColors.EudiRQESUi_theme_light_success)
    }

internal val ColorScheme.warning: Color
    @Composable get() = if (isSystemInDarkTheme()) {
        Color(ThemeColors.EudiRQESUi_theme_dark_warning)
    } else {
        Color(ThemeColors.EudiRQESUi_theme_light_warning)
    }

internal val ColorScheme.divider: Color
    @Composable get() = if (isSystemInDarkTheme()) {
        Color(ThemeColors.EudiRQESUi_theme_dark_divider)
    } else {
        Color(ThemeColors.EudiRQESUi_theme_light_divider)
    }