//
// Copyright (c) 2017, 2018, 2019, 2024 Couchbase, Inc. All rights reserved.
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

import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
}


// ----------------------------------------------------------------
// Constants
// ----------------------------------------------------------------

val cblGroup = "com.couchbase.lite"

val projectRootDir = "$projectDir/../../.."
val buildRelease = file("${projectRootDir}/version.txt").readText().trim()
val buildNumber = if (project.hasProperty("buildNumber") && project.property("buildNumber") != null) project.property("buildNumber") as String else "SNAPSHOT"

// if validating a release, we'll use the released bits; normally use a local build
val buildVersion = if (buildNumber == "RELEASE") buildRelease else "${buildRelease}-${buildNumber}"

val cblCommonRootDir = "${projectRootDir}/common"
val cblCommonDir = "${cblCommonRootDir}/common"
val cblCommonJavaDir = "${cblCommonRootDir}/java"
val cblCERootDir = "${projectRootDir}/ce"
val cblCECommonDir = "${cblCERootDir}/common"
val cblCEJavaDir = "${cblCERootDir}/java"

val reportsDir = "${layout.buildDirectory.get()}/reports"
val cblNativeDir = "${layout.buildDirectory.get()}/native"
val cblJavaLib = "couchbase-lite-java"

// This module is for testing on Jenkins. Require a real build number.
if (!(buildVersion.matches(Regex("""\d{1,2}\.\d{1,2}\.\d{1,2}([ab.]\d)?-\d{1,5}?""")))) {
    throw InvalidUserDataException("!!! Bad version: $buildVersion")
}

// This module is for testing on Jenkins. Require a build number.
if (buildNumber == "SNAPSHOT") { throw InvalidUserDataException("!!! A build number is required") }

// Comma separated list of annotations for tests that should not be run.
// e.g., -PtestFilter='com.couchbase.lite.internal.utils.SlowTest,com.couchbase.lite.internal.utils.VerySlowTest'
val testFilter = if (!project.hasProperty("testFilter")) null else (project.property("testFilter") as String).replace("\\s".toRegex(), "")

// Set -Pverbose to get full console logs for tests
val verbose = project.hasProperty("verbose")


// ----------------------------------------------------------------
// Build
// ----------------------------------------------------------------

println("Testing CBL Java ${buildVersion}")

group = cblGroup
version = buildVersion

tasks.named<JavaCompile>("compileJava") { options.encoding = "UTF-8" }

java {
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8
}

sourceSets {
    main {
        resources.setSrcDirs(listOf(
            "${cblCommonJavaDir}/main/resources",          // Resource files
            cblNativeDir                                   // Native Libraries
        ))
    }
    test {
        java.setSrcDirs(listOf(
            "${cblCommonDir}/test/java",                   // Common tests
            "${cblCommonJavaDir}/test/java",               // Common Java tests
            "${cblCECommonDir}/test/java",                 // CE Common tests
            "${cblCEJavaDir}/lib/src/test/java"            // CE Java tests
        ))
        resources.setSrcDirs(listOf(
            "${cblCommonDir}/test/assets"                  // Common test assets
        ))
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
    testCompileOnly(libs.androidx.annotation)

    testImplementation("com.couchbase.lite:${cblJavaLib}:${buildVersion}")

    testImplementation(libs.kotlin.stdlib)
    testImplementation(libs.junit)
}


// ----------------------------------------------------------------
// Testing
// ----------------------------------------------------------------

tasks.named<JavaCompile>("compileTestJava") { options.encoding = "UTF-8" }

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    @Suppress("DEPRECATION")
    kotlinOptions.jvmTarget = "1.8"
}

kotlin { kotlinDaemonJvmArgs = listOf("-Xmx3072m", "-Xms512m") }

tasks.named<Test>("test") {
    workingDir = File(workingDir, ".test-" + SimpleDateFormat("yyMMddHHmm").format(Date()))
    if (!workingDir.exists()) workingDir.mkdirs()

    if (testFilter != null) { exclude(testFilter) }

    testLogging {
        outputs.upToDateWhen { false }

        events = setOf(
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
        )

        showStandardStreams = verbose

        showCauses = true
        showExceptions = true
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}


// ----------------------------------------------------------------
// Tasks
// ----------------------------------------------------------------

// This target requires setting the property "automatedTests" true.
// See settings.gradle for explanation.
tasks.register("ciTest") { dependsOn("test") }
