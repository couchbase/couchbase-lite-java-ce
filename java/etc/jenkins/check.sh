#!/bin/bash
#
# Static Analysis for Couchbase Lite Java, Community Edition
#
NEXUS_URL="http://nexus.build.couchbase.com:8081/nexus/content/repositories/releases/com/couchbase/litecore"

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
TOOLS_DIR="${SCRIPT_DIR}/../../../../common/tools"

function usage() {
    echo "Usage: $0 <build number> <distro>"
    exit 1
}

if [ "$#" -ne 2 ]; then
    usage
fi

BUILD_NUMBER="$1"
if [ -z "${BUILD_NUMBER}" ]; then
    usage
fi

DISTRO="$2"
if [ -z "${DISTRO}" ]; then
   usage
fi

echo "======== CHECK Couchbase Lite Java, Community Edition v`cat ../../version.txt`-${BUILD_NUMBER} (${DISTRO})"

"${TOOLS_DIR}/clean_litecore.sh" -p "${DISTRO}"

echo "======== Download Lite Core ..."
"${TOOLS_DIR}/fetch_litecore.sh" -p "${DISTRO}" -e EE -n "${NEXUS_URL}"

echo "======== Build mbedcrypto ..."
"${TOOLS_DIR}/build_litecore.sh" -l mbedcrypto -e EE

./gradlew ciCheck -PbuildNumber="${BUILD_NUMBER}" || exit 1

