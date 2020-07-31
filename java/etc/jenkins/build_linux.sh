#!/bin/bash
#
# Build Couchbase Lite Java, Community Edition for MacOS, Windows, Linux
# This script assumes the the OSX and Windows builds are available on latestbuilds
#
PRODUCT="couchbase-lite-java-ee"
LATESTBUILDS_URL="http://latestbuilds.service.couchbase.com/builds/latestbuilds"
NEXUS_URL="http://nexus.build.couchbase.com:8081/nexus/content/repositories/releases/com/couchbase/litecore"

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
COMMON_DIR="${SCRIPT_DIR}/../../../../common"
TOOLS_DIR="${COMMON_DIR}/tools"
LIB_DIR="${COMMON_DIR}/lite-core"

function usage() {
   echo "Usage: $0 <release version> <build number> <workspace path> <distro>"
   exit 1
}

if [ "$#" -ne 4 ]; then
   usage
fi

VERSION="$1"
if [ -z "${VERSION}" ]; then
   usage
fi

BUILD_NUMBER="$2"
if [ -z "${BUILD_NUMBER}" ]; then
   usage
fi

WORKSPACE="$3"
if [ -z "${WORKSPACE}" ]; then
   usage
fi

DISTRO="$4"
if [ -z "${DISTRO}" ]; then
   usage
fi

echo "======== BUILD Couchbase Lite Java, Community Edition v`cat ../../version.txt`-${BUILD_NUMBER} (${DISTRO})"

echo "======== Download Lite Core ..."
"${TOOLS_DIR}/fetch_litecore.sh" -e CE -n "${NEXUS_URL}" -p "${DISTRO}"

echo "======== Build mbedcrypto ..."
"${TOOLS_DIR}/build_litecore.sh" -e CE -l mbedcrypto

echo "======== Check"
touch local.properties
./gradlew ciCheck -PbuildNumber="${BUILD_NUMBER}" || exit 1

echo "======== Download platform artifacts"
for PLATFORM in macos windows; do
   ARTIFACT="${PRODUCT}-${VERSION}-${BUILD_NUMBER}-${PLATFORM}.zip"
   REMOTE_ARTIFACT="${LATESTBUILDS_URL}/couchbase-lite-java/${VERSION}/${BUILD_NUMBER}/${ARTIFACT}"
   LOCAL_ARTIFACT="${WORKSPACE}/${ARTIFACT}"

   echo "Downloading artifact: ${REMOTE_ARTIFACT}"
   curl -L "${REMOTE_ARTIFACT}" -o "${LOCAL_ARTIFACT}"

   echo "Extracting artifact: ${LOCAL_ARTIFACT}"
   "${TOOLS_DIR}/extract_libs.sh" "${LOCAL_ARTIFACT}" "${WORKSPACE}"

   rm -rf "${LOCAL_ARTIFACT}"
done

echo "======== Build"
./gradlew ciBuild -PbuildNumber="${BUILD_NUMBER}" || exit 1

find lib/build/distributions
echo "======== BUILD COMPLETE"
