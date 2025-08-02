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

package eu.europa.ec.eudi.testrqes

import android.app.Application
import android.content.Context
import eu.europa.ec.eudi.rqes.HashAlgorithmOID
import eu.europa.ec.eudi.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.eudi.rqesui.domain.extension.toUriOrEmpty
import eu.europa.ec.eudi.rqesui.infrastructure.EudiRQESUi
import eu.europa.ec.eudi.rqesui.infrastructure.config.DocumentRetrievalConfig
import eu.europa.ec.eudi.rqesui.infrastructure.config.EudiRQESUiConfig
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.QtspData
import java.net.URI

class TestRQESApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initRQESSDK()
    }

    private fun initRQESSDK() {
        EudiRQESUi.setup(application = this, config = DefaultConfig(this))
    }
}

private class DefaultConfig(val context: Context) : EudiRQESUiConfig {

    override val qtsps: List<QtspData>
        get() = listOf(
            QtspData(
                name = "Wallet-Centric",
                endpoint = "https://walletcentric.signer.eudiw.dev/csc/v2".toUriOrEmpty(),
                clientId = "wallet-client-tester",
                clientSecret = "somesecrettester2",
                authFlowRedirectionURI = URI.create("rqes://oauth/callback"),
                hashAlgorithm = HashAlgorithmOID.SHA_256,
                tsaUrl = "https://timestamp.sectigo.com/qualified"
            )
        )

    override val translations: Map<String, Map<LocalizableKey, String>>
        get() {
            return mapOf(
                "en" to mapOf(
                    LocalizableKey.View to "VIEW",
                )
            )
        }

    override val documentRetrievalConfig: DocumentRetrievalConfig
        get() = DocumentRetrievalConfig.X509Certificates(
            context = context,
            certificates = listOf(
                R.raw.pidissuerca02_cz,
                R.raw.pidissuerca02_ee,
                R.raw.pidissuerca02_eu,
                R.raw.pidissuerca02_lu,
                R.raw.pidissuerca02_nl,
                R.raw.pidissuerca02_pt,
                R.raw.pidissuerca02_ut
            ),
            shouldLog = true
        )
}