pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../../gradle/libs.versions.toml"))
        }
    }
}

// This project contains two distinct applications:
// 1) the CBL-Android-KTX source and the code to build, unit-test and publish it to maven
// 2) an independent application that runs automated tests on the application build in #1
//
// The two apps are mutually exclusive: the test app is built and run only by CI machines
// during the release process.  All other work in this repo will use only the "lib" module.

if (providers.gradleProperty("automatedTests").map { it.toBoolean() }.getOrElse(false)) {
    include(":test")
} else {
    include(":lib")
}

// Set includeCBLAndroidSource=true in gradle.properties to use ../android source instead of Maven.
val includeCBLAndroidSource = providers.gradleProperty("includeCBLAndroidSource")
    .map(String::toBoolean)
    .getOrElse(false)

if (includeCBLAndroidSource) {
    // Rename this build's :lib to avoid conflict with project(":lib") in the Android build.
    project(":lib").name = "android-ktx"

    includeBuild("../android") {
        name = "couchbase-lite-android-build"

        dependencySubstitution {
            substitute(module("com.couchbase.lite:couchbase-lite-android"))
                .using(project(":lib"))
        }
    }
}
