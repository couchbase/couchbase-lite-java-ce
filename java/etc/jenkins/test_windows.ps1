param(
    [Parameter(Mandatory=$true)]
    [string]$buildNumber,

    [Parameter(Mandatory=$true)]
    [string]$reportsDir
)

$status = 0

Write-Host "======== TEST Couchbase Lite Java for Windows, Community Edition"
$process = Start-Process -FilePath "$PSScriptRoot\..\..\gradlew.bat" -ArgumentList "--no-daemon ciTest --console=plain -PautomatedTests=true -PbuildNumber=$buildNumber" -PassThru -Wait
if($process.ExitCode -ne 0){
    $status = 8
}
& 7z a -tzip -r "$reportsDir\test-log-windows.zip" test.log

Write-Host "======== Publish test reports"
Push-Location test\build
Remove-Item -Recurse -Force test-results\test\binary
Xcopy /e /i /y test-results\test reports\tests\test\raw
cd reports\tests
& 7z a -tzip -r "$reportsDir\test-reports-windows.zip" test
Pop-Location


Write-Host "======== TEST COMPLETE $status"
exit $status