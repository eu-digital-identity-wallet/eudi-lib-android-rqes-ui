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

package eu.europa.ec.eudi.rqesui.infrastructure.config

import eu.europa.ec.eudi.rqesui.infrastructure.config.data.QtspData
import org.junit.Assert.assertNull
import org.junit.Test

class TestEudiRQESUiConfig {

    //region signingLogger

    // Diagnostic logging does not enable signing callbacks.
    @Test
    fun `Given no signing logger, When diagnostic logging changes, Then signing callbacks remain disabled`() {
        listOf(false, true).forEach { diagnosticLogsEnabled ->
            // Arrange
            val config = object : EudiRQESUiConfig {
                override val qtsps: List<QtspData> = emptyList()
                override val documentRetrievalConfig = DocumentRetrievalConfig.NoValidation
                override val printLogs = diagnosticLogsEnabled
            }

            // Act
            val signingLogger = config.signingLogger

            // Assert
            assertNull(signingLogger)
        }
    }

    //endregion
}