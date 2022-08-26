#!/bin/bash
#
# Build Couchbase Lite Java for Linux, MacOS, Windows, Community Edition
# This script assumes the the OSX and Windows builds are available on latestbuilds
#
PRODUCT="couchbase-lite-java"
LATESTBUILDS="http://latestbuilds.service.couchbase.com/builds/latestbuilds"

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
ROOT_DIR="${SCRIPT_DIR}/../../../.."
TOOLS_DIR="${ROOT_DIR}/etc/jenkins"
CORE_DIR="${ROOT_DIR}/common/lite-core"

function usage() {
   echo "Usage: $0 <release version> <build number> <workspace path>"
   exit 1
}

if [ "$#" -ne 3 ]; then usage; fi

VERSION="$1"
if [ -z "${VERSION}" ]; then usage; fi

BUILD_NUMBER="$2"
if [ -z "${BUILD_NUMBER}" ]; then usage; fi

WORKSPACE="$3"
if [ -z "${WORKSPACE}" ]; then usage; fi

echo "======== BUILD Couchbase Lite Java for Linux, Community Edition v`cat ../../version.txt`-${BUILD_NUMBER}"

echo "======== Linux: Download Platform Artifacts" 
# Linux LiteCore should already be in place,
# because it was pulled during the check phase

ARTIFACTS_DIR="${WORKSPACE}/zip_tmp"
rm -rf "${ARTIFACTS_DIR}" > /dev/null 2>&1
mkdir -p "${ARTIFACTS_DIR}"
pushd "${ARTIFACTS_DIR}" > /dev/null

ARTIFACT="${PRODUCT}-${VERSION}"
ARTIFACT_URL="${LATESTBUILDS}/couchbase-lite-java/${VERSION}/${BUILD_NUMBER}"
for PLATFORM in macos windows; do
   rm -rf "${PLATFORM}" > /dev/null 2>&1
   mkdir  "${PLATFORM}"
   pushd "${PLATFORM}" > /dev/null 2>&1

   curl -f -L "${ARTIFACT_URL}/${ARTIFACT}-${BUILD_NUMBER}-${PLATFORM}.zip" -o "${PLATFORM}.zip" || exit 4

   unzip "${PLATFORM}.zip"

   jar -xf "${ARTIFACT}-${BUILD_NUMBER}/lib/${ARTIFACT}.jar" "${ARTIFACTS_DIR}/libs"

   popd > /dev/null
done
cp -R libs/* "${CORE_DIR}"

popd > /dev/null

echo "======== Linux: Build Java"
./gradlew ciBuild -PbuildNumber="${BUILD_NUMBER}" || exit 6

echo "======== Linux: BUILD COMPLETE"

