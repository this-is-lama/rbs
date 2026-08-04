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

Get-ChildItem -Path $reportsDir -Filter "*-summary.json" -ErrorAction SilentlyContinue | Remove-Item -Force -ErrorAction SilentlyContinue

$scenarios = @(
    "00-health-check",
    "01-restaurants-list",
    "02-restaurant-details",
    "03-availability",
    "04-pricing-offer",
    "05-create-booking",
    "06-full-booking-flow",
    "07-mixed-user-flow",
    "08-cache-check",
    "09-notification-resilience",
    "10-auto-stress-limit"
)

Write-Host "Starting all k6 scenarios. Test data is loaded only from config.js."
Write-Host "Reports directory: $reportsDir"
Write-Host ""

foreach ($scenario in $scenarios) {
    $scenarioFile = Join-Path $PSScriptRoot "scenarios\$scenario.js"
    $summaryFile = Join-Path $reportsDir "$scenario-summary.json"

    Write-Host "========================================"
    Write-Host "Running $scenario"
    Write-Host "========================================"

    & $k6Path run --summary-export $summaryFile $scenarioFile
    $exitCode = $LASTEXITCODE

    if ($exitCode -ne 0) {
        Write-Host "Scenario $scenario finished with exit code $exitCode. Next scenario will still be started."
    }

    Write-Host ""
}

Write-Host "Generating Markdown report..."
& powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot "generate-report.ps1")

Write-Host ""
Write-Host "All tests finished."
Write-Host "Markdown report: reports\README.md"