import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask
import java.io.BufferedReader
import java.io.FileReader
import java.time.Instant
import java.util.regex.Pattern

plugins {
    id("checkstyle")
    id("pmd")
    id("maven-publish")
    alias(libs.plugins.spotbugs)
    alias(libs.plugins.android.library)
}


// ----------------------------------------------------------------
// Constants
// ----------------------------------------------------------------
val cblGroup = "com.couchbase.lite"
val cblArtifactId = "couchbase-lite-android"
val cblDescription = "Couchbase Lite is an embedded lightweight, document-oriented (NoSQL), syncable database engine."
val cblLicense = "Couchbase, Inc. Community Edition License Agreement"
val cblLicenseUrl = "https://raw.githubusercontent.com/couchbase/product-texts/${getLicenseVersion()}/mobile/couchbase-lite/license/LICENSE_community.txt"
val cblProjectUrl = "https://github.com/couchbaselabs/couchbase-lite-java-ce-root"
val cblIssuesUrl = "https://github.com/couchbaselabs/couchbase-lite-java-ce-root/issues"
val cblSiteUrl = "https://developer.couchbase.com/mobile/"
val buildTime = Instant.now().toString()
val projectRootDir = "$projectDir/../../.."
val buildRelease = file("${projectRootDir}/version.txt").readText().trim()
val buildNumber = if (project.hasProperty("buildNumber") && project.property("buildNumber") != null) project.property("buildNumber") as String else "SNAPSHOT"
val buildVersion = "${buildRelease}-${buildNumber}"
val buildCommit = getBuildId()
val cblCommonRootDir = "${projectRootDir}/common"
val cblCommonDir = "${cblCommonRootDir}/common"
val cblCommonAndroidDir = "${cblCommonRootDir}/android"
val cblCommonEtcDir = "${cblCommonRootDir}/etc"
val cblCERootDir = "${projectRootDir}/ce"
val cblCECommonDir = "${cblCERootDir}/common"
val cblCEAndroidDir = "${cblCERootDir}/android"
val reportsDir = "${layout.buildDirectory.get()}/reports"
val myDependencies = listOf("okio", "okhttp")

if (!(buildVersion.matches(Regex("""^\d{1,2}\.\d{1,2}\.\d{1,2}([ab.]\d)?-.*""")))) {
    throw InvalidUserDataException("!!! Bad version: $buildVersion")
}

// comma separated list of annotations for tests that should not be run.
// e.g., -PtestFilter='com.couchbase.lite.internal.utils.SlowTest,com.couchbase.lite.internal.utils.VerySlowTest'
val testFilter = if (!project.hasProperty("testFilter")) null else (project.property("testFilter") as String).replace("\\s".toRegex(), "")

// Target repo for maven publish
val mavenUrl = if (!project.hasProperty("mavenUrl")) null else project.property("mavenUrl") as String


// ----------------------------------------------------------------
// Build
// ----------------------------------------------------------------

println("Building CBL Android ${buildVersion}@${buildCommit}")

group = cblGroup
version = buildVersion

androidComponents {
    onVariants(selector().withBuildType("debug")) {
        it.packaging.jniLibs.keepDebugSymbols.add("**/*so")
    }
}

android {
    namespace = "com.couchbase.lite"

    buildToolsVersion = libs.versions.buildTools.get()
    ndkVersion = libs.versions.ndk.get()
    compileSdk = libs.versions.compileSdk.get().toInt()

    externalNativeBuild {
        cmake {
            version = libs.versions.cmake.get()
            path = file("${cblCommonDir}/CMakeLists.txt")
        }
    }

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_11
        sourceCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures { buildConfig = true }

    defaultConfig {
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        aarMetadata {
            minCompileSdk = libs.versions.minSdk.get().toInt()
        }

        @Suppress("UnstableApiUsage")
        externalNativeBuild {
            cmake { targets("LiteCoreJNI") }
        }

        buildConfigField("String", "VERSION_NAME", "\"${buildVersion}\"")
        buildConfigField("String", "BUILD_TIME", "\"${buildTime}\"")
        buildConfigField("String", "BUILD_COMMIT", "\"${buildCommit}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        if (testFilter != null) {
            testInstrumentationRunnerArguments["notAnnotation"] = testFilter
        }

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        debug {
            ndk.debugSymbolLevel = "FULL"

            isMinifyEnabled = false

            @Suppress("UnstableApiUsage")
            externalNativeBuild {
                cmake {
                    arguments(
                        "-DANDROID_STL=c++_static",
                        "-DANDROID_TOOLCHAIN=clang",
                        "-DANDROID_PLATFORM=android-24",
                        "-DCMAKE_BUILD_TYPE=Debug"
                    )
                }
            }

            buildConfigField("boolean", "ENTERPRISE", "false")
            buildConfigField("boolean", "CBL_DEBUG", "true")
        }

        release {
            ndk.debugSymbolLevel = "SYMBOL_TABLE"

            isMinifyEnabled = false

            @Suppress("UnstableApiUsage")
            externalNativeBuild {
                cmake {
                    arguments(
                        "-DANDROID_STL=c++_static",
                        "-DANDROID_TOOLCHAIN=clang",
                        "-DANDROID_PLATFORM=android-24",
                        "-DCMAKE_BUILD_TYPE=RelWithDebInfo"
                    )
                }
            }

            buildConfigField("boolean", "ENTERPRISE", "false")
            buildConfigField("boolean", "CBL_DEBUG", "false")
        }
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("${cblCommonAndroidDir}/main/AndroidManifest.xml")
            java.directories.addAll(
                setOf(
                    "${cblCommonDir}/main/java",                   // Common
                    "${cblCommonAndroidDir}/main/java",           // Common Android
                    "${cblCECommonDir}/main/java",                // CE Common
                    "src/main/java"
                )
            )
            res.directories.add(
                "${cblCommonAndroidDir}/main/res"                // Common resources
            )
        }
        getByName("debug") {
            manifest.srcFile("${cblCommonAndroidDir}/debug/AndroidManifest.xml")
        }
        getByName("androidTest") {
            java.directories.addAll(
                setOf(
                    "${cblCommonDir}/test/java",                   // Common tests
                    "${cblCommonAndroidDir}/androidTest/java",    // Common Android tests
                    "${cblCECommonDir}/test/java",                // CE Common tests
                    "src/androidTest/java"                           // CE Android tests
                )
            )
            kotlin.directories.addAll(
                setOf(
                    "${cblCommonDir}/test/java",                   // Common tests
                    "${cblCommonAndroidDir}/androidTest/java",    // Common Android tests
                    "${cblCECommonDir}/test/java",                // CE Common tests
                    "src/androidTest/java"                           // CE Android tests
                )
            )
            assets.directories.addAll(
                setOf(
                    "${cblCommonDir}/test/assets",                 // Common assets
                    "${cblCECommonDir}/test/assets"               // CE Common assets
                )
            )
            res.directories.add(
                "${cblCommonAndroidDir}/androidTest/res"      // Common test resources
            )
        }
    }

    lint {
        disable += setOf("UseSparseArrays", "CustomX509TrustManager")
        abortOnError = false
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

    testImplementation(libs.kotlin.stdlib)
    testImplementation(libs.junit)

    androidTestImplementation(libs.kotlin.stdlib)

    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.rules)
}

// ----------------------------------------------------------------
// Documentation
// ----------------------------------------------------------------

tasks.register<Javadoc>("javadoc") {
    isFailOnError = false

    source(android.sourceSets.getByName("main").java.srcDirs)
    exclude("com/couchbase/lite/internal/**")

    options {
        title = "$cblLicense $buildVersion"
        memberLevel = JavadocMemberLevel.PROTECTED
        encoding = "UTF-8"
        this as StandardJavadocDocletOptions
        docEncoding = "UTF-8"
        charSet = "UTF-8"
        locale = "en_US"
        links("https://docs.oracle.com/en/java/javase/11/docs/api")
        addStringOption("Xdoclint:none", "-quiet")
        if (JavaVersion.current() > JavaVersion.VERSION_1_8) { addBooleanOption("-ignore-source-errors", true) }
    }
}

// ----------------------------------------------------------------
// Static analysis
// ----------------------------------------------------------------

val FILE_FILTER = listOf("**/R.class",
    "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*", "**/*Test*.*", "**/gen/**", "okhttp3/**")

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

    source(android.sourceSets.getByName("main").java.srcDirs)
    include("**/*.java")
    exclude(FILE_FILTER)

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

    ruleSetFiles = files("${cblCommonEtcDir}/pmd/pmd.xml")

    source(android.sourceSets.getByName("main").java.srcDirs)
    include("**/*.java")
    exclude(FILE_FILTER)

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
listOf("Html", "Xml").forEach { reportType ->
    tasks.register<SpotBugsTask>("spotbugs${reportType}") {
        description = "Spotbugs with $reportType report"
        group = "verification"

        effort = Effort.MAX
        reportLevel = Confidence.MEDIUM
        ignoreFailures = false

        sourceDirs = project.files(android.sourceSets.getByName("main").java.srcDirs)
        classDirs = fileTree("${layout.buildDirectory.get()}/intermediates/javac/debug/classes")

        excludeFilter = file("${cblCommonEtcDir}/spotbugs/spotbugs.xml")
        onlyAnalyze = listOf("com.couchbase.lite.-")

        reports {
            create("xml") {
                required = (reportType == "Xml")
                outputLocation = file("${reportsDir}/spotbugs.xml")
            }
            create("html") {
                required = (reportType == "Html")
                outputLocation = file("${reportsDir}/spotbugs.html")
                setStylesheet("fancy-hist.xsl")
            }
        }
    }
}

// ----------------------------------------------------------------
// Publication
// ----------------------------------------------------------------

val mavenRepoUser: String? = System.getenv("MAVEN_REPO_USR")
val mavenRepoPass: String? = System.getenv("MAVEN_REPO_PASS")

// Generate javadoc.jar
tasks.register<Jar>("javadocJar") {
    dependsOn(tasks.named("javadoc"))
    archiveBaseName = cblArtifactId
    archiveClassifier = "javadoc"
    from(tasks.named<Javadoc>("javadoc").map { it.destinationDir!! })
}

// Generate source.jar
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
    // Javadoc classpath — depends on AGP-generated tasks
    tasks.named<Javadoc>("javadoc") {
        dependsOn(
            tasks.named("generateReleaseBuildConfig"),
            tasks.named("generateReleaseRFile")
        )

        classpath += project.files(android.sourceSets.getByName("main").java.srcDirs.joinToString(File.pathSeparator))
        classpath += project.files("${layout.buildDirectory.get()}/generated/source/buildConfig/release")
        classpath += files(tasks.named<JavaCompile>("compileReleaseJavaWithJavac").map { it.classpath })
        classpath += files(androidComponents.sdkComponents.bootClasspath)
    }

    // SpotBugs classpath — depends on AGP-generated tasks
    listOf("Html", "Xml").forEach { reportType ->
        tasks.named<SpotBugsTask>("spotbugs${reportType}") {
            dependsOn(
                tasks.named("generateDebugRFile"),
                tasks.named("generateReleaseRFile"),
                tasks.named("compileDebugJavaWithJavac")
            )

            auxClassPaths.from(tasks.named<JavaCompile>("compileDebugJavaWithJavac").map { it.classpath })
            auxClassPaths.from(tasks.named<JavaCompile>("compileReleaseJavaWithJavac").map { it.classpath })
            auxClassPaths.from(files(androidComponents.sdkComponents.bootClasspath))
        }
    }

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

            create<MavenPublication>("libRelease") {
                groupId = cblGroup
                artifactId = cblArtifactId
                version = buildVersion

                artifact(tasks.named("bundleReleaseAar"))
                artifact(tasks.named("sourcesJar"))
                artifact(tasks.named("javadocJar"))

                configurePom()
            }
        }
    }

    // Task ordering (roughly last to first)
    tasks.withType<Javadoc>().configureEach { shouldRunAfter(tasks.named("connectedDebugAndroidTest")) }
    tasks.named("connectedDebugAndroidTest") { shouldRunAfter(tasks.named("smokeTest")) }
    tasks.named("testDebugUnitTest") { shouldRunAfter(tasks.named("smokeTest")) }
    tasks.withType<SpotBugsTask>().configureEach { shouldRunAfter(tasks.withType<Pmd>()) }
    tasks.withType<Pmd>().configureEach { shouldRunAfter(tasks.withType<Checkstyle>()) }
    tasks.withType<Checkstyle>().configureEach { shouldRunAfter(tasks.named("compileDebugSources")) }
}

// ----------------------------------------------------------------
// Tasks
// ----------------------------------------------------------------

tasks.register("smokeTest") {
    dependsOn("compileDebugSources", "checkstyle", "lint", "pmd", "spotbugsHtml")
}

// if there is a ce_android-ktx project: its dev tests will run
gradle.projectsEvaluated {
    if (!(rootProject.subprojects.any { it.name == "ce_android-ktx" })) {
        tasks.register("devTest") {
            dependsOn("connectedDebugAndroidTest")
        }
    }
}

tasks.register("ciCheck") {
    dependsOn("checkstyle", "lint", "pmd", "spotbugsXml", "test")
}
tasks.register("ciBuild") {
    dependsOn("assembleRelease")
}
tasks.register("ciPublish") {
    dependsOn("javadocJar", "generatePomFileForLibReleasePublication", "publishLibReleasePublicationToMavenRepository")
}

// Clean
// delete .cxx directory to force rerun of cmake.
// This is necessary because 'clean' deletes the file 'zconf.h' which is in
// .../build/intermediates, causing subsequent builds to fail.
tasks.named("clean") {
    doLast { delete("${projectDir}/.cxx") }
}

// ----------------------------------------------------------------
// Extensions
// ----------------------------------------------------------------

fun getLicenseVersion(): String = getCommit("product-texts", "master")

fun getBuildId(): String {
    val hostname = try { ProcessBuilder("hostname").start().inputStream.bufferedReader().readText().trim() } catch (_: Exception) { "rogue" }
    return "${getCommit("couchbase-lite-java-ee-root", "unofficial").substring(0, 10)}@${hostname}"
}

fun getCommit(project: String, dflt: String): String {
    var manifest: BufferedReader? = null
    return try {
        manifest = FileReader("${rootDir}/../../manifest.xml").buffered()
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