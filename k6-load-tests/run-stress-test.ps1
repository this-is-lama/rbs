$ErrorActionPreference = "Continue"
Set-Location -Path $PSScriptRoot

$k6 = Get-Command k6 -ErrorAction SilentlyContinue
if ($null -eq $k6) {
    $defaultK6 = "C:\Program Files\k6\k6.exe"
    if (Test-Path $defaultK6) {
        $k6Path = $defaultK6
    } else {
        Write-Host "k6 not found. Install k6 or add k6.exe to PATH."
        exit 1
    }
} else {
    $k6Path = $k6.Source
}

$reportsDir = Join-Path $PSScriptRoot "reports"
if (!(Test-Path $reportsDir)) {
    New-Item -ItemType Directory -Path $reportsDir | Out-Null
}

$scenarioFile = Join-Path $PSScriptRoot "scenarios\10-auto-stress-limit.js"
$summaryFile = Join-Path $reportsDir "10-auto-stress-limit-summary.json"

Write-Host "Running stress scenario. Test data is loaded only from config.js."
Write-Host "Reports directory: $reportsDir"
Write-Host ""

& $k6Path run --summary-export $summaryFile $scenarioFile
& powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot "generate-report.ps1")