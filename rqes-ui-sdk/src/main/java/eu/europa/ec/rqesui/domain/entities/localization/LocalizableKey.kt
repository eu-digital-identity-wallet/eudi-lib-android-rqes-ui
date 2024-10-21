package eu.europa.ec.rqesui.domain.entities.localization

import eu.europa.ec.rqesui.domain.extension.localizationFormatWithArgs

enum class LocalizableKey {
    Mock,
    MockWithValues;

    fun defaultTranslation(args: List<String>): String {
        return when (this) {
            Mock -> "Mock"
            MockWithValues -> "Mock %@, %@"
        }.localizationFormatWithArgs(args)
    }
}