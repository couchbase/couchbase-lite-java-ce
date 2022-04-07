#!/bin/bash
#
# Build Couchbase Lite Android, Community Edition
#
MAVEN_URL="http://proget.build.couchbase.com/maven2/cimaven"

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
ROOT_DIR="${SCRIPT_DIR}/../../../.."
COMMON_TOOLS="${ROOT_DIR}/common/tools"

function usage() {
    echo "Usage: $0 <sdk path> <build number> <reports dir>"
    exit 1
}

if [ "$#" -ne 3 ]; then
    usage
fi

# ignored
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

STATUS=0

echo "======== BUILD Couchbase Lite Android, Community Edition v`cat ../../version.txt`-${BUILD_NUMBER}"

echo "======== Clean up ..."
"${COMMON_TOOLS}/clean_litecore.sh"

echo "======== Download Lite Core ..."
"${COMMON_TOOLS}/fetch_android_litecore.sh" -e CE

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

