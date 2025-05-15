/*
 * Copyright (c) 2023 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work
 * except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific language
 * governing permissions and limitations under the Licence.
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
                scaUrl = "https://walletcentric.signer.eudiw.dev",
                clientId = "wallet-client-tester",
                clientSecret = "somesecrettester2",
                authFlowRedirectionURI = URI.create("rqes://oauth/callback"),
                hashAlgorithm = HashAlgorithmOID.SHA_256,
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