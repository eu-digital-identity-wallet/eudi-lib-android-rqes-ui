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

package eu.europa.ec.eudi.rqesui.infrastructure.config

import eu.europa.ec.eudi.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.QtspData
import eu.europa.ec.eudi.rqesui.infrastructure.theme.ThemeManager
import eu.europa.ec.eudi.rqesui.infrastructure.theme.values.ThemeColors
import eu.europa.ec.eudi.rqesui.infrastructure.theme.values.ThemeTypography

interface EudiRQESUiConfig {

    val rqesServiceConfig: RqesServiceConfig

    // QTSPs List
    val qtsps: List<QtspData>

    /**
     * Provides a map of translations for different locales.
     *
     * The key of the outer map represents the locale (e.g., "en" for English).
     *
     * The value of the outer map is another map, where:
     *  - The key is a [LocalizableKey] representing a specific string to be translated.
     *  - The value is the translated string for that key in the given locale.
     *
     * Currently, only English ("en") translations are provided, using the default translations
     * defined in the [LocalizableKey] enum entries.
     *
     * In order to override a translation that receives arguments
     * please do so writing `$ARGUMENTS_SEPARATOR` in place of each of the arguments.
     * For example:
     *
     * ```
     * override val translations: Map<String, Map<LocalizableKey, String>>
     *         get() {
     *             return mapOf(
     *                 "en" to mapOf(
     *                     LocalizableKey.View to "VIEW",
     *                     LocalizableKey.SignedBy to "Signed by $ARGUMENTS_SEPARATOR",
     *                 )
     *             )
     *         }
     * ```
     *
     */
    val translations: Map<String, Map<LocalizableKey, String>>
        get() {
            return mapOf(
                "en" to LocalizableKey.entries.associateWith { it.defaultTranslation() }
            )
        }

    // Logging is enabled
    val printLogs: Boolean
        get() = false

    // Theme manager
    val themeManager: ThemeManager
        get() {
            return ThemeManager.Builder()
                .withLightColors(ThemeColors.lightColors)
                .withDarkColors(ThemeColors.darkColors)
                .withTypography(ThemeTypography.typo)
                .build()
        }

    /**
     * Specifies the level of trust required for document retrieval operations.
     */
    val documentRetrievalConfig: DocumentRetrievalConfig
}