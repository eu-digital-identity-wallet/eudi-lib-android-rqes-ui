# EUDI Remote Qualified Electronic Signature (RQES) UI library for Android

:heavy_exclamation_mark: **Important!** Before you proceed, please read
the [EUDI Wallet Reference Implementation project description](https://github.com/eu-digital-identity-wallet/.github/blob/main/profile/reference-implementation.md)

----

## Table of contents

* [Overview](#overview)
* [Dependencies](#dependencies)
* [How to use](#how-to-use)
* [License](#license)

## Overview

This module provides the core and UI functionality for the EUDI Wallet, focusing on the Remote Qualified Electronic Signature (RQES) service. 
The `EudiRQESUi` object defines methods for setting up and using the SDK. The SDK offers compile-time configuration capabilities through the `EudiRQESUiConfig` interface.

## Requirements

- Android 9 (API level 28) or higher

### Dependencies

To use snapshot versions add the following to your project's settings.gradle file:

```kotlin
dependencyResolutionManagement {
    repositories {
        // .. other repositories
        maven {
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            mavenContent { snapshotsOnly() }
        }
    }
}
```

To include the library in your project, add the following dependencies to your app's build.gradle
file.

```kotlin
dependencies {
    implementation("eu.europa.ec.eudi:eudi-lib-android-rqes-ui:0.0.5-SNAPSHOT")
}
```

## How to use

TBD

## How to contribute

We welcome contributions to this project. To ensure that the process is smooth for everyone
involved, follow the guidelines found in [CONTRIBUTING.md](CONTRIBUTING.md).

## License

### Third-party component licenses

See [licenses.md](licenses.md) for details.

### License details

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
