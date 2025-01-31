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

package eu.europa.ec.eudi.rqesui.presentation.entities.config

import eu.europa.ec.eudi.rqesui.domain.serializer.UiSerializable
import eu.europa.ec.eudi.rqesui.domain.serializer.UiSerializableParser

internal data class OptionsSelectionUiConfig(
    val optionsSelectionScreenState: String,
) : UiSerializable {
    companion object Parser : UiSerializableParser {
        override val serializedKeyName = "optionsSelectionConfig"
    }
}