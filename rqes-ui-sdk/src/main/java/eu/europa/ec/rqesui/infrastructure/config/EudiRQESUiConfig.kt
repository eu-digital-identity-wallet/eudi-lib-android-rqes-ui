package eu.europa.ec.rqesui.infrastructure.config

import eu.europa.ec.rqesui.domain.entities.localization.LocalizableKey
import java.net.URI

interface EudiRQESUiConfig {

    // QTSPs List
    val qtsps: List<URI>

    // Transactions per locale
    val translations: Map<String, Map<LocalizableKey, String>>

    // Logging is enabled
    val printLogs: Boolean
}