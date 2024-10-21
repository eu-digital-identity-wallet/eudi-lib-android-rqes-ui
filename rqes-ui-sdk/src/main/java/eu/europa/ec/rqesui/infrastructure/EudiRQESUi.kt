package eu.europa.ec.rqesui.infrastructure

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import eu.europa.ec.rqesui.domain.di.base.EudiRQESUIModule
import eu.europa.ec.rqesui.infrastructure.config.EudiRQESUiConfig
import eu.europa.ec.rqesui.presentation.ui.container.EudiRQESContainer
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.ksp.generated.module

object EudiRQESUi {

    private lateinit var _eudiRQESUiConfig: EudiRQESUiConfig

    fun setup(application: Application, config: EudiRQESUiConfig) {
        _eudiRQESUiConfig = config
        setupKoin(application)
    }

    fun fromStart(context: Context, resultCode: Int) {
        (context as? Activity)?.startActivityForResult(
            Intent(context, EudiRQESContainer::class.java),
            resultCode
        )
    }

    @Throws
    internal fun getEudiRQESUiConfig(): EudiRQESUiConfig {
        if (!::_eudiRQESUiConfig.isInitialized) {
            throw IllegalStateException("EudiRQESUiConfig must be initialized first")
        }
        return _eudiRQESUiConfig
    }

    private fun setupKoin(application: Application) {
        startKoin {
            androidContext(application)
            if (getEudiRQESUiConfig().printLogs) {
                androidLogger()
            }
            modules(EudiRQESUIModule().module)
        }
    }
}