#!/bin/bash
#
# Package and release Couchbase Lite Java release, Community Edition
#
LIB_NAME='couchbase-lite-java'

PROGET_URL='https://proget.sc.couchbase.com'
MAVEN_URL="${PROGET_URL}/maven2/internalmaven/com/couchbase/lite"

function usage() {
    echo "Usage: $0 "'<release version> <build number> <artifacts path>'
    exit 1
}

if [ "$#" -lt 3 ]; then usage; fi

VERSION="$1"
if [ -z "$VERSION" ]; then usage; fi

BUILD_NUMBER="$2"
if [ -z "$BUILD_NUMBER" ]; then usage; fi

ARTIFACTS="$3"
if [ -z "$ARTIFACTS" ]; then usage; fi

BUILD="${VERSION}-${BUILD_NUMBER}"

echo "======== RELEASE Couchbase Lite Java release, Community Edition v${BUILD}"

echo "======== Promote ${LIB_NAME}-${BUILD}"
curl -v -H "Content-Type: application/json" \
    --data '{"API_Key": "'"${PROGET_PROMOTION_TOKEN}"'", "name": "'"${LIB_NAME}"'", "group": "com.couchbase.lite", "version": "'"${BUILD}"'", "fromFeed": "cimaven", "toFeed": "internalmaven"}' \
    "${PROGET_URL}/api/promotions/promote"
echo

pushd "${ARTIFACTS}"
curl "${MAVEN_URL}/${LIB_NAME}/${BUILD}/${LIB_NAME}-${BUILD}.pom" -o pom.xml
curl "${MAVEN_URL}/${LIB_NAME}/${BUILD}/${LIB_NAME}-${BUILD}.jar" -o "${LIB_NAME}-${BUILD}-release.jar"
curl --remote-name "${MAVEN_URL}/${LIB_NAME}/${BUILD}/${LIB_NAME}-${BUILD}-javadoc.jar"
curl --remote-name "${MAVEN_URL}/${LIB_NAME}/${BUILD}/${LIB_NAME}-${BUILD}-sources.jar"

echo "======== RELEASE COMPLETE Couchbase Lite Java release, Community Edition"

