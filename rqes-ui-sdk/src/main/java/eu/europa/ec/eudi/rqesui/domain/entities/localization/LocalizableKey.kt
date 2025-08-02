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

package eu.europa.ec.eudi.rqesui.domain.entities.localization

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
    SelectDocumentSubtitle,
    SelectSigningService,
    SelectSigningServiceSubtitle,
    SelectServiceTitle,
    SelectServiceSubtitle,
    SelectCertificateSubtitle,
    SelectSigningCertificateTitle,
    SelectSigningCertificateSubtitle,
    SigningService,
    SigningCertificate,
    SigningCertificates,
    SuccessDescription,
    View,
    Close,
    Cancel,
    Continue,
    Done,
    Share,
    SharingDocument,
    CloseSharingMessage,
    GenericErrorButtonRetry,
    GenericErrorMessage,
    GenericServiceErrorMessage,
    GenericErrorDescription,
    GenericErrorDocumentNotFound,
    GenericErrorDocumentMultipleNotSupported,
    GenericErrorQtspNotFound,
    GenericErrorCertificatesNotFound,
    Certificate;

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
            SelectDocumentTitle -> "Document"
            SelectDocumentSubtitle -> "Choose a document from your device to sign electronically."
            SigningService -> "Signing service"
            SelectSigningService -> "Select signing service"
            SelectSigningServiceSubtitle -> "Remote Signing Service enables secure online document signing."
            SelectServiceTitle -> "Signing services"
            SelectServiceSubtitle -> "Select the Signing Service that will be used to issue a digital certificate"
            SelectSigningCertificateTitle -> "Select signing certificate"
            SelectCertificateSubtitle -> "The signing certificate is used to verify your identity and is linked to your electronic signature."
            SuccessDescription -> "You have successfully signed your document."
            View -> "VIEW"
            Close -> "Close"
            Cancel -> "Cancel"
            Continue -> "Continue"
            Done -> "Done"
            Share -> "Share"
            SharingDocument -> "Sharing document?"
            CloseSharingMessage -> "Closing will redirect you back to the dashboard without saving or sharing the document."
            GenericErrorButtonRetry -> "TRY AGAIN"
            GenericErrorMessage -> "Oups! Something went wrong"
            GenericServiceErrorMessage -> "It seems the RQES Signing Service is unavailable. Please try again later."
            GenericErrorDescription -> "If the issue persists, please contact customer support"
            GenericErrorDocumentNotFound -> "No Document data found"
            GenericErrorDocumentMultipleNotSupported -> "Currently, multiple documents are not supported"
            GenericErrorQtspNotFound -> "No selected QTSP found"
            GenericErrorCertificatesNotFound -> "No certificates found"
            SigningCertificate -> "Signing certificate"
            SigningCertificates -> "Signing certificates"
            SelectSigningCertificateSubtitle -> "Select a digital certificate to sign your document"
            Certificate -> "Certificate $ARGUMENTS_SEPARATOR"
        }
    }
}