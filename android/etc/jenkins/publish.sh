#!/bin/bash
#
# Publish Couchbase Lite Android, Community Edition
#
PRODUCT='couchbase-lite-android'
LIB_NAME="${PRODUCT}"
EDITION='community'
POM_FILE='pom.xml'

PROGET_URL='https://proget.sc.couchbase.com'
MAVEN_URL="${PROGET_URL}/maven2/internalmaven/com/couchbase/lite"

function usage() {
    echo "Usage: $0 "'<release version> <build number> <artifacts path> <workspace path>'
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

if ! hash mvn 2>/dev/null; then
    echo "Cannot find the 'mvn' command.  Please be sure it is on the PATH"
    exit 1
fi

BUILD="${VERSION}-${BUILD_NUMBER}"
STATUS=0

echo "======== PUBLISH Couchbase Lite Android, Community Edition v${BUILD}"

echo "======== Promote ${LIB_NAME}-${BUILD}"
curl -v -H "Content-Type: application/json" \
    --data '{"API_Key": "'"${PROGET_PROMOTION_TOKEN}"'", "name": "'"${LIB_NAME}"'", "group": "com.couchbase.lite", "version": "'"${BUILD}"'", "fromFeed": "cimaven", "toFeed": "internalmaven"}' \
    "${PROGET_URL}/api/promotions/promote"

echo "======== Copy artifacts to staging directory"
pushd "${ARTIFACTS}"
curl "${MAVEN_URL}/${LIB_NAME}/${BUILD}/${LIB_NAME}-${BUILD}.pom" -o "${POM_FILE}"
curl "${MAVEN_URL}/${LIB_NAME}/${BUILD}/${LIB_NAME}-${BUILD}.aar" -o "${LIB_NAME}-${BUILD}-release.aar"
curl --remote-name "${MAVEN_URL}/${LIB_NAME}/${BUILD}/${LIB_NAME}-${BUILD}-javadoc.jar"
curl --remote-name "${MAVEN_URL}/${LIB_NAME}/${BUILD}/${LIB_NAME}-${BUILD}-sources.jar"
popd

echo "======== Pull dependencies for zip"
DEPS_DIR="${WORKSPACE}/dependencies"
rm -rf "${DEPS_DIR}"
mkdir -p "${DEPS_DIR}"
pushd "${DEPS_DIR}"
cp "${ARTIFACTS}/${POM_FILE}" ./pom.xml
sed -i.bak "s#<packaging>aar</packaging>#<packaging>pom</packaging>#" pom.xml
diff pom.xml pom.xml.bak
mvn install dependency:copy-dependencies
popd

echo "======== Create zip"
ZIP_STAGING="${WORKSPACE}/staging"
rm -rf "${ZIP_STAGING}"
mkdir -p "${ZIP_STAGING}"
pushd "${ZIP_STAGING}"
mkdir license lib docs
cp "${DEPS_DIR}/target/dependency/okio"*.jar lib
cp "${DEPS_DIR}/target/dependency/okhttp"*.jar lib
cp "${ARTIFACTS}/${LIB_NAME}-${BUILD}-release.aar" "lib/${LIB_NAME}-${VERSION}.aar"
cp "${ARTIFACTS}/${LIB_NAME}-${BUILD}-javadoc.jar" "docs/${LIB_NAME}-${VERSION}-javadoc.jar"
cp "${WORKSPACE}/cbl-java/legal/mobile/couchbase-lite/license/LICENSE_${EDITION}.txt" license/LICENSE.TXT
curl -Lfs --remote-name "https://raw.githubusercontent.com/couchbase/product-metadata/master/couchbase-lite-android/blackduck/${VERSION}/notices.txt" || true
zip -r "${ARTIFACTS}/${PRODUCT}-${EDITION}-${BUILD}.zip" *
popd

echo "======== PUBLICATION COMPLETE (${STATUS}) Couchbase Lite Android, Community Edition"
exit $STATUS

