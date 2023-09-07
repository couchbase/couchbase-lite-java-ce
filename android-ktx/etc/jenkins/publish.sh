#!/bin/bash
#
# Publish Couchbase Lite Android Kotlin Extensions, Community Edition
#
LIB_NAME='couchbase-lite-android-ktx'
EDITION='community'
POM_FILE='pom-ktx.xml'

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

echo "======== PUBLISH Couchbase Lite Android Kotlin Extensions, Community Edition v${BUILD}"

echo "======== Promote ${LIB_NAME}-${BUILD}"
curl -v -H "Content-Type: application/json" \
    --data '{"API_Key": "'"${PROGET_PROMOTION_TOKEN}"'", "name": "'"${LIB_NAME}"'", "group": "com.couchbase.lite", "version": "'"${BUILD}"'", "fromFeed": "cimaven", "toFeed": "internalmaven"}' \
    "${PROGET_URL}/api/promotions/promote"
echo

echo "======== Copy artifacts to staging directory"
pushd "${ARTIFACTS}"
curl "${MAVEN_URL}/${LIB_NAME}/${BUILD}/${LIB_NAME}-${BUILD}.pom" -o "${POM_FILE}"
curl "${MAVEN_URL}/${LIB_NAME}/${BUILD}/${LIB_NAME}-${BUILD}.aar" -o "${LIB_NAME}-${BUILD}-release.aar"
curl --remote-name "${MAVEN_URL}/${LIB_NAME}/${BUILD}/${LIB_NAME}-${BUILD}-javadoc.jar"
curl --remote-name "${MAVEN_URL}/${LIB_NAME}/${BUILD}/${LIB_NAME}-${BUILD}-sources.jar"
popd

echo "======== PUBLICATION COMPLETE Couchbase Lite Android Kotlin Extensions, Community Edition"

