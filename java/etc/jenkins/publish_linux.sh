#!/bin/bash
#
# Publish Couchbase Lite Java for Linux, Community Edition
#
MAVEN_URL="http://proget.build.couchbase.com/maven2"
STATUS=0

function usage() {
    echo "Usage: $0 <release version> <build number> <artifacts path> <workspace path> [<maven feed>]"
    exit 1
}

if [ "$#" -lt 4 -o "$#" -gt 5 ]; then usage; fi

VERSION="$1"
if [ -z "$VERSION" ]; then usage; fi

BUILD_NUMBER="$2"
if [ -z "$BUILD_NUMBER" ]; then usage; fi

ARTIFACTS="$3"
if [ -z "$ARTIFACTS" ]; then usage; fi

WORKSPACE="$4"
if [ -z "$WORKSPACE" ]; then usage; fi

FEED="$5"
if [ -z "$FEED" ]; then
    MAVEN_URL="${MAVEN_URL}/cimaven"
else
    MAVEN_URL="${MAVEN_URL}/$FEED"
fi

echo "======== PUBLISH Couchbase Lite Java for Linux, Community Edition v`cat ../../version.txt`-${BUILD_NUMBER}" 
./gradlew ciPublish -PbuildNumber=${BUILD_NUMBER} -PmavenUrl=${MAVEN_URL} || STATUS=7

echo "======== Linux: Copy artifacts to staging directory"
cp lib/build/distributions/couchbase-lite-java*.zip "${ARTIFACTS}/couchbase-lite-java-community-${VERSION}-${BUILD_NUMBER}.zip"
cp lib/build/libs/*.jar "${ARTIFACTS}/"
cp lib/build/publications/couchbaseLiteJava/pom-default.xml "${ARTIFACTS}/pom.xml"

echo "======== Linux: PUBLICATION COMPLETE: ${STATUS}"
exit $STATUS

