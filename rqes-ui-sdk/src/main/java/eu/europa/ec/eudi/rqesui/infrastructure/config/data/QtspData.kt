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

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import eu.europa.ec.eudi.rqes.HashAlgorithmOID
import eu.europa.ec.eudi.rqes.SigningAlgorithmOID
import eu.europa.ec.eudi.rqes.core.RQESService
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import java.net.URI

/**
 * Configuration for a single Qualified Trust Service Provider (QTSP).
 *
 * @property signingAlgorithm The signing algorithm used for this QTSP. Defaults to
 * [RQESService.SigningAlgorithm.FirstSupportedByCredential], which picks the first algorithm
 * advertised by the credential the user selects. Use
 * [RQESService.SigningAlgorithm.Specific] to pin a particular algorithm; it must be one the
 * selected credential supports, otherwise signing fails.
 */
@Parcelize
@TypeParceler<HashAlgorithmOID, HashAlgorithmOIDParceler>
@TypeParceler<RQESService.SigningAlgorithm, SigningAlgorithmParceler>
data class QtspData(
    val name: String,
    val endpoint: Uri,
    val tsaUrl: String?,
    val clientId: String,
    val clientSecret: String,
    val authFlowRedirectionURI: URI,
    val hashAlgorithm: HashAlgorithmOID,
    val signingAlgorithm: RQESService.SigningAlgorithm = RQESService.SigningAlgorithm.FirstSupportedByCredential,
) : Parcelable

object HashAlgorithmOIDParceler : Parceler<HashAlgorithmOID> {
    override fun create(parcel: Parcel): HashAlgorithmOID {
        return HashAlgorithmOID(parcel.readString().orEmpty())
    }

    override fun HashAlgorithmOID.write(parcel: Parcel, flags: Int) {
        parcel.writeString(value)
    }
}

object SigningAlgorithmParceler : Parceler<RQESService.SigningAlgorithm> {
    override fun create(parcel: Parcel): RQESService.SigningAlgorithm {
        return parcel.readString()
            ?.let { RQESService.SigningAlgorithm.Specific(SigningAlgorithmOID(it)) }
            ?: RQESService.SigningAlgorithm.FirstSupportedByCredential
    }

    override fun RQESService.SigningAlgorithm.write(parcel: Parcel, flags: Int) {
        parcel.writeString(
            when (this) {
                RQESService.SigningAlgorithm.FirstSupportedByCredential -> null
                is RQESService.SigningAlgorithm.Specific -> oid.value
            }
        )
    }
}
