#!/bin/bash
#
# Build Couchbase Lite Android Kotlin Extensions, Community Edition
#

# These versions must match the versions in lib/build.gradle
BUILD_TOOLS_VERSION='32.0.0'

MAVEN_URL="http://proget.build.couchbase.com/maven2/cimaven"


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

echo "======== BUILD Couchbase Lite Android Kotlin Extensions, Community Edition v`cat ../../version.txt`-${BUILD_NUMBER}"

echo "======== Install Toolchain"
yes | ${SDK_MGR} --licenses > /dev/null 2>&1
${SDK_MGR} --install "build-tools;${BUILD_TOOLS_VERSION}"

echo "======== Build"
./gradlew ciBuild -PbuildNumber="${BUILD_NUMBER}" || STATUS=6

if  [ $STATUS -eq 0 ]; then
    echo "======== Publish artifacts"
    ./gradlew ciPublish -PbuildNumber="${BUILD_NUMBER}" -PmavenUrl="${MAVEN_URL}" || STATUS=7
fi

# No reports yet...
#echo "======== Archive reports"
#pushd lib/build
#zip -r "${REPORTS}/analysis-reports-android-ktx" reports
#popd

echo "======== BUILD COMPLETE (${STATUS}) Couchbase Lite Android Kotlin Extensions, Community Edition"
exit $STATUS

