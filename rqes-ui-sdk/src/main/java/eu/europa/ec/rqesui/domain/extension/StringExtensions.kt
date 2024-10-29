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

package eu.europa.ec.rqesui.domain.extension

import eu.europa.ec.rqesui.domain.entities.localization.LocalizableKey

/**
 * Formats a string by replacing placeholders with provided arguments.
 *
 * This function iterates through the provided arguments and replaces each occurrence of the `argSeparator`
 * in the original string with the corresponding argument. By default, the placeholder is `LocalizableKey.ARGUMENTS_SEPARATOR`.
 *
 * For example:
 *
 * ```kotlin
 * val template = "Hello, @arg! Welcome to @arg."
 * val formattedString = template.localizationFormatWithArgs(listOf("John", "Kotlin"))
 * // formattedString will be "Hello, John! Welcome to Kotlin."
 * ```
 *
 * You can also use a custom placeholder:
 *
 * ```kotlin
 * val template = "Hello, {name}! Welcome to {language}."
 * val formattedString = template.localizationFormatWithArgs(listOf("John", "Kotlin"), argSeparator = "{name}")
 * // formattedString will be "Hello, John! Welcome to {language}." // Only the first occurrence of "{name}" is replaced.
 * ```
 *
 * @param args A list of arguments to replace the placeholders with. Defaults to an empty list.
 * @param argSeparator The placeholder string to be replaced by the arguments. Defaults to `LocalizableKey.ARGUMENTS_SEPARATOR`.
 * @return The formatted string with placeholders replaced by arguments.
 */
fun String.localizationFormatWithArgs(
    args: List<String> = emptyList(),
    argSeparator: String = LocalizableKey.ARGUMENTS_SEPARATOR,
): String {
    return args.fold(this) { acc, arg ->
        acc.replaceFirst(oldValue = argSeparator, newValue = arg)
    }
}