#
# CI Build script for Enterprise Android
#
GROUP='com.couchbase.lite'
PRODUCT='coucbase-lite-android-ee'
EDITION='enterprise'

# These versions must match the versions in lib/build.gradle
NDK_VERSION='20.1.5948944'
CMAKE_VERSION='3.10.2.4988404'

MAVEN_URL="http://mobile.maven.couchbase.com/maven2/cimaven"


function usage() {
    echo "Usage: $0 <sdk path> <build number>"
    exit 1
}

if [ "$#" -ne 2 ]; then
    usage
fi

SDK_HOME="$1"
if [ -z "$SDK_HOME" ]; then
    usage
fi

BUILD_NUMBER="$2"
if [ -z "$BUILD_NUMBER" ]; then
    usage
fi

SDK_MGR="${SDK_HOME}/tools/bin/sdkmanager"

echo "======== BUILD Couchbase Lite Android, Enterprise Edition v`cat ../version.txt`-${BUILD_NUMBER}"

echo "======== Install Toolchain"
yes | ${SDK_MGR} --licenses > /dev/null 2>&1
${SDK_MGR} --install 'build-tools;29.0.3'
${SDK_MGR} --install "cmake;${CMAKE_VERSION}"
${SDK_MGR} --install "ndk;${NDK_VERSION}"

# The Jenkins script has already put passwords into local.properties
cat <<EOF >> local.properties
sdk.dir=${SDK_HOME}
ndk.dir=${SDK_HOME}/ndk/${NDK_VERSION}
cmake.dir=${SDK_HOME}/cmake/${CMAKE_VERSION}
EOF

echo "======== Build"
./gradlew ciCheck -PbuildNumber="${BUILD_NUMBER}" || exit 1

echo "======== Publish artifacts"
./gradlew ciPublish -PbuildNumber="${BUILD_NUMBER}" -PmavenUrl="${MAVEN_URL}" || exit 1

echo "======== BUILD COMPLETE"
