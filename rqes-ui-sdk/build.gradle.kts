/*
 * Copyright (c) 2026 European Commission
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

import com.android.build.api.dsl.LibraryExtension
import com.vanniktech.maven.publish.AndroidMultiVariantLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
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

group = GROUP

extensions.configure<LibraryExtension>("android") {
    namespace = NAMESPACE
    compileSdk = SDK_VERSION.toInt()

    defaultConfig {
        minSdk = MIN_SDK_VERSION.toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("release") {
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

    testOptions {
        unitTests.all {
            it.jvmArgs("-Duser.language=en", "-Duser.country=US")
        }
    }
}

extensions.configure<KotlinAndroidProjectExtension> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.withType<Test>().configureEach {
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    )
}

dependencies {
    api(libs.eudi.lib.android.rqes.core)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material3)

    implementation(libs.material)

    implementation(libs.timber)
    implementation(libs.androidx.security)

    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.ui.tooling)
    api(libs.androidx.compose.material.iconsExtended)

    api(libs.koin.android)
    implementation(libs.koin.compose)

    implementation(libs.gson)

    implementation(libs.android.pdf.viewer)

    testImplementation(libs.junit)
    testImplementation(libs.koin.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.turbine)
}

mavenPublishing {
    configure(
        AndroidMultiVariantLibrary(
            sourcesJar = SourcesJar.Sources(),
            javadocJar = JavadocJar.Empty(),
            includedBuildTypeValues = setOf("release")
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
        total {
            html { onCheck = false }
            xml { onCheck = false }
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
}