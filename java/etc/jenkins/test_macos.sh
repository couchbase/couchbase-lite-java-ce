#!/bin/bash
#
# Test Couchbase Lite Java for MacOS, Community Edition
#
function usage() {
    echo "Usage: $0 <build number> <reports path>"
    exit 1
}

if [ "$#" -ne 2 ]; then usage; fi

BUILD_NUMBER="$1"
if [ -z "$BUILD_NUMBER" ]; then usage; fi

REPORTS="$2"
if [ -z "$REPORTS" ]; then usage; fi

STATUS=0

echo "======== TEST Couchbase Lite Java for MacOS, Community Edition v`cat ../../version.txt`-${BUILD_NUMBER}"
./gradlew ciTest --console=plain -PautomatedTests=true -PbuildNumber="${BUILD_NUMBER}" > test.log 2>&1 || STATUS=8
zip -r "${REPORTS}/test-log-macos" test.log

echo "======== OSX: Publish reports"
pushd test/build > /dev/null
rm -rf test-results/test/binary
cp -a test-results/test reports/tests/test/raw

cd reports/tests
zip -r "${REPORTS}/test-reports-macos" test
popd > /dev/null

echo "======== OSX: TEST COMPLETE ${STATUS}"
exit $STATUS

