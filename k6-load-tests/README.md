# RBS k6 load tests

Готовый набор сценариев для нагрузочного тестирования RBS.

## Запуск

На Windows запустите:

```bat
.\run-all-tests.bat
```

Можно также запустить напрямую через PowerShell:

```powershell
.\run-all-tests.ps1
```

## Нагрузка

- `00-health-check.js` — 100 VUs;
- `01-restaurants-list.js` — 100 VUs;
- `02-restaurant-details.js` — 100 VUs;
- `03-availability.js` — 100 VUs;
- `04-pricing-offer.js` — 100 VUs;
- `05-create-booking.js` — 100 VUs;
- `06-full-booking-flow.js` — 100 VUs;
- `07-mixed-user-flow.js` — 100 VUs;
- `08-cache-check.js` — 100 VUs;
- `09-notification-resilience.js` — 100 VUs;
- `10-auto-stress-limit.js` — постепенный рост до 1000 VUs.

## Важные параметры

Перед запуском проверьте значения в `run-all-tests.ps1`:

- `BASE_URL`;
- `USER_EMAIL`;
- `USER_PASSWORD`;
- `RESTAURANT_ID`;
- `TABLE_ID`;
- `DISH_ID`;
- `VISIT_START`;
- `VISIT_END`.

После выполнения тестов Markdown-отчёт будет создан в файле `reports/README.md`.
