$ErrorActionPreference = "Stop"
Set-Location -Path $PSScriptRoot

$reportsDir = Join-Path $PSScriptRoot "reports"
$outputFile = Join-Path $reportsDir "README.md"

if (!(Test-Path $reportsDir)) {
    New-Item -ItemType Directory -Path $reportsDir | Out-Null
}

function Get-MetricValue($summary, [string]$metricName, [string]$fieldName) {
    if ($null -eq $summary.metrics) { return $null }
    $metricProperty = $summary.metrics.PSObject.Properties[$metricName]
    if ($null -eq $metricProperty) { return $null }

    $metric = $metricProperty.Value

    if ($null -ne $metric.values) {
        $valueProperty = $metric.values.PSObject.Properties[$fieldName]
        if ($null -ne $valueProperty) { return $valueProperty.Value }
    }

    $directProperty = $metric.PSObject.Properties[$fieldName]
    if ($null -ne $directProperty) { return $directProperty.Value }

    return $null
}

function Format-Number($value) {
    if ($null -eq $value) { return "-" }
    return ("{0:N2}" -f [double]$value).Replace(",", " ")
}

function Format-Integer($value) {
    if ($null -eq $value) { return "-" }
    return ("{0:N0}" -f [double]$value).Replace(",", " ")
}

function Format-Percent($value) {
    if ($null -eq $value) { return "-" }
    return ("{0:N2}%" -f ([double]$value * 100)).Replace(",", " ")
}

function Get-ScenarioLoad([string]$name) {
    if ($name -like "10-auto-stress-limit*") { return "до 1000 VUs" }
    return "100 VUs"
}

$files = Get-ChildItem -Path $reportsDir -Filter "*-summary.json" -ErrorAction SilentlyContinue | Sort-Object Name
$rows = @()

foreach ($file in $files) {
    try {
        $json = Get-Content $file.FullName -Raw -Encoding UTF8
        $summary = $json | ConvertFrom-Json
        $name = $file.BaseName.Replace("-summary", "")

        $rows += [PSCustomObject]@{
            Name = $name
            Load = Get-ScenarioLoad $name
            File = $file.Name
            HttpReqs = Get-MetricValue $summary "http_reqs" "count"
            FailedRate = Get-MetricValue $summary "http_req_failed" "rate"
            DurationAvg = Get-MetricValue $summary "http_req_duration" "avg"
            DurationP95 = Get-MetricValue $summary "http_req_duration" "p(95)"
            ChecksRate = Get-MetricValue $summary "checks" "rate"
            Iterations = Get-MetricValue $summary "iterations" "count"
            IterationRate = Get-MetricValue $summary "iterations" "rate"
            StressMaxVus = Get-MetricValue $summary "stress_active_vus" "max"
        }
    } catch {
        Write-Warning "Could not process $($file.Name): $($_.Exception.Message)"
    }
}

$now = Get-Date -Format "dd.MM.yyyy HH:mm:ss"
$lines = New-Object System.Collections.Generic.List[string]

$lines.Add("# Отчёт по нагрузочному тестированию RBS")
$lines.Add("")
$lines.Add("Дата формирования отчёта: **$now**.")
$lines.Add("")
$lines.Add("Обычные сценарии `00-09` выполнялись последовательно с нагрузкой **100 виртуальных пользователей**. Стрессовый сценарий `10-auto-stress-limit` выполнялся последним и предусматривал постепенное увеличение нагрузки до **1000 виртуальных пользователей**.")
$lines.Add("")
$lines.Add("## 1. Сводная таблица результатов")
$lines.Add("")
$lines.Add("| Сценарий | Нагрузка | HTTP-запросы | Ошибки HTTP | p95 HTTP, мс | Среднее HTTP, мс | Успешные проверки | Итерации | Итерации/с |")
$lines.Add("|---|---:|---:|---:|---:|---:|---:|---:|---:|")

foreach ($row in $rows) {
    $lines.Add("| $($row.Name) | $($row.Load) | $(Format-Integer $row.HttpReqs) | $(Format-Percent $row.FailedRate) | $(Format-Number $row.DurationP95) | $(Format-Number $row.DurationAvg) | $(Format-Percent $row.ChecksRate) | $(Format-Integer $row.Iterations) | $(Format-Number $row.IterationRate) |")
}

$lines.Add("")
$lines.Add("## 2. Детализация по сценариям")
$lines.Add("")

foreach ($row in $rows) {
    $lines.Add("### $($row.Name)")
    $lines.Add("")
    $lines.Add("- Файл JSON-отчёта: `$($row.File)`.")
    $lines.Add("- Заданная нагрузка: **$($row.Load)**.")
    $lines.Add("- Количество HTTP-запросов: **$(Format-Integer $row.HttpReqs)**.")
    $lines.Add("- Доля ошибочных HTTP-запросов: **$(Format-Percent $row.FailedRate)**.")
    $lines.Add("- Среднее время HTTP-запроса: **$(Format-Number $row.DurationAvg) мс**.")
    $lines.Add("- 95-й перцентиль времени HTTP-запроса: **$(Format-Number $row.DurationP95) мс**.")
    $lines.Add("- Доля успешных проверок: **$(Format-Percent $row.ChecksRate)**.")
    $lines.Add("- Количество завершённых итераций: **$(Format-Integer $row.Iterations)**.")
    $lines.Add("- Производительность по завершённым итерациям: **$(Format-Number $row.IterationRate) итераций/с**.")
    if ($row.Name -like "10-auto-stress-limit*" -and $null -ne $row.StressMaxVus) {
        $lines.Add("- Максимальное зафиксированное количество активных виртуальных пользователей: **$(Format-Integer $row.StressMaxVus) VUs**.")
    }
    $lines.Add("")
}

$stress = $rows | Where-Object { $_.Name -like "10-auto-stress-limit*" } | Select-Object -First 1
$lines.Add("## 3. Оценка предельной нагрузки")
$lines.Add("")

if ($null -ne $stress) {
    $estimatedUsers = if ($null -ne $stress.StressMaxVus) { [math]::Round([double]$stress.StressMaxVus) } else { 1000 }
    $iterationRate = Format-Number $stress.IterationRate
    $lines.Add("По результатам стрессового сценария система была проверена при росте нагрузки до **1000 VUs** либо до момента нарушения пороговых условий. Приблизительная оценка достигнутого уровня нагрузки составляет **$estimatedUsers виртуальных пользователей**.")
    $lines.Add("")
    $lines.Add("Приблизительная пропускная способность по завершённым пользовательским итерациям составила **$iterationRate итераций/с**. Данный показатель можно использовать как ориентировочную оценку количества пользовательских сценариев, обрабатываемых системой за одну секунду в условиях проведённого теста.")
} else {
    $lines.Add("Стрессовый сценарий не найден среди JSON-отчётов. Проверьте наличие файла `10-auto-stress-limit-summary.json` в папке `reports`.")
}

$lines.Add("")
$lines.Add("## 4. Вывод")
$lines.Add("")
$lines.Add("Полученные результаты позволяют оценить устойчивость серверной части RBS при одновременной работе пользователей. Обычные сценарии показывают поведение отдельных операций при нагрузке 100 VUs, а стрессовый сценарий используется для приближённого определения предельной нагрузки системы.")

Set-Content -Path $outputFile -Value $lines -Encoding UTF8
Write-Host "Markdown report generated: $outputFile"
