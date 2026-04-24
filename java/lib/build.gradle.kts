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

import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask
import java.io.BufferedReader
import java.io.FileReader
import java.time.Instant
import java.util.regex.Pattern
import java.text.SimpleDateFormat
import java.util.Date
import org.apache.tools.ant.filters.ReplaceTokens


// ----------------------------------------------------------------
// Plugins
// ----------------------------------------------------------------

plugins {
    id("java-library")
    id("cpp")
    id("java-library-distribution")
    id("checkstyle")
    id("pmd")
    id("maven-publish")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spotbugs)
}


// ----------------------------------------------------------------
// Constants
// ----------------------------------------------------------------

val cblGroup = "com.couchbase.lite"
val jarArtifact = "couchbase-lite-java"
val cblDescription = "Couchbase Lite is an embedded lightweight, document-oriented (NoSQL), syncable database engine."
val cblLicense = "Couchbase, Inc. Community Edition License Agreement"
val cblLicenseUrl = "https://raw.githubusercontent.com/couchbase/product-texts/${getLicenseVersion()}/mobile/couchbase-lite/license/LICENSE_community.txt"
val cblProjectUrl = "https://github.com:couchbase/couchbase-lite-java-ce-root"
val cblIssuesUrl = "https://github.com/couchbase/couchbase-lite-java-ce-root/issues"
val cblSiteUrl = "https://developer.couchbase.com/mobile/"

val buildTime = Instant.now().toString()
val projectRootDir = "$projectDir/../../.."
val buildRelease = file("${projectRootDir}/version.txt").readText().trim()
val buildNumber = if (project.hasProperty("buildNumber") && project.property("buildNumber") != null) project.property("buildNumber") as String else "SNAPSHOT"
val buildVersion = "${buildRelease}-${buildNumber}"
val buildCommit = getBuildId()

val cblCommonRootDir = "${projectRootDir}/common"
val cblCommonDir = "${cblCommonRootDir}/common"
val cblCommonJavaDir = "${cblCommonRootDir}/java"
val cblCERootDir = "${projectRootDir}/ce"
val cblCECommonDir = "${cblCERootDir}/common"
val cblCommonEtcDir = "${cblCommonRootDir}/etc"

val generatedDir = "${layout.buildDirectory.get()}/generated/sources"
val reportsDir = "${layout.buildDirectory.get()}/reports"

val cblNativeDir = "${layout.buildDirectory.get()}/native"

val myDependencies = listOf("okio", "okhttp")

if (!(buildVersion.matches(Regex("""^\d{1,2}\.\d{1,2}\.\d{1,2}([ab.]\d)?-.*""")))) {
    throw InvalidUserDataException("!!! Bad version: $buildVersion")
}

// Build platform
val platform = when {
    org.gradle.internal.os.OperatingSystem.current().isWindows -> "windows"
    org.gradle.internal.os.OperatingSystem.current().isLinux -> "linux"
    org.gradle.internal.os.OperatingSystem.current().isMacOsX -> "macos"
    else -> "???"
}

// Comma separated list of annotations for tests that should not be run.
// e.g., -PtestFilter='com.couchbase.lite.internal.utils.SlowTest,com.couchbase.lite.internal.utils.VerySlowTest'
val testFilter = if (!project.hasProperty("testFilter")) null else (project.property("testFilter") as String).replace("\\s".toRegex(), "")

// Target repo for maven publish
val mavenUrl = if (!project.hasProperty("mavenUrl")) null else project.property("mavenUrl") as String

// Set -Pverbose to get full console logs for tests
val verbose = project.hasProperty("verbose")


// ----------------------------------------------------------------
// Build
// ----------------------------------------------------------------

println("Building CBL Java ${buildVersion}@${buildCommit}")

group = cblGroup
version = buildVersion

tasks.named<JavaCompile>("compileJava") { options.encoding = "UTF-8" }

java {
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8
}

sourceSets {
    main {
        java.setSrcDirs(listOf(
            "${cblCommonDir}/main/java",                   // Common
            "${cblCommonJavaDir}/main/java",               // Common Java
            "${cblCECommonDir}/main/java",                 // CE Common
            "src/main/java",
            "${generatedDir}/java"
        ))
        resources.setSrcDirs(listOf(
            "${cblCommonJavaDir}/main/resources",          // Resource files
            cblNativeDir                                   // Native Libraries
        ))
    }
    test {
        java.setSrcDirs(listOf(
            "${cblCommonDir}/test/java",                   // Common tests
            "${cblCommonJavaDir}/test/java",               // Common Java tests
            "${cblCECommonDir}/test/java"                  // CE Common tests
        ))
        resources.setSrcDirs(listOf(
            "${cblCommonDir}/test/assets"                  // Common test assets
        ))
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    //androidx.work:work-runtime:2.8.1 requires annotations 1.3.0
    compileOnly(libs.androidx.annotation)

    compileOnly(libs.spotbugs.annotations)

    implementation(libs.okhttp)

    testCompileOnly(libs.androidx.annotation)

    testImplementation(libs.kotlin.stdlib)
    testImplementation(libs.junit)
}

// Copy the version template into the source, inserting build info
tasks.register<Copy>("generateCBLVersion") {
    outputs.upToDateWhen { false }
    from("${cblCommonJavaDir}/templates/CBLVersion.java") {
        filter(
            mapOf("tokens" to mapOf(
                "VERSION" to buildVersion,
                "VARIANT" to "CE",
                "TYPE" to "release",
                "BUILD" to buildNumber,
                "COMMIT" to buildCommit
            )),
            ReplaceTokens::class.java
        )
    }
    into("${generatedDir}/java/com/couchbase/lite/internal/core")
}

tasks.named("compileJava") { dependsOn(tasks.named("generateCBLVersion")) }
tasks.named("compileKotlin") { dependsOn(tasks.named("generateCBLVersion")) }


// ----------------------------------------------------------------
// Build Native
// ----------------------------------------------------------------

// The Software Model (model{} block) cannot be used from Kotlin DSL in Gradle 9.x.
// Native build configuration is in native.gradle (Groovy DSL).
apply(from = "native.gradle")


// ----------------------------------------------------------------
// Javadoc
// ----------------------------------------------------------------

tasks.named<Javadoc>("javadoc") {
    isFailOnError = false

    exclude("**/internal/**")
    exclude("com/couchbase/lite/utils")

    options {
        this as StandardJavadocDocletOptions
        title = "$cblLicense $buildVersion"
        memberLevel = JavadocMemberLevel.PUBLIC
        docEncoding = "UTF-8"
        encoding = "UTF-8"
        charSet = "UTF-8"
        locale = "en_US"
        links("https://docs.oracle.com/en/java/javase/11/docs/api")
        addStringOption("Xdoclint:none", "-quiet")
        if (JavaVersion.current() > JavaVersion.VERSION_1_8) {
            addStringOption("-release", "8")
            addBooleanOption("-ignore-source-errors", true)
        }
    }
}


// ----------------------------------------------------------------
// Static analysis
// ----------------------------------------------------------------

val fileFilter = listOf("**/gen/**", "okhttp3/**", "org/json/**")

/////// Checkstyle
checkstyle {
    toolVersion = libs.versions.checkstyle.get()
    dependencies {
        checkstyle("com.puppycrawl.tools:checkstyle:${toolVersion}")
        checkstyle(libs.sevntu)
        checkstyle(files("${cblCommonEtcDir}/checkstyle/kotlin-checks-0.9.jar"))
    }

    configFile = file("${cblCommonEtcDir}/checkstyle/checkstyle.xml")
    configProperties["configDir"] = file("${cblCommonEtcDir}/checkstyle")

    isShowViolations = true
}
tasks.register<Checkstyle>("checkstyle") {
    description = "Checkstyle"
    group = "verification"
    dependsOn(tasks.named("generateCBLVersion"))

    source(sourceSets["main"].java.srcDirs)
    include("**/*.java")
    exclude(fileFilter)

    classpath = files()

    reports {
        xml.required = true
        xml.outputLocation = file("${reportsDir}/checkstyle.xml")
        html.required = true
        html.outputLocation = file("${reportsDir}/checkstyle.html")
    }
}

/////// PMD
pmd {
    toolVersion = libs.versions.pmd.get()
    ruleSets = listOf()
}
tasks.register<Pmd>("pmd") {
    description = "PMD"
    group = "verification"
    maxHeapSize = "2g"

    dependsOn(tasks.named("generateCBLVersion"))

    ruleSetFiles = files("${cblCommonEtcDir}/pmd/pmd.xml")

    source(sourceSets["main"].java.srcDirs)
    include("**/*.java")
    exclude(fileFilter)

    reports {
        xml.required = true
        xml.outputLocation = file("${reportsDir}/pmd.xml")
        html.required = true
        html.outputLocation = file("${reportsDir}/pmd.html")
    }
}

/////// SpotBugs
// If SpotBug is run on code generated with Java 11
// it will generate quite a few extraneous NULLCHECK warnings.
// Sadly the problem is Java 11 code generation: the only solution
// is to disable the check.
spotbugs { toolVersion = libs.versions.spotbugs.get() }
val spotbugsReportsDir = reportsDir
listOf("Html", "Xml").forEach { reportType ->
    tasks.register<SpotBugsTask>("spotbugs${reportType}") {
        description = "Spotbugs with $reportType report"
        group = "verification"

        dependsOn(tasks.named("compileJava"), tasks.named("processResources"))

        effort = Effort.MAX
        reportLevel = Confidence.MEDIUM
        ignoreFailures = false

        sourceDirs = project.files(sourceSets["main"].java.srcDirs)
        classDirs = fileTree(mapOf("dir" to "${layout.buildDirectory.get()}/classes/java/main"))
        auxClassPaths.from(tasks.named<JavaCompile>("compileJava").map { it.classpath })

        excludeFilter = file("${cblCommonEtcDir}/spotbugs/spotbugs.xml")
        onlyAnalyze = listOf("com.couchbase.lite.-")

        reports {
            create("xml") {
                required = (reportType == "Xml")
                outputLocation = file("${spotbugsReportsDir}/spotbugs.xml")
            }
            create("html") {
                required = (reportType == "Html")
                outputLocation = file("${spotbugsReportsDir}/spotbugs.html")
                setStylesheet("fancy-hist.xsl")
            }
        }
    }
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
    System.getenv("LD_LIBRARY_PATH")?.let { jvmArgs("-Djava.library.path=$it") }

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
// Jar
// ----------------------------------------------------------------

base { archivesName = jarArtifact }


// ----------------------------------------------------------------
// Publication
// ----------------------------------------------------------------

val mavenRepoUser: String? = System.getenv("MAVEN_REPO_USR")
val mavenRepoPass: String? = System.getenv("MAVEN_REPO_PSW")

// Generate javadoc.jar
val javadocJarTask = tasks.register<Jar>("javadocJar") {
    dependsOn(tasks.named("javadoc"))
    archiveBaseName = jarArtifact
    archiveClassifier = "javadoc"
    from(tasks.named<Javadoc>("javadoc").map { it.destinationDir!! })
}
tasks.named("distZip") { dependsOn(javadocJarTask) }
tasks.named("distTar") { dependsOn(javadocJarTask) }

// Generate sources.jar
tasks.register<Jar>("sourcesJar") {
    dependsOn(tasks.named("generateCBLVersion"))
    archiveBaseName = jarArtifact
    archiveClassifier = "sources"
    from(sourceSets["main"].java.srcDirs)
}

artifacts {
    add("archives", tasks.named("javadocJar"))
    add("archives", tasks.named("sourcesJar"))
}

tasks.withType<Tar>().configureEach { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
tasks.withType<Zip>().configureEach { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }

publishing {
    publications {
        create<MavenPublication>("couchbaseLiteJava") {
            val suffix = if (platform == "linux") "" else "-$platform"

            groupId = cblGroup
            artifactId = "$jarArtifact$suffix"
            version = buildVersion

            artifact(tasks.named("jar"))
            artifact(tasks.named("sourcesJar"))
            artifact(tasks.named("javadocJar"))

            pom {
                name = "$cblGroup:$jarArtifact"
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
                    configurations.getByName("implementation").allDependencies.forEach { dep ->
                        if (myDependencies.contains(dep.name)) {
                            val depNode = dependenciesNode.appendNode("dependency")
                            depNode.appendNode("groupId", dep.group)
                            depNode.appendNode("artifactId", dep.name)
                            depNode.appendNode("version", dep.version)
                        }
                    }
                }
            }
        }
    }

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


// ----------------------------------------------------------------
// Tasks
// ----------------------------------------------------------------

tasks.register("smokeTest") {
    dependsOn("clean", "compileJava", "checkstyle", "pmd", "spotbugsHtml", "testClasses")
}
tasks.register("devTest") { dependsOn("test") }
tasks.register("devPublish") { dependsOn("publishCouchbaseLiteJavaPublicationToMavenLocal") }

tasks.register("ciCheck") { dependsOn("compileJava", "checkstyle", "pmd", "spotbugsXml", "testClasses") }
tasks.register("ciBuild") { dependsOn("assemble") }
tasks.register("ciTest") { dependsOn("test") }
tasks.register("ciPublish") {
    dependsOn("generatePomFileForCouchbaseLiteJavaPublication", "publishCouchbaseLiteJavaPublicationToMavenRepository")
}

// Task ordering (roughly last to first)
tasks.withType<Javadoc>().configureEach { shouldRunAfter(tasks.named("test")) }
tasks.named("test") { shouldRunAfter(tasks.named("smokeTest")) }
tasks.named("testClasses") { shouldRunAfter(tasks.withType<SpotBugsTask>()) }
tasks.withType<SpotBugsTask>().configureEach { shouldRunAfter(tasks.withType<Pmd>()) }
tasks.withType<Pmd>().configureEach { shouldRunAfter(tasks.withType<Checkstyle>()) }
tasks.withType<Checkstyle>().configureEach { shouldRunAfter(tasks.named("compileJava")) }


// ----------------------------------------------------------------
// Extensions
// ----------------------------------------------------------------

fun getLicenseVersion(): String = getCommit("product-texts", "master")

fun getBuildId(): String {
    val hostname = try { ProcessBuilder("hostname").start().inputStream.bufferedReader().readText().trim() } catch (_: Exception) { "rogue" }
    return "${getCommit("couchbase-lite-java-ee-root", "unofficial").substring(0, 10)}@${hostname}"
}

fun getCommit(proj: String, dflt: String): String {
    var manifest: BufferedReader? = null
    return try {
        manifest = FileReader("${projectRootDir}/../manifest.xml").buffered()
        val revEx = Pattern.compile("<project.* name=\"${proj}\".* revision=\"([\\dabcdef]{40})\"")
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
