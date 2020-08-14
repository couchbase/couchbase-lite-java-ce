
echo on

rem Publish Couchbase Lite Java, Community Edition for Windows

set product=couchbase-lite-java
set mavenUrl="http://mobile.maven.couchbase.com/maven2/internalmaven"

if "%3%" == "" (
    echo Usage: publish_windows.bat ^<VERSION^> ^<BUILD_NUMBER^> ^<ARTIFACTS^>
    exit /B 1
)

set version=%1%
set buildNumber=%2%
set artifactsDir=%3%

echo ======== PUBLISH Couchbase Lite Java, Community Edition  
call gradlew.bat ciPublish -PbuildNumber=%buildNumber% -PmavenUrl=%mavenUrl%

echo "======== Copy artifacts to staging directory"
copy lib\build\distributions\%product%-%version%-%buildNumber%.zip %artifactsDir%\%product%-%version%-%buildNumber%-windows.zip

echo ======== PUBLICATION COMPLETE
exit /B 0
