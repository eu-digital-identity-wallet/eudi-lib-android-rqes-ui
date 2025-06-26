# EUDI Remote Qualified Electronic Signature (RQES) UI library for Android

:heavy_exclamation_mark: **Important!** Before you proceed, please read
the [EUDI Wallet Reference Implementation project description](https://github.com/eu-digital-identity-wallet/.github/blob/main/profile/reference-implementation.md)

----

## Table of contents

* [Overview](#overview)
* [Installation](#installation)
* [How to use](#how-to-use)
* [License](#license)

## Overview

This module provides the core and UI functionality for the EUDI Wallet, focusing on the Remote Qualified Electronic Signature (RQES) service. 
The `EudiRQESUi` object defines methods for setting up and using the SDK. The SDK offers compile-time configuration capabilities through the `EudiRQESUiConfig` interface.

## Requirements

- Android 10 (API level 29) or higher

## Installation

Add the following dependency to your app's build.gradle file to include the library in your project.

```Gradle
dependencies {
    implementation("eu.europa.ec.eudi:eudi-lib-android-rqes-ui:$version")
}
```

## How to use

### Configuration

Implement the `EudiRQESUiConfig` interface and supply all the necessary options for the SDK.

```kotlin
class RQESConfigImpl : EudiRQESUiConfig {

    // Optional. Default English translations will be used if not set.
    override val translations: Map<String, Map<LocalizableKey, String>> get()

    // Optional. Default theme will be used if not set.
    override val themeManager: ThemeManager get()

    override val qtsps: List<QtspData> get()

    // Optional. Default is false.
    override val printLogs: Boolean get()
            
    override val documentRetrievalConfig: DocumentRetrievalConfig get()
}
```

Example:

```kotlin
class RQESConfigImpl(val context: Context) : EudiRQESUiConfig {

    override val qtsps: List<QtspData>
        get() = listOf(
            QtspData(
                name = "your_name",
                endpoint = "your_endpoint".toUriOrEmpty(),
                scaUrl = "your_sca",
                clientId = "your_clientid",
                clientSecret = "your_secret",
                authFlowRedirectionURI = URI.create("your_registered_deeplink"),
                hashAlgorithm = HashAlgorithmOID.SHA_256,
            )
        )

    override val printLogs: Boolean get() = BuildConfig.DEBUG

    override val documentRetrievalConfig: DocumentRetrievalConfig
        get() = DocumentRetrievalConfig.X509Certificates(
            context = context,
            certificates = listOf(R.raw.my_certificate),
            shouldLog = should_log_option
        )
}
```

### Setup

#### oAuth

Register the `authFlowRedirectionURI` in your application's manifest to ensure the RQES Service can trigger your application.
It is the application's responsibility to retrieve the `code` query parameter from the deep link and pass it to the SDK to continue the flow.

```Xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />

    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />

    <data
        android:host="oauth"
        android:path="/callback"
        android:scheme="rqes://" />

</intent-filter>
```

Alternatively, you can use Android App Links [Google Documentation](https://developer.android.com/studio/write/app-link-indexing)

#### Document Retrieval (Same Device Scenario)

Register a deeplink in your application's manifest to allow the RQES Service to trigger your application.
It is the application's responsibility to retrieve the remote URL and pass it to the SDK to initialize the same-device document retrieval flow.

```Xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />

    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />

    <data
        android:host="your_host"
        android:scheme="your_scheme://" />

</intent-filter>
```

Initialize the SDK in your Application class by providing your application context, configuration, and, if you are using Koin for dependency injection, the KoinApplication.

```kotlin
EudiRQESUi.setup(
    application = application_context,
    config = rqes_config,
    koinApplication = koinapplication_if_applicable
)
```

### Initialization

#### Local file

Start the signing process by providing the context of your activity and the URI of the selected file.

```kotlin
EudiRQESUi.initiate(
    context = activity_context,
    documentUri = DocumentUri(file_uri)
)
```

#### Remote URL for document retrieval

Start the signing process by providing your activity context and the document retrieval service's remote URL (retrieved via deep link or QR code).

```kotlin
EudiRQESUi.initiate(
    context = activity_context,
    remoteUri = RemoteUri(remote_uri)
)
```

Resume the signing process once the `authFlowRedirectionURI` triggers your application following the PID presentation process. 
Provide your activity context and the extracted code from the `authFlowRedirectionURI` deep link.

```kotlin
EudiRQESUi.resume(
    context = activity_context,
    authorizationCode = code
)
```

## How to contribute

We welcome contributions to this project. To ensure that the process is smooth for everyone
involved, follow the guidelines found in [CONTRIBUTING.md](CONTRIBUTING.md).

## License

Copyright (c) 2024 European Commission

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
