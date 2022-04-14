param (
    [Parameter(Mandatory=$true)]
	[string]$vsGen,

    [Parameter(Mandatory=$true)]
	[string]$buildNumber
)

$toolsDir = "$PSScriptRoot\..\..\..\..\etc\jenkins"

Write-Host "======== BUILD Couchbase Lite Java for Windows, Community Edition"

Write-Host "======== Download Lite Core"
& $toolsDir/fetch_core.ps1 -Edition "CE"

Write-Host "======== Build Java"
$process = Start-Process -FilePath "$PSScriptRoot\..\..\gradlew.bat" -ArgumentList "--no-daemon ciBuild -PbuildNumber=$buildNumber" -PassThru -Wait
if($process.ExitCode -ne 0){
    Write-Host "Failed with error $process.ExitCode" 
    exit 6
}

Write-Host "======== BUILD COMPLETE"
exit 0

