Set-PSDebug -Trace 1

# Build COuchbase Lite Java for Windows, Community Edition
$liteCoreRepoUrl = "http://nexus.build.couchbase.com:8081/nexus/content/repositories/releases/com/couchbase/litecore"


if ($args[2] -eq "")
{
    Write-Host "Usage: build_windows.ps1 <VS Generator: 2015,2017,2019> <BUILD_NUMBER>"
    exit 1
}

param (
	[string]$vsGen,
	[string]$buildNumber
)

Push-Location $PSScriptRoot
$scriptDir = "$PSScriptRoot"
Pop-Location

$toolsDir = "$scriptDir\..\..\..\..\common\tools"

Write-Host "======== BUILD Couchbase Lite Java for Windows, Community Edition"
Write-Host "======== Clean up"
& $toolsDir/clean_litecore.ps1

Write-Host "======== Download Lite Core"
& $toolsDir/fetch_litecore.ps1 $LiteCoreRepoUrl -Edition "CE"

Write-Host "======== Build Java"
$process = Start-Process -FilePath "gradlew.bat" -ArgumentList "ciBuild -PbuildNumber=$buildNumber" -PassThru -Wait
if($process.ExitCode -ne 0){
    Write-Host "Failed with error $process.ExitCode"
    exit $process.ExitCode
}

Write-Host "======== BUILD COMPLETE"
exit 0

