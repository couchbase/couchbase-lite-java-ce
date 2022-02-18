Set-PSDebug -Trace 1

#Publish Couchbase Lite Java for Windows, Community Edition
$product="couchbase-lite-java"
$mavenUrl= "http://proget.build.couchbase.com/maven2/cimaven"

if ($args[3] -eq "")
{
    Write-Host "Usage: publish_windows.ps1 <VERSION> <BUILD_NUMBER> <ARTIFACT>"
    exit 1
}

param (
	[string]$version,
	[string]$buildNumber,
    [string]$artifactsDir
)

$status = 0

Write-Host "======== PUBLISH Couchbase Lite Java for Windows, Community Edition"
$process = Start-Process -FilePath "$PSScriptRoot\..\..\gradlew.bat" -ArgumentList "ciPublish -PbuildNumber=$buildNumber" -PassThru -Wait
if($process.ExitCode -ne 0){
    $status = 5
}

Write-Host "======== Copy artifacts to staging directory"
Copy-Item "lib\build\distributions\$product-$version-$buildNumber.zip $artifactsDir\$product-$version-$buildNumber-windows.zip"

Write-Host "======== PUBLICATION COMPLETE $status"
exit $status