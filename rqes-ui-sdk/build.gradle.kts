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

import com.vanniktech.maven.publish.AndroidMultiVariantLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.kotlin.kover)
    alias(libs.plugins.owasp.dependencycheck)
}

val NAMESPACE: String by project
val GROUP: String by project
val SDK_VERSION: String by project
val MIN_SDK_VERSION: String by project

val POM_SCM_URL: String by project

android {
    namespace = NAMESPACE
    group = GROUP
    compileSdk = Integer.parseInt(SDK_VERSION)

    defaultConfig {
        minSdk = Integer.parseInt(MIN_SDK_VERSION)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

dependencies {
    // RQES-Core
    api(libs.eudi.lib.android.rqes.core)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material3)

    // Material
    implementation(libs.material)

    // Misc
    implementation(libs.timber)
    implementation(libs.androidx.security)

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.ui.tooling)

    // Koin
    api(libs.koin.android)
    implementation(libs.koin.annotations)
    implementation(libs.koin.compose)
    ksp(libs.koin.ksp)

    //Gson
    implementation(libs.gson)

    // PDF
    implementation(libs.android.pdf.viewer)

    // Test Dependencies
    testImplementation(libs.junit)
    testImplementation(libs.koin.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.turbine)
}

// Compile time check
ksp {
    arg("KOIN_CONFIG_CHECK", "true")
}

mavenPublishing {
    configure(
        AndroidMultiVariantLibrary(
            sourcesJar = true,
            publishJavadocJar = true,
            setOf("release")
        )
    )
    pom {
        ciManagement {
            system = "github"
            url = "${POM_SCM_URL}/actions"
        }
    }
}

kover {
    reports {
        filters {
            excludes {
                packages(
                    "*.ksp.*",
                    "*.di",
                    "*.serializer",
                    "*.config",
                    "*.config.*",
                    "*.provider.*",
                    "*.provider",
                    "*.localization.*",
                    "*.localization",
                    "*.infrastructure.*",
                    "*.infrastructure",
                    "*.presentation.architecture.*",
                    "*.presentation.architecture",
                    "*.presentation.entities.*",
                    "*.presentation.entities",
                    "*.presentation.extension.*",
                    "*.presentation.extension",
                    "*.presentation.navigation.*",
                    "*.presentation.navigation",
                    "*.presentation.router.*",
                    "*.presentation.router",
                    "*.presentation.ui.component.*",
                    "*.presentation.ui.component",
                    "*.presentation.ui.container.*",
                    "*.presentation.ui.container",
                    "*.util.*",
                    "*.util",
                    "*.helper.*",
                    "*.helper",
                )
                classes(
                    "*LogController*",
                    "*Screen*",
                )
            }
        }
    }
}