#!/bin/bash
#
# Test Couchbase Lite Java, Community Edition
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

STATUS=0

echo "======== TEST Couchbase Lite Java, Community Edition v`cat ../../version.txt`-${BUILD_NUMBER}"
export LD_LIBRARY_PATH="${ROOT}/common/lite-core/support/linux/x86_64:${LD_LIBRARY_PATH}"
./gradlew ciTest --info --console=plain || STATUS=1

echo "======== Publish reports"
pushd lib/build
zip -r "${REPORTS}/test-reports-linux" reports
popd

find "${REPORTS}"
echo "======== TEST COMPLETE ${STATUS}"
exit $STATUS

