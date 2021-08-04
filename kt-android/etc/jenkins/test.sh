#!/bin/bash
#
# Test Couchbase Lite Android, Enterprise Edition
#

# These versions must match the versions in lib/build.gradle
BUILD_TOOLS_VERSION='30.0.3'


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

SDK_MGR="${SDK_HOME}/tools/bin/sdkmanager --channel=1 --install"
STATUS=0

echo "======== TEST Couchbase Lite Android, Community Edition v`cat ../../version.txt`-${BUILD_NUMBER} on device: ${ANDROID_SERIAL}"

echo "======== Install Toolchain"
yes | ${SDK_MGR} --licenses > /dev/null 2>&1
${SDK_MGR} "build-tools;${BUILD_TOOLS_VERSION}"

cat <<EOF >> local.properties
sdk.dir=${SDK_HOME}
EOF

echo "======== Test"
./gradlew ciTest --info --console=plain -PautomatedTests=true -PbuildNumber="${BUILD_NUMBER}" || STATUS=5

echo "======== Archive reports"
pushd test/build/reports/androidTests
zip -r "${REPORTS}/test-reports-android" connected
find "${REPORTS}"
popd

echo "======== TEST COMPLETE"
exit $STATUS

