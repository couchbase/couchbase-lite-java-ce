#!/bin/bash
#
# Build Couchbase Lite Android, Community Edition
#

# These versions must match the versions in lib/build.gradle
NDK_VERSION='22.0.7026061'
CMAKE_VERSION='3.18.1'
BUILD_TOOLS_VERSION='32.0.0'

NEXUS_URL="http://nexus.build.couchbase.com:8081/nexus/content/repositories/releases/com/couchbase/litecore"
MAVEN_URL="http://proget.build.couchbase.com/maven2/cimaven"

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
TOOLS_DIR="${SCRIPT_DIR}/../../../../common/tools"


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

SDK_MGR="${SDK_HOME}/cmdline-tools/latest/bin/sdkmanager --channel=1"
STATUS=0

echo "======== BUILD Couchbase Lite Android, Community Edition v`cat ../../version.txt`-${BUILD_NUMBER}"

echo "======== Install Toolchain"
yes | ${SDK_MGR} --licenses > /dev/null 2>&1
${SDK_MGR} --install "build-tools;${BUILD_TOOLS_VERSION}"
${SDK_MGR} --install "cmake;${CMAKE_VERSION}"
${SDK_MGR} --install "ndk;${NDK_VERSION}"

echo "======== Clean up ..."
"${TOOLS_DIR}/clean_litecore.sh"

echo "======== Download Lite Core ..."
"${TOOLS_DIR}/fetch_android_litecore.sh" -e CE -n "${NEXUS_URL}"

echo "======== Check"
./gradlew ciCheck -PbuildNumber="${BUILD_NUMBER}" || STATUS=5

if  [ $STATUS -eq 0 ]; then
    echo "======== Build"
    ./gradlew ciBuild -PbuildNumber="${BUILD_NUMBER}" || STATUS=6
fi

if  [ $STATUS -eq 0 ]; then
    echo "======== Publish artifacts"
    ./gradlew ciPublish -PbuildNumber="${BUILD_NUMBER}" -PmavenUrl="${MAVEN_URL}" || STATUS=7
fi

echo "======== Archive reports"
pushd lib/build
zip -r "${REPORTS}/analysis-reports-android" reports
popd

echo "======== BUILD COMPLETE (${STATUS}) Couchbase Lite Android, Community Edition"
exit $STATUS

