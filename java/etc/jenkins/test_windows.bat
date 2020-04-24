@echo on

:: Build Couchbase Lite Java, Community Edition for Windows


if "%2%" == "" (
    echo Usage: test_windows.bat ^<BUILD_NUMBER^> ^<REPORTS^>
    exit /B 1
)

set buildNumber=%2%
set reportsDir=%1%


echo ======== TEST Couchbase Lite Java, Community Edition 
call gradlew.bat ciTest || goto error

echo ======== Copy test reports
copy lib\build\reports  %reportsDir%

echo ======== TEST COMPLETE

goto :eof

:error
echo Failed with error %ERRORLEVEL%.
exit /b %ERRORLEVEL%
