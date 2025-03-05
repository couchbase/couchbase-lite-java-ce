#!/bin/bash
#
# Publish Couchbase Lite Java for MacOS, Community Edition
#
MAVEN_URL="https://proget.sc.couchbase.com/maven2/cimaven"
STATUS=0

function usage() {
    echo "Usage: $0 <release version> <build number> <artifacts path> <workspace path>"
    exit 1
}

if [ "$#" -ne 4 ]; then usage; fi

VERSION="$1"
if [ -z "$VERSION" ]; then usage; fi

BUILD_NUMBER="$2"
if [ -z "$BUILD_NUMBER" ]; then usage; fi

ARTIFACTS="$3"
if [ -z "$ARTIFACTS" ]; then usage; fi

WORKSPACE="$4"
if [ -z "$WORKSPACE" ]; then usage; fi

echo "======== PUBLISH Couchbase Lite Java for MacOS, Community Edition v`cat ../../version.txt`-${BUILD_NUMBER}" 
./gradlew ciPublish -PbuildNumber=${BUILD_NUMBER} -PmavenUrl=${MAVEN_URL} || STATUS=7

echo "======== OSX: PUBLICATION COMPLETE: ${STATUS}"
exit $STATUS

