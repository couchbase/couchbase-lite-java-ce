param (
    [Parameter(Mandatory=$true)]
	[string]$vsGen,

    [Parameter(Mandatory=$true)]
	[string]$buildNumber
)

$toolsDir = "$PSScriptRoot\..\..\..\..\etc\jenkins"
$status = 0

Write-Host "======== BUILD Couchbase Lite Java for Windows, Community Edition"

Write-Host "======== Windows: Download Lite Core"
& $toolsDir\fetch_core.ps1 -Edition "CE"

Write-Host "======== Windows: Build Java"
& "$PSScriptRoot\..\..\gradlew.bat" --no-daemon ciBuild -PbuildNumber=$buildNumber
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failing with error $LASTEXITCODE"
    $status = 6
}

Write-Host "======== Windows: BUILD COMPLETE"
exit $status

