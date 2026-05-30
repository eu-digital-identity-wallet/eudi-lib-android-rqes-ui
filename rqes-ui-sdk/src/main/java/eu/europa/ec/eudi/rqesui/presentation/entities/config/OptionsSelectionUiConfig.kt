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

package eu.europa.ec.eudi.rqesui.presentation.entities.config

import eu.europa.ec.eudi.rqesui.domain.serializer.UiSerializable
import eu.europa.ec.eudi.rqesui.domain.serializer.UiSerializableParser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface OptionsSelectionScreenState {

    @Serializable
    @SerialName("QtspSelection")
    data object QtspSelection : OptionsSelectionScreenState

    @Serializable
    @SerialName("CertificateSelection")
    data object CertificateSelection : OptionsSelectionScreenState
}

@Serializable
internal data class OptionsSelectionUiConfig(
    val optionsSelectionScreenState: OptionsSelectionScreenState,
) : UiSerializable {

    companion object Parser : UiSerializableParser {
        override val serializedKeyName = "optionsSelectionConfig"
    }
}