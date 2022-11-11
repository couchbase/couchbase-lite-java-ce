param(
    [Parameter(Mandatory=$true)]
    [string]$buildNumber,

    [Parameter(Mandatory=$true)]
    [string]$reportsDir
)

Set-PSDebug -Trace 2

$local:ErrorActionPreference = 'Continue'

$status = 0

Write-Host "======== TEST Couchbase Lite Java for Windows, Community Edition"
& "$PSScriptRoot\..\..\gradlew.bat" --no-daemon ciTest --console=plain -PautomatedTests=true -PbuildNumber="$buildNumber" 2>&1 > test.log
if ($LASTEXITCODE -ne 0) {
    Write-Host "Tests failed with error $LASTEXITCODE"
    Get-Content test.log
    $status = 8
}
7z a -tzip -r "$reportsDir\test-log-windows.zip" test.log test\.test*\hs_err_pid*.log

Write-Host "======== Windows: Publish test reports"
Push-Location test\build
Remove-Item -Recurse -Force test-results\test\binary
Xcopy /e /i /y test-results\test reports\tests\test\raw

Set-Location reports\tests
7z a -tzip -r "$reportsDir\test-reports-windows.zip" test
Pop-Location

Write-Host "======== Windows: TEST COMPLETE $status"
exit $status
