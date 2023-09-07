#!/bin/bash
#
# Package and release Couchbase Lite Java release, Community Edition
#
LIB_NAME='couchbase-lite-java'

function usage() {
    echo "Usage: $0 "'<release version> <build number>'
    exit 1
}

if [ "$#" -lt 2 ]; then usage; fi

VERSION="$1"
if [ -z "$VERSION" ]; then usage; fi

BUILD_NUMBER="$2"
if [ -z "$BUILD_NUMBER" ]; then usage; fi

BUILD="${VERSION}-${BUILD_NUMBER}"

echo "======== RELEASE Couchbase Lite Java release, Community Edition v${BUILD}"

echo "======== Promote ${LIB_NAME}-${BUILD}"
curl -v -H "Content-Type: application/json" \
    --data '{"API_Key": "'"${PROGET_PROMOTION_TOKEN}"'", "name": "'"${LIB_NAME}"'", "group": "com.couchbase.lite", "version": "'"${BUILD}"'", "fromFeed": "cimaven", "toFeed": "internalmaven"}' \
    "https://proget.sc.couchbase.com/api/promotions/promote"
echo

echo "======== RELEASE COMPLETE Couchbase Lite Java release, Community Edition"

