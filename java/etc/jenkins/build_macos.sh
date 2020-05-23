#!/bin/bash
#
# Build Couchbase Lite Java, Community Edition for MacOS
#
NEXUS_URL="http://nexus.build.couchbase.com:8081/nexus/content/repositories/releases/com/couchbase/litecore"

function usage() {
    echo "Usage: $0 <build number>"
    exit 1
}

if [ "$#" -ne 1 ]; then
    usage
fi

BUILD_NUMBER="$1"
if [ -z "$BUILD_NUMBER" ]; then
    usage
fi

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
TOOLS_DIR="${SCRIPT_DIR}/../../../../common/tools"

echo "======== BUILD Couchbase Lite Java, Community Edition v`cat ../../version.txt`-${BUILD_NUMBER}"

echo "======== Download Lite Core ..."
"${TOOLS_DIR}/fetch_litecore.sh" -e CE -n "${NEXUS_URL}"

echo "======== Build mbedcrypto ..."
"${TOOLS_DIR}/build_litecore.sh" -e CE -l mbedcrypto

echo "======== Build"
touch local.properties
./gradlew ciBuild -PbuildNumber="${BUILD_NUMBER}" || exit 1

find lib/build/distributions
echo "======== BUILD COMPLETE"
