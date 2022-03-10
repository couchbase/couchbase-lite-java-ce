#!/bin/bash
#
# Static Analysis for Couchbase Lite Java, Community Edition
#
NEXUS_URL="http://nexus.build.couchbase.com:8081/nexus/content/repositories/releases/com/couchbase/litecore"

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
TOOLS_DIR="${SCRIPT_DIR}/../../../../common/tools"

function usage() {
    echo "Usage: $0 <build number>"
    exit 1
}

if [ "$#" -ne 1 ]; then
    usage
fi

BUILD_NUMBER="$1"
if [ -z "${BUILD_NUMBER}" ]; then
    usage
fi

echo "======== CHECK Couchbase Lite Java, Community Edition v`cat ../../version.txt`-${BUILD_NUMBER}"

echo "======== Clean up ..."
"${TOOLS_DIR}/clean_litecore.sh"

echo "======== Download Lite Core ..."
"${TOOLS_DIR}/fetch_java_litecore.sh" -p "linux" -e EE -n "${NEXUS_URL}"

./gradlew ciCheck -PbuildNumber="${BUILD_NUMBER}" || exit 1

