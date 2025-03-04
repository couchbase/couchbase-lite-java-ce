#!/bin/bash
#
# Publish Couchbase Lite Java for Linux, Community Edition
#
MAVEN_URL="https://proget.sc.couchbase.com/maven2/cimaven"

function usage() {
    echo "Usage: $0 "'<build number>'
    exit 1
}

if [ "$#" -ne 1 ]; then usage; fi

BUILD_NUMBER="$1"
if [ -z "$BUILD_NUMBER" ]; then usage; fi

STATUS=0

echo "======== PUBLISH Couchbase Lite Java for Linux, Community Edition v`cat ../../version.txt`-${BUILD_NUMBER}" 
./gradlew ciPublish -PbuildNumber=${BUILD_NUMBER} -PmavenUrl=${MAVEN_URL} || STATUS=7

echo "======== Linux: PUBLICATION COMPLETE: ${STATUS}"
exit $STATUS

