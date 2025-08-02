/*
 * Copyright (c) 2025 European Commission
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

package eu.europa.ec.eudi.rqesui.infrastructure.config

import eu.europa.ec.eudi.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.QtspData
import eu.europa.ec.eudi.rqesui.infrastructure.theme.ThemeManager
import eu.europa.ec.eudi.rqesui.infrastructure.theme.values.ThemeColors
import eu.europa.ec.eudi.rqesui.infrastructure.theme.values.ThemeTypography

interface EudiRQESUiConfig {

    /**
     * The list of Qualified Trust Service Providers (QTSPs) to be displayed and used for signing.
     * Each entry in the list provides information about a specific QTSP.
     */
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