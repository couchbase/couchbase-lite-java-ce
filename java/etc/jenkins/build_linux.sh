#!/bin/bash
#
# Build Couchbase Lite Java for Linux, MacOS, Windows, Community Edition
# This script assumes the the OSX and Windows builds are available on latestbuilds
#
PROGET_URL='https://proget.sc.couchbase.com'
MAVEN_URL="${PROGET_URL}/maven2/cimaven/com/couchbase/lite"

PRODUCT='couchbase-lite-java'
LIB_NAME="${PRODUCT}"

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

BUILD="${VERSION}-${BUILD_NUMBER}"
CORE_DIR="${WORKSPACE}/cbl-java/common/lite-core"

echo "======== BUILD Couchbase Lite Java for Linux, Community Edition v${BUILD}"

NATIVE_LIBS_DIR="${WORKSPACE}/native_libs"
rm -rf "${NATIVE_LIBS_DIR}" > /dev/null 2>&1
mkdir -p "${NATIVE_LIBS_DIR}/libs"
pushd "${NATIVE_LIBS_DIR}" > /dev/null

# Linux LiteCore should already be in place,
# because it was pulled during the check phase
for PLATFORM in macos windows; do
   mkdir  "${PLATFORM}"
   pushd "${PLATFORM}" > /dev/null 2>&1

   echo "======== Download Platform Artifacts: $PLATFORM"
   curl "${MAVEN_URL}/${LIB_NAME}-${PLATFORM}/${BUILD}/${LIB_NAME}-${PLATFORM}-${BUILD}.jar" -o cbl.jar || exit 4

   jar -xf cbl.jar libs
   cp -R libs/* ../libs

   popd > /dev/null
done
cp -R libs/* "${CORE_DIR}"

popd > /dev/null

echo "======== Linux: Build Java"
./gradlew ciBuild -PbuildNumber="${BUILD_NUMBER}" || exit 6

echo "======== Linux: BUILD COMPLETE"

