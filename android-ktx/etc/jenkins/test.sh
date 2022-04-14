#!/bin/bash
#
# Test Couchbase Lite Android Kotlin Extensions, Enterprise Edition
#

function usage() {
    echo "Usage: $0 <sdk path> <build number> <reports dir>"
    exit 1
}

if [ "$#" -ne 3 ]; then usage; fi

# ignored
SDK_HOME="$1"
if [ -z "$SDK_HOME" ]; then usage; fi

BUILD_NUMBER="$2"
if [ -z "$BUILD_NUMBER" ]; then usage; fi

REPORTS="$3"
if [ -z "REPORTS" ]; then usage; fi

STATUS=0

echo "======== TEST Couchbase Lite Android Kotlin Extensions, Community Edition v`cat ../../version.txt`-${BUILD_NUMBER} on device: ${ANDROID_SERIAL}"

echo "======== Test"
./gradlew ciTest --console=plain -PautomatedTests=true -PbuildNumber="${BUILD_NUMBER}" || STATUS=8

echo "======== Publish reports"
pushd test/build
cp -a outputs/androidTest-results/connected reports/androidTests/connected/raw
cd reports/androidTests
zip -r "${REPORTS}/test-reports-android" connected
popd

echo "======== TEST COMPLETE"
exit $STATUS

