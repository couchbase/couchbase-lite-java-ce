#!/bin/bash
#
# Publish Couchbase Lite Android Kotlin Extensions, Community Edition
#
PRODUCT='couchbase-lite-android-ktx'
LIB_NAME="${PRODUCT}"
EDITION='community'

MAVEN_URL="http://proget.build.couchbase.com/maven2/internalmaven"

COMMON_ETC="`pwd`/../../common/etc"


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

if ! hash mvn 2>/dev/null; then
    echo "Cannot find the 'mvn' command.  Please be sure it is on the PATH"
    exit 1
fi

STATUS=0

echo "======== PUBLISH Couchbase Lite Android Kotlin Extensions, Community Edition v`cat ../../version.txt`-${BUILD_NUMBER}" 

## Really should promote the existing package, instead of re-publishing
## Something like this:
## curl -X POST  -H "Content-Type: application/json" \
##     --data '{"API_Key": "<promote key>", "groupName": "com.couchbase.lite", "packageName": "couchbase-lite-android", "version": "2.7.0-43", "fromFeed": "cimaven", "toFeed": "internalmaven"}' \
##      http://proget.build.couchbase.com/api/promotions/promote
## At present that call fails to promote the entire package (bad PK copying the source tar)
## so, for now, just republish the same bits.
./gradlew ciPublish -PbuildNumber="${BUILD_NUMBER}" -PmavenUrl="${MAVEN_URL}" || STATUS=7

echo "======== Copy artifacts to staging directory"
POM_FILE='pom-ktx.xml'
cp lib/build/outputs/aar/*.aar "${ARTIFACTS}/"
cp lib/build/libs/*.jar "${ARTIFACTS}/"
cp lib/build/publications/libRelease/pom-default.xml "${ARTIFACTS}/${POM_FILE}"

echo "======== Pull dependencies for zip"
DEPS_DIR="${WORKSPACE}/dependencies"
rm -rf "${DEPS_DIR}"
mkdir -p "${DEPS_DIR}"
pushd "${DEPS_DIR}"
cp "${ARTIFACTS}/${POM_FILE}" ./pom.xml
sed -i.bak "s#<packaging>aar</packaging>#<packaging>pom</packaging>#" pom.xml
diff pom.xml pom.xml.bak
mvn install dependency:copy-dependencies -gs "${COMMON_ETC}/mvn/settings.xml" -PCblInternalMaven
popd

echo "======== Create zip"
ZIP_STAGING="${WORKSPACE}/staging"
rm -rf "${ZIP_STAGING}"
mkdir -p "${ZIP_STAGING}"
pushd "${ZIP_STAGING}"
mkdir license lib docs
# dependencies
cp "${DEPS_DIR}/target/dependency/"*.jar lib
# cbl library
cp "${DEPS_DIR}/target/dependency/couchbase-lite-android"*.aar "lib/couchbase-lite-android-${VERSION}.aar"
# ktx lib
cp "${ARTIFACTS}/${LIB_NAME}-${VERSION}-${BUILD_NUMBER}-release.aar" "lib/${LIB_NAME}-${VERSION}.aar"
# cbl javadoc
cp "${ARTIFACTS}/${LIB_NAME}-${VERSION}-${BUILD_NUMBER}-javadoc.jar" "docs/${LIB_NAME}-${VERSION}-javadoc.jar"
# license
cp "${WORKSPACE}/cbl-java/legal/mobile/couchbase-lite/license/LICENSE_${EDITION}.txt" license/LICENSE.TXT
zip -r "${ARTIFACTS}/${PRODUCT}-${EDITION}-${VERSION}-${BUILD_NUMBER}.zip" *
popd

echo "======== PUBLICATION COMPLETE (${STATUS}) Couchbase Lite Android Kotlin Extensions, Community Edition"
exit $STATUS

