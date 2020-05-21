
echo on

rem Publish Couchbase Lite Java, Community Edition for Windows

set product=couchbase-lite-java

if "%3%" == "" (
    echo Usage: publish_windows.bat ^<VERSION^> ^<BUILD_NUMBER^> ^<ARTIFACTS^>
    exit /B 1
)

set version=%1%
set buildNumber=%2%
set artifactsDir=%3%

echo ======== PUBLISH Couchbase Lite Java, Community Edition  
copy lib\build\distributions\%product%-%version%-%buildNumber%.zip %artifactsDir%\%product%-%version%-%buildNumber%-windows.zip || goto error

echo ======== PUBLICATION COMPLETE
exit /B 0

:error
echo Failed with error %ERRORLEVEL%.
exit /B %ERRORLEVEL%

