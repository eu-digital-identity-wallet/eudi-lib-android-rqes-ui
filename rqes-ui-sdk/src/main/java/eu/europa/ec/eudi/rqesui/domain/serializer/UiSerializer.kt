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

import eu.europa.ec.eudi.rqesui.domain.extension.decodeFromBase64
import eu.europa.ec.eudi.rqesui.domain.extension.encodeToBase64

internal interface UiSerializer {
    fun <M : UiSerializable> toBase64(
        model: M,
        parser: UiSerializableParser
    ): String?

    fun <M : UiSerializable> fromBase64(
        payload: String?,
        model: Class<M>,
        parser: UiSerializableParser
    ): M?
}

internal class UiSerializerImpl : UiSerializer {

    override fun <M : UiSerializable> toBase64(
        model: M,
        parser: UiSerializableParser
    ): String? {
        return try {
            parser.provideParser().toJson(model).encodeToBase64()
        } catch (e: Exception) {
            null
        }
    }

    override fun <M : UiSerializable> fromBase64(
        payload: String?,
        model: Class<M>,
        parser: UiSerializableParser
    ): M? {
        return try {
            parser.provideParser().fromJson(
                payload?.decodeFromBase64(),
                model
            )
        } catch (e: Exception) {
            null
        }
    }
}