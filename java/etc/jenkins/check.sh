#!/bin/bash
#
# Static Analysis for Couchbase Lite Java, Community Edition
#
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
TOOLS_DIR="${SCRIPT_DIR}/../../../../etc/jenkins"

function usage() {
    echo "Usage: $0 <build number>"
    exit 1
}

if [ "$#" -ne 1 ]; then usage; fi

BUILD_NUMBER="$1"
if [ -z "${BUILD_NUMBER}" ]; then usage; fi

echo "======== CHECK Couchbase Lite Java, Community Edition v`cat ../../version.txt`-${BUILD_NUMBER}"

echo "======== Download Lite Core ..."
"${TOOLS_DIR}/fetch_core.sh" -p linux -e CE

./gradlew ciCheck -PbuildNumber="${BUILD_NUMBER}" || exit 5

