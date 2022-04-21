param (
    [Parameter(Mandatory=$true)]
	[string]$version,

    [Parameter(Mandatory=$true)]
	[string]$buildNumber,

    [Parameter(Mandatory=$true)]
    [string]$artifactsDir
)

$product="couchbase-lite-java"
$mavenUrl= "http://proget.build.couchbase.com/maven2/cimaven"
$status = 0

Write-Host "======== PUBLISH Couchbase Lite Java for Windows, Community Edition"
& "$PSScriptRoot\..\..\gradlew.bat" --no-daemon ciPublish -PbuildNumber="$buildNumber" -PmavenUrl="$mavenUrl"
if ($LASTEXITCODE -ne 0) {
    $status = 7
    Write-Host "Publish failing with error $LASTEXITCODE"
}

Write-Host "======== Copy artifacts to staging directory"
Copy-Item "lib\build\distributions\$product-$version-$buildNumber.zip" -Destination "$artifactsDir\$product-$version-$buildNumber-windows.zip"

Write-Host "======== PUBLICATION COMPLETE $status"
exit $status

