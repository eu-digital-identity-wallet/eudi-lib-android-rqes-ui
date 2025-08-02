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

package eu.europa.ec.eudi.rqesui.util

const val mockedPlainFailureMessage = "Failure message"
const val mockedGenericErrorTitle = "resourceProvider's genericErrorTitle"
const val mockedGenericErrorMessage = "resourceProvider's genericErrorMessage"
const val mockedGenericServiceErrorMessage = "resourceProvider's genericServiceErrorMessage"
const val mockedAuthorizationUrl = "https://endpoint.com/mockedAuthorizationUrl"
const val mockedDocumentName = "Document.pdf"
const val mockedLocalizedText = "Localized text"
const val mockedPreferencesKey = "preferences_key"
const val mockedPreferenceStringValue = "preference value"
const val mockedDefaultPreferenceValue = "default value"
const val mockedQtspName = "mocked QTSP name"
const val mockedQtspNotFound = "mocked QTSP not found message"
const val mockedTsaUrl = ""
const val mockedClientId = "mocked_client_id"
const val mockedClientSecret = "mocked_client_secret"
const val mockedAuthorizationCode = "mockedAuthorizationCode"
const val mockedCertificatesNotFoundMessage = "No certificates found"
const val mockedCertificateName = "Certificate 1"
const val mockedUri = "https://mocked.uri.com"
const val mockedLocalFileUri = "content://mocked.provider/signed_pdfs/Document.pdf"
const val mockedInvalidUri = "mocked_invalid_uri_string"
const val mockedQtspEndpoint = "https://qtsp.endpoint.com"
const val mockedAuthorizationHttpsUrl = "https://rqes.com/authorization"
const val mockedDocumentNotFoundMessage = "mocked document not found message"
const val mockedFetchCertificatesFailureMessage = "mocked failed to fetch certificates message"
const val mockedSerializedConfig = "mockedSerializedConfig"

val mockedExceptionWithMessage = RuntimeException("Exception to test interactor.")
val mockedExceptionWithNoMessage = RuntimeException()