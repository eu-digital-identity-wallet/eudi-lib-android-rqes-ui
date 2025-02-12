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
import eu.europa.ec.eudi.rqes.HashAlgorithmOID
import eu.europa.ec.eudi.rqesui.domain.entities.localization.LocalizableKey
import eu.europa.ec.eudi.rqesui.domain.extension.toUri
import eu.europa.ec.eudi.rqesui.infrastructure.EudiRQESUi
import eu.europa.ec.eudi.rqesui.infrastructure.config.EudiRQESUiConfig
import eu.europa.ec.eudi.rqesui.infrastructure.config.RqesServiceConfig
import eu.europa.ec.eudi.rqesui.infrastructure.config.data.QtspData
import java.net.URI

class TestRQESApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initRQESSDK()
    }

    private fun initRQESSDK() {
        EudiRQESUi.setup(application = this, config = DefaultConfig())
    }
}

private class DefaultConfig : EudiRQESUiConfig {

    override val rqesServiceConfig: RqesServiceConfig
        get() = RqesServiceConfig(
            clientId = "wallet-client-tester",
            clientSecret = "somesecrettester2",
            authFlowRedirectionURI = URI.create("rqes://oauth/callback"),
            hashAlgorithm = HashAlgorithmOID.SHA_256,
        )

    override val qtsps: List<QtspData>
        get() = listOf(
            QtspData(
                name = "Wallet-Centric",
                endpoint = "https://walletcentric.signer.eudiw.dev/csc/v2".toUri(),
                scaUrl = "https://walletcentric.signer.eudiw.dev",
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
}