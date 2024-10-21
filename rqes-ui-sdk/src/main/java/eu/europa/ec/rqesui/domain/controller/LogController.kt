package eu.europa.ec.rqesui.domain.controller

import eu.europa.ec.rqesui.infrastructure.config.EudiRQESUiConfig
import timber.log.Timber
import timber.log.Timber.DebugTree

interface LogController {
    fun d(tag: String, message: () -> String)
    fun d(message: () -> String)
    fun e(tag: String, message: () -> String)
    fun e(tag: String, exception: Throwable)
    fun e(message: () -> String)
    fun e(exception: Throwable)
    fun w(tag: String, message: () -> String)
    fun w(message: () -> String)
    fun i(tag: String, message: () -> String)
    fun i(message: () -> String)
}

class LogControllerImpl(
    private val config: EudiRQESUiConfig
) : LogController {

    private companion object {
        const val TAG: String = "EudiRQESUi"
    }

    init {
        if (config.printLogs) {
            Timber.plant(DebugTree())
        }
    }

    override fun d(tag: String, message: () -> String) {
        Timber.d(message())
    }

    override fun d(message: () -> String) {
        d(tag = TAG, message = message)
    }

    override fun e(tag: String, message: () -> String) {
        Timber.e(message())
    }

    override fun e(tag: String, exception: Throwable) {
        Timber.e(exception)
    }

    override fun e(message: () -> String) {
        e(TAG, message)
    }

    override fun e(exception: Throwable) {
        e(TAG, exception)
    }

    override fun w(tag: String, message: () -> String) {
        Timber.w(message())
    }

    override fun w(message: () -> String) {
        w(TAG, message)
    }

    override fun i(tag: String, message: () -> String) {
        Timber.i(message())
    }

    override fun i(message: () -> String) {
        i(TAG, message)
    }
}