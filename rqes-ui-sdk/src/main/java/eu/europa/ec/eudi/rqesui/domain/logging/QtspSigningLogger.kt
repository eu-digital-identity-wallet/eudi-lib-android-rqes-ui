/*
 * Copyright (c) 2026 European Commission
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

package eu.europa.ec.eudi.rqesui.domain.logging

import eu.europa.ec.eudi.rqes.core.RqesSigningLogger
import eu.europa.ec.eudi.rqes.core.RqesSigningRecord

/**
 * Fills missing service names from the selected QTSP before forwarding signing records.
 */
internal class QtspSigningLogger(
    private val delegate: RqesSigningLogger,
    qtspName: String
) : RqesSigningLogger {

    private companion object {
        const val UNDETERMINED_LANGUAGE_TAG = "und"
    }

    private val fallbackServiceName = qtspName.takeIf { it.isNotBlank() }?.let { name ->
        RqesSigningRecord.LocalizedName(languageTag = UNDETERMINED_LANGUAGE_TAG, name = name)
    }

    override fun onSigningCompleted(record: RqesSigningRecord) {
        val serviceName = record.serviceName?.takeIf { it.name.isNotBlank() } ?: fallbackServiceName
        delegate.onSigningCompleted(record.copy(serviceName = serviceName))
    }
}