#!/bin/bash -e
#
# Test Couchbase Lite Android, Enterprise Edition
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

echo "======== TEST Couchbase Lite Android, Community Edition v`cat ../../version.txt`-${BUILD_NUMBER} on device: ${ANDROID_SERIAL}"
./gradlew ciTest --info --console=plain -PautomatedTests=true -PbuildNumber="${BUILD_NUMBER}" || exit 1

echo "======== Copy test reports"
cp -rp test/build/reports/* "${REPORTS}/"

echo "======== TEST COMPLETE"

