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

import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.BufferedReader
import java.io.FileReader
import java.time.Instant
import java.util.regex.Pattern

plugins {
    id("maven-publish")
    alias(libs.plugins.android.library)
    alias(libs.plugins.dokka)
}


// ----------------------------------------------------------------
// Constants
// ----------------------------------------------------------------

val cblGroup = "com.couchbase.lite"
val cblArtifactId = "couchbase-lite-android-ktx"
val cblDescription = "Couchbase Lite is an embedded lightweight, document-oriented (NoSQL), syncable database engine."
val cblLicense = "Couchbase, Inc. Community Edition License Agreement"
val cblLicenseUrl = "https://raw.githubusercontent.com/couchbase/product-texts/${getLicenseVersion()}/mobile/couchbase-lite/license/LICENSE_community.txt"
val cblProjectUrl = "https://github.com:couchbase/couchbase-lite-java-ce-root.git"
val cblIssuesUrl = "https://github.com/couchbase/couchbase-lite-java-ce-root/issues"
val cblSiteUrl = "https://developer.couchbase.com/mobile/"
val cblAndroidLib = "couchbase-lite-android"
val cblAndroidType = "aar"
val buildTime = Instant.now().toString()
val buildRelease = file("${projectDir}/../../..").resolve("version.txt").readText().trim()
val buildNumber = if (project.hasProperty("buildNumber") && project.property("buildNumber") != null) project.property("buildNumber") as String else "SNAPSHOT"
val buildVersion = "${buildRelease}-${buildNumber}"
val buildCommit = getBuildId()
val projectRootDir = "${projectDir}/../../.."
val cblCommonRootDir = "${projectRootDir}/common"
val cblCommonDir = "${cblCommonRootDir}/common"
val cblCommonAndroidDir = "${cblCommonRootDir}/android"
val cblCommonAndroidKtxDir = "${cblCommonRootDir}/android-ktx"
val cblCERootDir = "${projectRootDir}/ce"
val cblCECommonDir = "${cblCERootDir}/common"
val cblCEAndroidDir = "${cblCERootDir}/android"

if (!buildVersion.matches(Regex("""^\d{1,2}\.\d{1,2}\.\d{1,2}([ab.]\d)?-.*"""))) {
    throw InvalidUserDataException("!!! Bad version: $buildVersion")
}

val testFilter = if (!project.hasProperty("testFilter")) null
    else (project.property("testFilter") as String).replace("\\s".toRegex(), "")
val mavenUrl = if (!project.hasProperty("mavenUrl")) null else project.property("mavenUrl") as String


// ----------------------------------------------------------------
// Build
// ----------------------------------------------------------------

println("Building CBL Android KTX ${buildVersion}@${buildCommit} with CBL ${cblAndroidLib}:${buildVersion}")

group = cblGroup
version = buildVersion

android {
    namespace = "com.couchbase.lite.kotlin"

    buildToolsVersion = libs.versions.buildTools.get()
    compileSdk = libs.versions.compileSdk.get().toInt()

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_11
        sourceCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    packaging {
        resources.excludes += "META-INF/library_release.kotlin_module"
    }

    buildFeatures { buildConfig = true }

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        aarMetadata {
            minCompileSdk = libs.versions.minSdk.get().toInt()
        }

        buildConfigField("String", "VERSION_NAME", "\"${buildVersion}\"")
        buildConfigField("String", "BUILD_TIME", "\"${buildTime}\"")
        buildConfigField("String", "BUILD_COMMIT", "\"${buildCommit}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        if (testFilter != null) { testInstrumentationRunnerArguments["notAnnotation"] = testFilter }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            buildConfigField("boolean", "ENTERPRISE", "false")
            buildConfigField("boolean", "CBL_DEBUG", "true")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("boolean", "ENTERPRISE", "false")
            buildConfigField("boolean", "CBL_DEBUG", "false")
        }
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("${cblCommonAndroidKtxDir}/main/AndroidManifest.xml")
            kotlin.directories.addAll(setOf(
                "${cblCommonDir}/main/kotlin",
                "${cblCommonAndroidKtxDir}/main/kotlin",
                "${cblCECommonDir}/main/kotlin",
                "src/main/kotlin"
            ))
            res.directories.add("${cblCommonAndroidKtxDir}/main/res")
        }
        getByName("debug") {
            manifest.srcFile("${cblCommonAndroidKtxDir}/debug/AndroidManifest.xml")
        }
        getByName("androidTest") {
            java.directories.addAll(setOf(
                "${cblCommonDir}/test/java",
                "${cblCommonAndroidDir}/androidTest/java"
            ))
            kotlin.directories.addAll(setOf(
                "${cblCommonDir}/test/java",
                "${cblCommonAndroidDir}/androidTest/java",
                "${cblCommonDir}/test/kotlin",
                "${cblCECommonDir}/test/kotlin",
                "${cblCommonAndroidKtxDir}/androidTest/kotlin",
                "src/androidTest/kotlin"
            ))
            assets.directories.add("${cblCommonDir}/test/assets")
            res.directories.add("${cblCommonAndroidKtxDir}/androidTest/res")
        }
    }
}

repositories {
    if (buildVersion.endsWith("SNAPSHOT")) { mavenLocal() }
    else if (buildVersion.matches(Regex(""".*-\d+$"""))) {
        maven {
            url = uri("https://proget.sc.couchbase.com/maven2/cimaven/")
            content { includeGroupByRegex("com\\.couchbase\\.lite.*") }
        }
    } else {
        maven {
            url = uri("https://mobile.maven.couchbase.com/maven2/dev/")
            content { includeGroupByRegex("com\\.couchbase\\.lite.*") }
        }
    }

    google()
    mavenCentral()
}

dependencies {
    compileOnly(libs.androidx.annotation)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.work.runtime.ktx)
    implementation("com.couchbase.lite:${cblAndroidLib}:${buildVersion}")

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.work.testing)
}


// ----------------------------------------------------------------
// Publication
// ----------------------------------------------------------------

val mavenRepoUser: String? = System.getenv("MAVEN_REPO_USR")
val mavenRepoPass: String? = System.getenv("MAVEN_REPO_PASS")

tasks.register<Jar>("dokkaJar") {
    dependsOn(tasks.named("dokkaJavadoc"))
    archiveBaseName = cblArtifactId
    archiveClassifier = "javadoc"
    from(tasks.named<DokkaTask>("dokkaJavadoc").flatMap { it.outputDirectory })
}

tasks.register<Jar>("sourcesJar") {
    archiveBaseName = cblArtifactId
    archiveClassifier = "sources"
    from(android.sourceSets.getByName("main").java.srcDirs)
}

publishing {
    repositories {
        if (mavenUrl != null) {
            maven {
                url = uri(mavenUrl)
                credentials {
                    username = mavenRepoUser
                    password = mavenRepoPass
                }
            }
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            fun MavenPublication.configurePom() {
                pom {
                    name = "${cblGroup}:${cblArtifactId}"
                    description = cblDescription
                    url = cblSiteUrl
                    licenses {
                        license {
                            name = cblLicense
                            url = cblLicenseUrl
                            distribution = "repo"
                        }
                    }
                    developers {
                        developer {
                            name = "Couchbase Mobile"
                            email = "mobile@couchbase.com"
                            organization = "Couchbase"
                            organizationUrl = "https://www.couchbase.com"
                        }
                    }
                    issueManagement {
                        system = "github"
                        url = cblIssuesUrl
                    }
                    scm {
                        url = cblSiteUrl
                        connection = cblProjectUrl
                        developerConnection = cblProjectUrl
                    }
                    withXml {
                        val dependenciesNode = asNode().appendNode("dependencies")
                        val dep = dependenciesNode.appendNode("dependency")
                        dep.appendNode("groupId", cblGroup)
                        dep.appendNode("artifactId", cblAndroidLib)
                        dep.appendNode("version", buildVersion)
                        dep.appendNode("type", cblAndroidType)
                    }
                }
            }

            create<MavenPublication>("libRelease") {
                groupId = cblGroup
                artifactId = cblArtifactId
                version = buildVersion
                artifact(tasks.named("bundleReleaseAar"))
                artifact(tasks.named("sourcesJar"))
                artifact(tasks.named("dokkaJar"))
                configurePom()
            }

            create<MavenPublication>("libDebug") {
                groupId = cblGroup
                artifactId = cblArtifactId
                version = buildVersion
                artifact(tasks.named("bundleDebugAar"))
                artifact(tasks.named("sourcesJar"))
                artifact(tasks.named("dokkaJar"))
                configurePom()
            }
        }
    }

    tasks.named("connectedDebugAndroidTest") { shouldRunAfter(tasks.named("smokeTest")) }
    tasks.named("testDebugUnitTest") { shouldRunAfter(tasks.named("smokeTest")) }
}


// ----------------------------------------------------------------
// Tasks
// ----------------------------------------------------------------

gradle.projectsEvaluated {
    rootProject.subprojects.forEach { p ->
        if (p.name == "ce_android") {
            val publishTask = p.tasks.findByName("devPublish")
            if (publishTask != null) {
                val preBuildTask = tasks.findByName("preBuild")
                preBuildTask?.dependsOn(publishTask)
                preBuildTask?.mustRunAfter(publishTask)
            }
        }
    }
}

tasks.register("smokeTest") {
    dependsOn("clean", "compileDebugSources", "compileDebugAndroidTestSources", "compileDebugUnitTestSources", "lint")
}
tasks.register("devTest") { dependsOn("connectedDebugAndroidTest") }
tasks.register("devPublish") { dependsOn("publishLibDebugPublicationToMavenLocal") }
tasks.register("ciBuild") { dependsOn("assembleRelease") }
tasks.register("ciPublish") { dependsOn("generatePomFileForLibReleasePublication", "publishLibReleasePublicationToMavenRepository") }


// ----------------------------------------------------------------
// Extensions
// ----------------------------------------------------------------

fun getLicenseVersion(): String = getCommit("product-texts", "master")

fun getBuildId(): String {
    val hostname = try { ProcessBuilder("hostname").start().inputStream.bufferedReader().readText().trim() } catch (_: Exception) { "rogue" }
    return "${getCommit("couchbase-lite-java-ce-root", "unofficial").substring(0, 10)}@${hostname}"
}

fun getCommit(project: String, dflt: String): String {
    var manifest: BufferedReader? = null
    return try {
        manifest = FileReader("${projectDir}/../../../manifest.xml").buffered()
        val revEx = Pattern.compile("<project.* name=\"${project}\".* revision=\"([\\dabcdef]{40})\"")
        var l: String?
        while (manifest.readLine().also { l = it } != null) {
            val m = revEx.matcher(l)
            if (m.find()) return m.group(1)
        }
        dflt
    } catch (_: Exception) {
        dflt
    } finally {
        try { manifest?.close() } catch (_: Exception) { }
    }
}
