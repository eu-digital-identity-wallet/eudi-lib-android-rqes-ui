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

package eu.europa.ec.rqesui.domain.entities.localization

/**
 * Represents keys for localized strings.
 * These keys are used to retrieve localized strings.
 */
enum class LocalizableKey {
    SignDocument,
    CancelSignProcessTitle,
    CancelSignProcessSubtitle,
    CancelSignProcessSecondaryText,
    CancelSignProcessPrimaryText,
    SelectDocumentTitle,
    SelectDocument,
    ConfirmSelectionTitle,
    SelectService,
    SelectServiceTitle,
    SelectServiceSubtitle,
    SelectCertificateTitle,
    SelectCertificateSubtitle,
    Success,
    SuccessfullySignedDocument,
    SignedBy,
    View,
    Close,
    Cancel,
    Save,
    SaveAndClose,
    Sign,
    GenericErrorButtonRetry,
    GenericErrorMessage,
    GenericErrorDescription,
    GenericErrorDocumentNotFound,
    GenericErrorQtspNotFound;

    companion object {
        const val ARGUMENTS_SEPARATOR = "@arg"
    }

    fun defaultTranslation(): String {
        return when (this) {
            SignDocument -> "Sign document"
            CancelSignProcessTitle -> "Cancel signing process?"
            CancelSignProcessSubtitle -> "Cancel will redirect you back to the documents list without signing your document"
            CancelSignProcessSecondaryText -> "Cancel signing"
            CancelSignProcessPrimaryText -> "Continue signing"
            SelectDocumentTitle -> "Select a document from your device to sign electronically."
            SelectDocument -> "Select document"
            ConfirmSelectionTitle -> "Please confirm signing of the following"
            SelectService -> "Select service"
            SelectServiceTitle -> "Select remote signing service."
            SelectServiceSubtitle -> "Remote signing enables you to digitally sign documents without the need for locally installed digital identities. Cloud-hosted signing service makes remote signing possible."
            SelectCertificateTitle -> "You have chosen to sign the following document:"
            SelectCertificateSubtitle -> "Please confirm signing with one of the following certificates:"
            Success -> "Success!"
            SuccessfullySignedDocument -> "You successfully signed your document"
            SignedBy -> "Signed by: $ARGUMENTS_SEPARATOR"
            View -> "VIEW"
            Close -> "Close"
            Cancel -> "Cancel"
            Save -> "Save"
            SaveAndClose -> "Save and close"
            Sign -> "Sign"
            GenericErrorButtonRetry -> "TRY AGAIN"
            GenericErrorMessage -> "Oups! Something went wrong"
            GenericErrorDescription -> "If the issue persists, please contact customer support"
            GenericErrorDocumentNotFound -> "No Document data found"
            GenericErrorQtspNotFound -> "No selected QTSP found"
        }
    }
}