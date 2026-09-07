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

package eu.europa.ec.eudi.rqesui.infrastructure.config.data

import android.os.Parcel
import eu.europa.ec.eudi.rqes.HashAlgorithmOID
import eu.europa.ec.eudi.rqes.SigningAlgorithmOID
import eu.europa.ec.eudi.rqes.core.RQESService
import eu.europa.ec.eudi.rqesui.domain.extension.toUriOrEmpty
import eu.europa.ec.eudi.rqesui.util.mockedClientId
import eu.europa.ec.eudi.rqesui.util.mockedClientSecret
import eu.europa.ec.eudi.rqesui.util.mockedQtspEndpoint
import eu.europa.ec.eudi.rqesui.util.mockedQtspName
import eu.europa.ec.eudi.rqesui.util.mockedTsaUrl
import eu.europa.ec.eudi.rqesui.util.mockedUri
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.URI

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TestQtspData {

    private fun qtspData(
        signingAlgorithm: RQESService.SigningAlgorithm = RQESService.SigningAlgorithm.FirstSupportedByCredential,
    ) = QtspData(
        name = mockedQtspName,
        endpoint = mockedQtspEndpoint.toUriOrEmpty(),
        tsaUrl = mockedTsaUrl,
        clientId = mockedClientId,
        clientSecret = mockedClientSecret,
        authFlowRedirectionURI = URI.create(mockedUri),
        hashAlgorithm = HashAlgorithmOID.SHA_256,
        signingAlgorithm = signingAlgorithm,
    )

    private fun QtspData.parcelRoundTrip(): QtspData {
        val parcel = Parcel.obtain()
        return try {
            parcel.writeParcelable(this, 0)
            parcel.setDataPosition(0)
            @Suppress("DEPRECATION")
            requireNotNull(parcel.readParcelable(QtspData::class.java.classLoader))
        } finally {
            parcel.recycle()
        }
    }

    //region signingAlgorithm

    // Case 1:
    // QtspData is created without specifying a signing algorithm.
    // Expected: the default is FirstSupportedByCredential, preserving the behavior of letting
    // the selected credential decide.
    @Test
    fun `Given no signing algorithm, When QtspData is created, Then FirstSupportedByCredential is the default`() {
        assertEquals(
            RQESService.SigningAlgorithm.FirstSupportedByCredential,
            qtspData().signingAlgorithm
        )
    }

    // Case 2:
    // QtspData holding FirstSupportedByCredential is written to and read back from a Parcel.
    // Expected: the object is restored unchanged.
    @Test
    fun `Given FirstSupportedByCredential, When QtspData is parcelled, Then it round-trips unchanged`() {
        val original = qtspData(RQESService.SigningAlgorithm.FirstSupportedByCredential)

        val restored = original.parcelRoundTrip()

        assertEquals(original, restored)
        assertEquals(
            RQESService.SigningAlgorithm.FirstSupportedByCredential,
            restored.signingAlgorithm
        )
    }

    // Case 3:
    // QtspData holding a Specific signing algorithm is written to and read back from a Parcel.
    // Expected: the object is restored unchanged, including the algorithm OID.
    @Test
    fun `Given a Specific signing algorithm, When QtspData is parcelled, Then it round-trips unchanged`() {
        val expectedAlgorithm =
            RQESService.SigningAlgorithm.Specific(SigningAlgorithmOID.ECDSA_SHA256)
        val original = qtspData(expectedAlgorithm)

        val restored = original.parcelRoundTrip()

        assertEquals(original, restored)
        assertEquals(expectedAlgorithm, restored.signingAlgorithm)
        assertEquals(
            SigningAlgorithmOID.ECDSA_SHA256.value,
            (restored.signingAlgorithm as RQESService.SigningAlgorithm.Specific).oid.value
        )
    }

    //endregion
}
