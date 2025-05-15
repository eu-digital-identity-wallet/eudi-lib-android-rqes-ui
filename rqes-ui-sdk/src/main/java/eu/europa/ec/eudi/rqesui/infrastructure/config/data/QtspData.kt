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

package eu.europa.ec.eudi.rqesui.infrastructure.config.data

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import eu.europa.ec.eudi.rqes.HashAlgorithmOID
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import java.net.URI

@Parcelize
@TypeParceler<HashAlgorithmOID, HashAlgorithmOIDParceler>
data class QtspData(
    val name: String,
    val endpoint: Uri,
    val scaUrl: String,
    val clientId: String,
    val clientSecret: String,
    val authFlowRedirectionURI: URI,
    val hashAlgorithm: HashAlgorithmOID,
) : Parcelable

object HashAlgorithmOIDParceler : Parceler<HashAlgorithmOID> {
    override fun create(parcel: Parcel): HashAlgorithmOID {
        return HashAlgorithmOID(parcel.readString().orEmpty())
    }

    override fun HashAlgorithmOID.write(parcel: Parcel, flags: Int) {
        parcel.writeString(value)
    }
}