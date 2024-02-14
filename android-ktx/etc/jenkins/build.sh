#!/bin/bash
#
# Build Couchbase Lite Android Kotlin Extensions, Community Edition
#
MAVEN_URL="https://proget.sc.couchbase.com/maven2/cimaven"


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
if [ -z "$REPORTS" ]; then usage; fi

STATUS=0

echo "======== BUILD Couchbase Lite Android Kotlin Extensions, Community Edition v`cat ../../version.txt`-${BUILD_NUMBER}"

echo "======== Build"
./gradlew ciBuild -PbuildNumber="${BUILD_NUMBER}" || STATUS=6

if [ $STATUS -eq 0 ]; then
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

