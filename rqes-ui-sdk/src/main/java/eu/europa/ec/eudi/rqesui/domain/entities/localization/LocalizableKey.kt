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
    Success,
    SuccessfullySignedDocument,
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
            Success -> "Success!"
            SuccessfullySignedDocument -> "You successfully signed your document"
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
            GenericErrorQtspNotFound -> "No selected QTSP found"
            GenericErrorCertificatesNotFound -> "No certificates found"
            SigningCertificate -> "Signing certificate"
            SigningCertificates -> "Signing certificates"
            SelectSigningCertificateSubtitle -> "Select a digital certificate to sign your document"
            Certificate -> "Certificate $ARGUMENTS_SEPARATOR"
        }
    }
}