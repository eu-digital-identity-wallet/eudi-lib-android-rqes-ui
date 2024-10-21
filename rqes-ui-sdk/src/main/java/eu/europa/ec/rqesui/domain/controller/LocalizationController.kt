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