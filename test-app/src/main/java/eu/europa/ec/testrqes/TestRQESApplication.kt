package eu.europa.ec.testrqes

import android.app.Application
import eu.europa.ec.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.rqesui.infrastructure.EudiRQESUi
import eu.europa.ec.rqesui.infrastructure.config.EudiRQESUiConfig
import java.net.URI

class TestRQESApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initRQESSDK()
    }

    private fun initRQESSDK() {
        EudiRQESUi.setup(this, DefaultConfig())
    }
}

private class DefaultConfig : EudiRQESUiConfig {

    override val qtsps: List<URI>
        get() = emptyList()

    override val translations: Map<String, Map<LocalizableKey, String>>
        get() = mapOf(
            "en" to mapOf(
                LocalizableKey.Mock to "Mock",
                LocalizableKey.MockWithValues to "Mock %@, %@"
            )
        )

    override val printLogs: Boolean
        get() = true

}