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

package eu.europa.ec.eudi.rqesui.domain.serializer

import android.net.Uri
import eu.europa.ec.eudi.rqesui.domain.extension.decodeFromBase64
import eu.europa.ec.eudi.rqesui.domain.extension.encodeToBase64
import eu.europa.ec.eudi.rqesui.domain.serializer.kserializer.UriSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

/**
 * Shared [Json] used by [UiSerializerImpl] for every [UiSerializable] config.
 *
 * The Android [Uri] type is registered contextually so navigation configs that carry
 * one (e.g. `DocumentData.uri` inside `ViewDocumentUiConfig`) can round-trip without
 * each call site supplying its own serializer.
 */
private val UiJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    classDiscriminator = "type"
    serializersModule = SerializersModule {
        contextual(Uri::class, UriSerializer)
    }
}

internal interface UiSerializer {
    fun <M : UiSerializable> toBase64(
        model: M,
        parser: UiSerializableParser,
    ): String?

    fun <M : UiSerializable> fromBase64(
        payload: String?,
        model: Class<M>,
        parser: UiSerializableParser,
    ): M?
}

internal class UiSerializerImpl : UiSerializer {

    override fun <M : UiSerializable> toBase64(
        model: M,
        parser: UiSerializableParser,
    ): String? = runCatching {
        @Suppress("UNCHECKED_CAST")
        val serializer = serializer(model::class.java) as KSerializer<M>
        UiJson.encodeToString(serializer, model).encodeToBase64()
    }.getOrNull()

    override fun <M : UiSerializable> fromBase64(
        payload: String?,
        model: Class<M>,
        parser: UiSerializableParser,
    ): M? {
        if (payload == null) return null
        return runCatching {
            @Suppress("UNCHECKED_CAST")
            val serializer = serializer(model) as KSerializer<M>
            UiJson.decodeFromString(serializer, payload.decodeFromBase64())
        }.getOrNull()
    }
}