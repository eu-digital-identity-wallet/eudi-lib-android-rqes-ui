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
import java.net.URI

object EudiRQESUi {

    private lateinit var _eudiRQESUiConfig: EudiRQESUiConfig
    private var state: State = State.None

    fun setup(application: Application, config: EudiRQESUiConfig) {
        _eudiRQESUiConfig = config
        setupKoin(application)
    }

    fun initiate(
        context: Context,
        file: URI
    ) {
        setState(State.Initial(file))
        resume(context)
    }

    fun resume(context: Context) {
        (context as? Activity)?.startActivity(
            Intent(context, EudiRQESContainer::class.java)
        )
    }

    @Throws
    internal fun getEudiRQESUiConfig(): EudiRQESUiConfig {
        if (!::_eudiRQESUiConfig.isInitialized) {
            throw IllegalStateException("EudiRQESUi must be initialized first. Please call EudiRQESUi.setup()")
        }
        return _eudiRQESUiConfig
    }

    internal fun setState(state: State) {
        this.state = state
    }

    internal fun getState(): State = this.state

    private fun setupKoin(application: Application) {
        startKoin {
            androidContext(application)
            if (getEudiRQESUiConfig().printLogs) {
                androidLogger()
            }
            modules(EudiRQESUIModule().module)
        }
    }

    sealed class State {
        data object None : State()
        data class Initial(val file: URI) : State()
        data class Certificate(val file: String) : State()
        data class Sign(val value: String) : State()
    }
}