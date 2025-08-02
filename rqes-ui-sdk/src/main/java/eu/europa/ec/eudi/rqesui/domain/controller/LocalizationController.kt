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

package eu.europa.ec.eudi.rqesui.domain.controller

import eu.europa.ec.eudi.rqesui.domain.entities.error.EudiRQESUiError
import eu.europa.ec.eudi.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.eudi.rqesui.domain.extension.localizationFormatWithArgs
import eu.europa.ec.eudi.rqesui.infrastructure.config.EudiRQESUiConfig
import java.util.Locale

internal interface LocalizationController {
    fun get(
        key: LocalizableKey,
        args: List<String> = emptyList(),
    ): String
}

internal class LocalizationControllerImpl(
    private val config: EudiRQESUiConfig
) : LocalizationController {

    /**
     * Retrieves the localized string for the given key and language.
     *
     * This function first checks if translations are available. If not, it throws an [EudiRQESUiError].
     * It then determines the current language based on the device's locale.
     * If a translation is found for the key and language, it is returned.
     * Otherwise, the default translation of the key is used.
     * The returned string is formatted with any provided arguments.
     *
     * @param key The [LocalizableKey] to retrieve the translation for.
     * @param args A list of arguments to format the translation with.
     * @return The localized string for the given key and language.
     * @throws EudiRQESUiError If no translations are found in the config.
     */
    @Throws(EudiRQESUiError::class)
    override fun get(
        key: LocalizableKey,
        args: List<String>,
    ): String {
        if (config.translations.isEmpty()) {
            throw EudiRQESUiError(
                title = "Translation Error",
                message = "No translations found. Please provide translations in the config."
            )
        }

        val language = Locale.getDefault().language
        val translation = config.translations[language]?.get(key) ?: key.defaultTranslation()

        return translation.localizationFormatWithArgs(args)
    }
}