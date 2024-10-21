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

package eu.europa.ec.rqesui.domain.controller

import eu.europa.ec.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.rqesui.domain.extension.safeLet
import eu.europa.ec.rqesui.infrastructure.config.EudiRQESUiConfig
import java.util.Locale

interface LocalizationController {
    fun get(key: LocalizableKey, args: List<String>): String
}

class LocalizationControllerImpl(
    private val config: EudiRQESUiConfig
) : LocalizationController {

    override fun get(
        key: LocalizableKey,
        args: List<String>
    ): String {
        safeLet(
            config.translations.isNotEmpty(),
            config.translations[Locale.getDefault().isO3Language]?.get(key)
        ) { hasValues, translation ->
            if (hasValues) {
                return translation.format(args)
            }
        }
        return key.defaultTranslation(args)
    }
}