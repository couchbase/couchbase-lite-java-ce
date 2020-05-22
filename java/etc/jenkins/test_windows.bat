
echo on

rem Build Couchbase Lite Java, Community Edition for Windows

if "%2%" == "" (
    echo Usage: test_windows.bat ^<BUILD_NUMBER^> ^<REPORTS^>
    exit /B 1
)

set buildNumber=%1%
set reportsDir=%2%

echo ======== TEST Couchbase Lite Java, Community Edition 
call gradlew.bat ciTest --info --console=plain || goto error

echo ======== Copy test reports
xcopy lib\build\reports %reportsDir% /S /C /I /Y

echo ======== TEST COMPLETE
exit /B 0

:error
echo Failed with error %ERRORLEVEL%.
exit /B %ERRORLEVEL%

