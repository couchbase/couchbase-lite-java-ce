#!/bin/bash
#
# Test Couchbase Lite Java for Java, Community Edition
#
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
SUPPORT_DIR="${SCRIPT_DIR}/../../../../common/lite-core/linux/x86_64/lib"
STATUS=0

function usage() {
    echo "Usage: $0 <build number> <reports path>"
    exit 1
}

if [ "$#" -ne 2 ]; then usage; fi

BUILD_NUMBER="$1"
if [ -z "$BUILD_NUMBER" ]; then usage; fi

REPORTS="$2"
if [ -z "$REPORTS" ]; then usage; fi

echo "======== TEST Couchbase Lite Java for Linux, Community Edition v`cat ../../version.txt`-${BUILD_NUMBER}"
export LD_LIBRARY_PATH="${SUPPORT_DIR}:${LD_LIBRARY_PATH}"

./gradlew ciTest --console=plain -PautomatedTests=true -PbuildNumber="${BUILD_NUMBER}" > test.log 2>&1 || STATUS=8
zip -r "${REPORTS}/test-log-linux" test.log test/.test*/hs_err_pid*.log

echo "======== Linux: Publish reports"
pushd test/build > /dev/null
rm -rf test-results/test/binary
cp -a test-results/test reports/tests/test/raw

cd reports/tests
zip -r "${REPORTS}/test-reports-linux" test
popd > /dev/null

echo "======== Linux: TEST COMPLETE: ${STATUS}"
exit $STATUS

