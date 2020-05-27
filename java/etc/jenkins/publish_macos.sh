#!/bin/bash
#
# Publish Couchbase Lite Java, Community Edition
#
PRODUCT='couchbase-lite-java'
MAVEN_URL="http://mobile.maven.couchbase.com/maven2/internalmaven"

function usage() {
    echo "Usage: $0 <release version> <build number> <artifacts path> <workspace path>"
    exit 1
}

if [ "$#" -ne 4 ]; then
    usage
fi

VERSION="$1"
if [ -z "$VERSION" ]; then
    usage
fi

BUILD_NUMBER="$2"
if [ -z "$BUILD_NUMBER" ]; then
    usage
fi

ARTIFACTS="$3"
if [ -z "$ARTIFACTS" ]; then
    usage
fi

WORKSPACE="$4"
if [ -z "$WORKSPACE" ]; then
    usage
fi

echo "======== PUBLISH Couchbase Lite Java, Community Edition v`cat ../../version.txt`-${BUILD_NUMBER}" 
./gradlew ciPublish -PbuildNumber=${BUILD_NUMBER} -PmavenUrl=${MAVEN_URL} || exit 1

echo "======== Copy artifacts to staging directory"
cp "lib/build/distributions/${PRODUCT}-${VERSION}-${BUILD_NUMBER}.zip" "${ARTIFACTS}/${PRODUCT}-${VERSION}-${BUILD_NUMBER}-macos.zip" || exit 1

find "${ARTIFACTS}"
echo "======== PUBLICATION COMPLETE"

