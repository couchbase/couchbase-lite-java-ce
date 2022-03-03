param (
    [Parameter(Mandatory=$true)]
	[string]$version,

    [Parameter(Mandatory=$true)]
	[string]$buildNumber,

    [Parameter(Mandatory=$true)]
    [string]$artifactsDir
)

Set-PSDebug -Trace 1

#Publish Couchbase Lite Java for Windows, Community Edition
$product="couchbase-lite-java"
$mavenUrl= "http://proget.build.couchbase.com/maven2/cimaven"
$status = 0

Write-Host "======== PUBLISH Couchbase Lite Java for Windows, Community Edition"
$process = Start-Process -FilePath "$PSScriptRoot\..\..\gradlew.bat" -ArgumentList "--no-daemon ciPublish -PbuildNumber=$buildNumber -PmavenUrl=$mavenUrl" -PassThru -Wait
if($process.ExitCode -ne 0){
    $status = 5
}

Write-Host "======== Copy artifacts to staging directory"
Copy-Item "lib\build\distributions\$product-$version-$buildNumber.zip" -Destination "$artifactsDir\$product-$version-$buildNumber-windows.zip"

Write-Host "======== PUBLICATION COMPLETE $status"
exit $status