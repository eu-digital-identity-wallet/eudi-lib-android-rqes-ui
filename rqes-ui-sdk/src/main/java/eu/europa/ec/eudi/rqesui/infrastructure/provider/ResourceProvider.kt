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

package eu.europa.ec.eudi.rqesui.infrastructure.provider

import android.content.ContentResolver
import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import eu.europa.ec.eudi.rqesui.domain.controller.LocalizationController
import eu.europa.ec.eudi.rqesui.domain.entities.localization.LocalizableKey
import java.io.File

internal interface ResourceProvider {
    fun provideContext(): Context
    fun provideContentResolver(): ContentResolver
    fun getString(@StringRes resId: Int): String
    fun getStringFromRaw(@RawRes resId: Int): String
    fun getQuantityString(@PluralsRes resId: Int, quantity: Int, vararg formatArgs: Any): String
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String
    fun genericErrorTitle(): String
    fun genericErrorMessage(): String
    fun genericServiceErrorMessage(): String
    fun getLocalizedString(localizableKey: LocalizableKey, args: List<String> = emptyList()): String
    fun getSignedDocumentsCache(): File

    fun getDownloadsCache(): File
}

internal class ResourceProviderImpl(
    private val context: Context,
    private val localizationController: LocalizationController
) : ResourceProvider {

    override fun provideContext() = context

    override fun provideContentResolver(): ContentResolver = context.contentResolver

    override fun genericErrorTitle(): String =
        getLocalizedString(LocalizableKey.GenericErrorMessage)

    override fun genericErrorMessage() = getLocalizedString(LocalizableKey.GenericErrorDescription)

    override fun genericServiceErrorMessage() =
        getLocalizedString(LocalizableKey.GenericServiceErrorMessage)

    override fun getString(@StringRes resId: Int): String =
        try {
            context.getString(resId)
        } catch (_: Exception) {
            ""
        }

    override fun getStringFromRaw(@RawRes resId: Int): String =
        try {
            context.resources.openRawResource(resId).bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            ""
        }

    override fun getQuantityString(
        @PluralsRes resId: Int,
        quantity: Int,
        vararg formatArgs: Any
    ): String =
        try {
            context.resources.getQuantityString(resId, quantity, *formatArgs)
        } catch (e: Exception) {
            ""
        }

    override fun getString(resId: Int, vararg formatArgs: Any): String =
        try {
            context.getString(resId, *formatArgs)
        } catch (_: Exception) {
            ""
        }

    override fun getLocalizedString(localizableKey: LocalizableKey, args: List<String>): String {
        return localizationController.get(localizableKey, args)
    }

    override fun getSignedDocumentsCache(): File = File(
        context.cacheDir,
        "signed_pdfs"
    ).apply {
        mkdirs()
    }

    override fun getDownloadsCache(): File =
        File(context.cacheDir, "downloads").apply { mkdirs() }
}