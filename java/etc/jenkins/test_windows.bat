
echo on

rem Build Couchbase Lite Java for Windows, Community Edition

if "%2%" == "" (
    echo Usage: test_windows.bat ^<BUILD_NUMBER^> ^<REPORTS^>
    exit /B 1
)

set buildNumber=%1%
set reportsDir=%2%
set status=0

echo ======== TEST Couchbase Lite Java for Windows, Community Edition 
call gradlew.bat ciTest --console=plain -PautomatedTests=true -PbuildNumber=%buildNumber%  > test.log 2>&1 || set status=5
7z a -tzip -r "%reportsDir%\test-log-windows.zip" test.log

echo ======== Publish test reports
pushd test\build
7z a -tzip -r "%reportsDir%\test-reports-windows.zip" reports
popd

echo ======== TEST COMPLETE %status%
exit /B %status%

