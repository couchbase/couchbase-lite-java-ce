#!/bin/bash -e
#
# Test Couchbase Lite Java, Community Edition for MacOS
#
function usage() {
    echo "Usage: $0 <build number> <reports path>"
    exit 1
}

if [ "$#" -ne 2 ]; then
    usage
fi

BUILD_NUMBER="$1"
if [ -z "$BUILD_NUMBER" ]; then
    usage
fi

REPORTS="$2"
if [ -z "REPORTS" ]; then
    usage
fi

echo "======== TEST Couchbase Lite Java, Community Edition v`cat ../../version.txt`-${BUILD_NUMBER}"
./gradlew ciTest --info --console=plain || exit 1

echo "======== Copy test reports"
cp -a lib/build/reports/* "${REPORTS}"

find "${REPORTS}"
echo "======== TEST COMPLETE"

