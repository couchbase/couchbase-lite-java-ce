#!/bin/bash
#
# Static Analysis for Couchbase Lite Java, Community Edition
#

function usage() {
    echo "Usage: $0 <build number>"
    exit 1
}

if [ "$#" -ne 1 ]; then
    usage
fi

BUILD_NUMBER="$1"
if [ -z "${BUILD_NUMBER}" ]; then
    usage
fi

echo "======== CHECK Couchbase Lite Java, Enterprise Edition v`cat ../../version.txt`-${BUILD_NUMBER}"
touch local.properties
./gradlew ciCheck -PbuildNumber="${BUILD_NUMBER}" || exit 1

