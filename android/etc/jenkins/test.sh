#!/bin/bash
#
# Test Couchbase Lite Android, Enterprise Edition
#

# These versions must match the versions in lib/build.gradle
NDK_VERSION='21.3.6528147'
CMAKE_VERSION='3.10.2.4988404'
BUILD_TOOLS_VERSION='30.0.2'


function usage() {
    echo "Usage: $0 <sdk path> <build number> <reports dir>"
    exit 1
}

if [ "$#" -ne 3 ]; then
    usage
fi

SDK_HOME="$1"
if [ -z "$SDK_HOME" ]; then
    usage
fi

BUILD_NUMBER="$2"
if [ -z "$BUILD_NUMBER" ]; then
    usage
fi

REPORTS="$3"
if [ -z "REPORTS" ]; then
    usage
fi

SDK_MGR="${SDK_HOME}/cmdline-tools/latest/bin/sdkmanager --channel=1 --install"

echo "======== TEST Couchbase Lite Android, Community Edition v`cat ../../version.txt`-${BUILD_NUMBER} on device: ${ANDROID_SERIAL}"

echo "======== Install Toolchain"
yes | ${SDK_MGR} --licenses > /dev/null 2>&1
${SDK_MGR} "build-tools;${BUILD_TOOLS_VERSION}"
${SDK_MGR} "cmake;${CMAKE_VERSION}"
${SDK_MGR} "ndk;${NDK_VERSION}"

cat <<EOF >> local.properties
sdk.dir=${SDK_HOME}
ndk.dir=${SDK_HOME}/ndk/${NDK_VERSION}
cmake.dir=${SDK_HOME}/cmake/${CMAKE_VERSION}
EOF

echo "======== Test"
./gradlew ciTest --info --console=plain -PautomatedTests=true -PbuildNumber="${BUILD_NUMBER}"

echo "======== Publish reports"
pushd test/build/reports/androidTests
zip -r "${REPORTS}/test-reports-android" connected
popd

echo "======== TEST COMPLETE"

