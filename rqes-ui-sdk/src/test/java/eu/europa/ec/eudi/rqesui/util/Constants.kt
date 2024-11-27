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

package eu.europa.ec.eudi.rqesui.util

import eu.europa.ec.eudi.rqesui.domain.extension.toUri
import java.net.URI

const val mockedPlainFailureMessage = "Failure message"
const val mockedGenericErrorMessage = "resourceProvider's genericErrorMessage"
const val mockedAuthorizationUrl = "https://endpoint.com/mockedAuthorizationUrl"
const val mockedDocumentName = "Document.pdf"
const val mockedLocalizedText = "Localized text"
const val mockedPreferencesKey = "preferences_key"
const val mockedPreferenceStringValue = "preference value"
const val mockedQtspName = "mocked QTSP name"
const val mockedScaUrl = "https://mocked.qtsp.sca.com"
const val mockedClientId = "mocked_client_id"
const val mockedClientSecret = "mocked_client_secret"
const val mockedAuthorizationCode = "mockedAuthorizationCode"

val mockedExceptionWithMessage = RuntimeException("Exception to test interactor.")
val mockedExceptionWithNoMessage = RuntimeException()
val mockedUri = URI.create("https://mocked.uri.com")
val mockedQtspEndpoint = "https://qtsp.endpoint.com".toUri()