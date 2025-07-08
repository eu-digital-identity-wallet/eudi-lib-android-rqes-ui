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