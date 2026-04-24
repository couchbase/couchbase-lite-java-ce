//
// Copyright (c) 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024 Couchbase, Inc. All rights reserved.
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

rootProject.name = "couchbase-lite-java"

// This project contains two distinct applications:
// 1) the CBL-Java source and the code to build, unit-test and publish it to maven
// 2) an independent application that runs automated tests on the application build in #1
//
// The two apps are mutually exclusive: the test app is built and run only by CI machines
// during the release process.  All other work in this repo will use only the "lib" module.
if (gradle.startParameter.projectProperties["automatedTests"]?.toBoolean() != true) {
    include(":lib")
} else {
    include(":test")
}
