param (
    [Parameter(Mandatory=$true)]
	[string]$version,

    [Parameter(Mandatory=$true)]
	[string]$buildNumber,

    [Parameter(Mandatory=$true)]
    [string]$artifactsDir
)

$mavenUrl= "https://proget.sc.couchbase.com/maven2/cimaven"
$status = 0

Write-Host "======== PUBLISH Couchbase Lite Java for Windows, Community Edition"
& "$PSScriptRoot\..\..\gradlew.bat" --no-daemon ciPublish -PbuildNumber="$buildNumber" -PmavenUrl="$mavenUrl"
if ($LASTEXITCODE -ne 0) {
    $status = 7
    Write-Host "Publish failing with error $LASTEXITCODE"
}

Write-Host "======== Windows: PUBLICATION COMPLETE $status"
exit $status

