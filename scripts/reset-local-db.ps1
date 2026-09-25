# Пересоздаёт ЛОКАЛЬНЫЕ базы user/restaurant/booking/notification и данные Grafana.
# Нужно, когда Liquibase не может накатить миграции на старые базы.
# ВНИМАНИЕ: локальные пользователи, рестораны и брони будут удалены. На проде не запускать.
# Запуск из корня проекта:  powershell -ExecutionPolicy Bypass -File scripts\reset-local-db.ps1

$ErrorActionPreference = "Stop"
$compose = @("compose", "-f", "docker-compose.local.yml")
$services = @("user-service", "restaurant-service", "booking-service", "notification-service", "grafana",
              "postgres-user", "postgres-restaurant", "postgres-booking", "postgres-notification")
$volumes = @("pg_user_data", "pg_restaurant_data", "pg_booking_data", "pg_notification_data", "grafana_data")

$answer = Read-Host "Удалить локальные базы user/restaurant/booking/notification и данные Grafana? (y/n)"
if ($answer -ne "y") { Write-Host "Отменено"; exit 0 }

Write-Host "`n1/3 Останавливаю и удаляю контейнеры..." -ForegroundColor Cyan
docker @compose stop @services
docker @compose rm -f @services

Write-Host "`n2/3 Удаляю тома с данными..." -ForegroundColor Cyan
$project = (docker @compose config --format json | ConvertFrom-Json).name
foreach ($v in $volumes) {
    $full = "${project}_$v"
    if (docker volume ls -q --filter "name=^${full}$") {
        docker volume rm $full | Out-Null
        Write-Host "  удалён $full"
    } else {
        Write-Host "  $full не найден, пропускаю"
    }
}

Write-Host "`n3/3 Запускаю всё заново..." -ForegroundColor Cyan
docker @compose up -d

Write-Host "`nГотово. Через 1-2 минуты сервисы появятся в Eureka (http://localhost:8761), Grafana - http://localhost:3000" -ForegroundColor Green
