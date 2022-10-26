param (
    [Parameter(Mandatory=$true)]
	[string]$version,

    [Parameter(Mandatory=$true)]
	[string]$buildNumber,

    [Parameter(Mandatory=$true)]
    [string]$artifactsDir
)

$mavenUrl= "http://proget.build.couchbase.com/maven2/cimaven"
$status = 0

Write-Host "======== PUBLISH Couchbase Lite Java for Windows, Community Edition"
& "$PSScriptRoot\..\..\gradlew.bat" --no-daemon ciPublish -PbuildNumber="$buildNumber" -PmavenUrl="$mavenUrl"
if ($LASTEXITCODE -ne 0) {
    $status = 7
    Write-Host "Publish failing with error $LASTEXITCODE"
}

Write-Host "======== Windows: Copy artifacts to staging directory"
Copy-Item "lib\build\distributions\couchbase-lite-java*.zip" -Destination "$artifactsDir\couchbase-lite-java-community-$version-$buildNumber-windows.zip"

Write-Host "======== Windows: PUBLICATION COMPLETE $status"
exit $status

