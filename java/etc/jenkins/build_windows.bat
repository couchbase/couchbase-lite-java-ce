
echo on

rem Build Couchbase Lite Java for Windows, Community Edition

set liteCoreRepoUrl="http://nexus.build.couchbase.com:8081/nexus/content/repositories/releases/com/couchbase/litecore"

if "%2%" == "" (
    echo Usage: build_windows.bat ^<VS Generator: 2015,2017,2019^> ^<BUILD_NUMBER^>
    exit /B 1
)

set vsGen=%1%
set buildNumber=%2%

pushd %~dp0
set scriptDir=%CD%
popd
set toolsDir=%scriptDir%\..\..\..\..\common\tools

echo ======== BUILD Couchbase Lite Java for Windows, Community Edition

echo ======== Clean up
powershell.exe -ExecutionPolicy Bypass -Command "%toolsDir%\clean_litecore.ps1"

echo ======== Download Lite Core
powershell.exe -ExecutionPolicy Bypass -Command "%toolsDir%\fetch_java_litecore.ps1" %liteCoreRepoUrl% CE

echo ======== Build Java
call gradlew.bat ciBuild -PbuildNumber=%buildNumber% || goto error

echo ======== BUILD COMPLETE
exit /B 0

:error
echo Failed with error %ERRORLEVEL%.
exit /B %ERRORLEVEL%
