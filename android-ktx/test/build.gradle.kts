//
// Copyright (c) 2026 Couchbase, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// Please try to keep this build file as similar to the other family build files
// as is possible.
//


// ----------------------------------------------------------------
// Plugins
// ----------------------------------------------------------------

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}


// ----------------------------------------------------------------
// Constants
// ----------------------------------------------------------------

val cblGroup = "com.couchbase.lite"
val cblAndroidLib = "couchbase-lite-android-ktx"

val projectRootDir = "${projectDir}/../../.."
val buildRelease = file("${projectRootDir}/version.txt").readText().trim()
val buildNumber = if (project.hasProperty("buildNumber") && project.property("buildNumber") != null)
    project.property("buildNumber") as String else "SNAPSHOT"

// if validating a release, we'll use the released bits; normally use a local build
val buildVersion = if (buildNumber == "RELEASE") buildRelease else "${buildRelease}-${buildNumber}"

val cblCommonRootDir = "${projectRootDir}/common"
val cblCommonDir = "${cblCommonRootDir}/common"
val cblCommonAndroidDir = "${cblCommonRootDir}/android"
val cblCommonAndroidKtxDir = "${cblCommonRootDir}/android-ktx"
val cblCERootDir = "${projectRootDir}/ce"
val cblCECommonDir = "${cblCERootDir}/common"
val cblCEAndroidDir = "${cblCERootDir}/android"

// This module is for testing on Jenkins. Require a real build number.
if (!buildVersion.matches(Regex("""\d{1,2}\.\d{1,2}\.\d{1,2}([ab.]\d)?-\d{1,5}"""))) {
    throw InvalidUserDataException("!!! Bad version: $buildVersion")
}

// Comma separated list of annotations for tests that should not be run.
// e.g., -PtestFilter='com.couchbase.lite.internal.utils.SlowTest,com.couchbase.lite.internal.utils.VerySlowTest'
val testFilter = if (!project.hasProperty("testFilter")) null
    else (project.property("testFilter") as String).replace("\\s".toRegex(), "")


// ----------------------------------------------------------------
// Build
// ----------------------------------------------------------------

println("Testing CBL Android KTX $buildVersion")

group = cblGroup
version = buildVersion

android {
    namespace = "com.couchbase.lite.kotlin.test"

    buildToolsVersion = libs.versions.buildTools.get()
    compileSdk = libs.versions.compileSdk.get().toInt()

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_11
        sourceCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions { jvmTarget = "11" }

    packaging {
        resources.excludes += "META-INF/library_release.kotlin_module"
    }

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        if (testFilter != null) { testInstrumentationRunnerArguments["notAnnotation"] = testFilter }
    }

    buildTypes {
        debug { isMinifyEnabled = false }
        release { isMinifyEnabled = false }
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("${cblCommonAndroidKtxDir}/main/AndroidManifest.xml")
            kotlin.setSrcDirs(setOf(
                "${cblCommonDir}/main/kotlin",
                "${cblCommonAndroidKtxDir}/main/kotlin",
                "${cblCECommonDir}/main/kotlin",
                "${projectDir}/../lib/src/main/kotlin"
            ))
            res.setSrcDirs(setOf("${cblCommonAndroidKtxDir}/main/res"))
        }
        getByName("debug") {
            manifest.srcFile("${cblCommonAndroidKtxDir}/debug/AndroidManifest.xml")
        }
        getByName("androidTest") {
            manifest.srcFile("${cblCommonAndroidKtxDir}/debug/AndroidManifest.xml")
            java.setSrcDirs(setOf(
                "${cblCommonDir}/test/java",
                "${cblCommonAndroidDir}/androidTest/java",
                "${cblCECommonDir}/test/java",
                "${cblCEAndroidDir}/lib/src/androidTest/java"
            ))
            kotlin.setSrcDirs(setOf(
                "${cblCommonDir}/test/java",
                "${cblCommonAndroidDir}/androidTest/java",
                "${cblCommonDir}/test/kotlin",
                "${cblCECommonDir}/test/java",
                "${cblCECommonDir}/test/kotlin",
                "${cblCEAndroidDir}/lib/src/androidTest/java",
                "${cblCommonAndroidKtxDir}/androidTest/kotlin"
            ))
            assets.setSrcDirs(setOf("${cblCommonDir}/test/assets"))
            res.setSrcDirs(setOf("${cblCommonAndroidKtxDir}/androidTest/res"))
        }
    }
}

repositories {
    if (buildVersion.matches(Regex(""".*-\d+$"""))) {
        maven {
            // Test a release candidate
            url = uri("https://proget.sc.couchbase.com/maven2/cimaven/")
            content { includeGroupByRegex("com\\.couchbase\\.lite.*") }
        }
    } else {
        maven {
            // Validate a release
            url = uri("https://mobile.maven.couchbase.com/maven2/dev/")
            content { includeGroupByRegex("com\\.couchbase\\.lite.*") }
        }
    }

    google()
    mavenCentral()
}

dependencies {
    //androidx.work:work-runtime:2.8.1 requires annotations 1.3.0
    compileOnly(libs.androidx.annotation)

    implementation(libs.kotlinx.coroutines.core)

    // Work Manager support
    implementation(libs.androidx.work.runtime.ktx)

    implementation("com.couchbase.lite:${cblAndroidLib}:${buildVersion}")

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.rules)

    // Work Manager Test support
    androidTestImplementation(libs.androidx.work.testing)
}


// ----------------------------------------------------------------
// Tasks
// ----------------------------------------------------------------

// This target requires setting the property "automatedTests" true.
// See settings.gradle for explanation.
tasks.register("ciTest") { dependsOn("connectedDebugAndroidTest") }
