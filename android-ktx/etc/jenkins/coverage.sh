#!/bin/bash
#
# Generate test converage report for  Couchbase Lite Android Community Edition
#
LIB_NAME='couchbase-lite-android-ee'

PROGET_URL='https://proget.sc.couchbase.com'
MAVEN_URL="${PROGET_URL}/maven2/internalmaven/com/couchbase/lite"

function usage() {
    echo "Usage: $0 "'<version> <build number> <reports dir>'
    exit 1
}

if [ "$#" -ne 3 ]; then usage; fi

VERSION="$1"
if [ -z "$VERSION" ]; then usage; fi

BUILD_NUMBER="$2"
if [ -z "$BUILD_NUMBER" ]; then usage; fi

REPORTS="$3"
if [ -z "REPORTS" ]; then usage; fi

BUILD="${VERSION}-${BUILD_NUMBER}"
STATUS=0

echo "======== COVERAGE for Couchbase Lite Android, Enterprise Edition v`cat ../../version.txt`-${BUILD_NUMBER}}"

echo "======== Download the classfiles"
rm -rf test/classes
mkdir -p test/classes
pushd test
curl "${MAVEN_URL}/${LIB_NAME}/${BUILD}/${LIB_NAME}-${BUILD}.aar" -o lib.zip
unzip lib.zip
cd classes
jar -xf ../classes.jar
popd

echo "======== Generate report"
./gradlew generateCoverageReport --console=plain -PautomatedTests=true -Pcoverage=true -PbuildNumber="${BUILD_NUMBER}" || STATUS=8

echo "======== Publish reports"
pushd test/build/reports/jacoco/generateCoverageReport
mv html coverage
zip -r "${REPORTS}/coverage-android-ee" coverage
popd

echo "======== COVERAGE COMPLETE"
exit $STATUS

