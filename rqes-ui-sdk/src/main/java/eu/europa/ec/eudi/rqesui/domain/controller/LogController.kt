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

import eu.europa.ec.eudi.rqesui.infrastructure.config.EudiRQESUiConfig
import timber.log.Timber
import timber.log.Timber.DebugTree

internal interface LogController {
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

internal class LogControllerImpl(
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