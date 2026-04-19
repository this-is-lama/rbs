# RBS — Restaurant Booking System

`RBS` — микросервисный backend для бронирования столиков в ресторанах. Текущая версия проекта объединяет аутентификацию и JWT, каталог ресторанов с блюдами, столами и фотографиями, создание бронирований, Kafka-события и email-уведомления.

## Состав проекта

| Модуль | Порт | Назначение |
| --- | --- | --- |
| `eureka-server` | `8761` | Service discovery |
| `api-gateway` | `8080` | Единая точка входа, JWT-проверка и маршрутизация |
| `restaurant-service` | `8081` | Рестораны, менеджеры, столы, блюда, фото, Redis, MinIO |
| `booking-service` | `8082` | Бронирования, публичная проверка доступности, Kafka producer |
| `user-service` | `8083` | Регистрация, логин, refresh/logout, профиль и роли |
| `notification-service` | `8084` | Kafka consumer, хранение статусов сообщений, отправка email |
| `common` | — | Общие ошибки, security-утилиты, JWT-конвертеры и локализация |

## Архитектура

- `api-gateway` использует явные маршруты из конфигурации. `discovery locator` выключен.
- Все сервисы регистрируются в Eureka.
- Для бизнес-данных используется один контейнер PostgreSQL с отдельными БД: `userdb`, `restaurantdb`, `bookingdb`, `notificationdb`.
- `restaurant-service` использует Redis только для кэша чтения.
- Медиа хранятся в MinIO. В полном `docker-compose.yml` автоматически создаются bucket'ы `restaurant-media` и `dish-media`.
- Событие о создании бронирования публикуется в Kafka topic `booking-topic`.
- `notification-service` получает это событие и отправляет HTML-письмо.

## Что реально есть в API

### Аутентификация и пользователи

Через `user-service` и gateway доступны:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/users/me`
- `PUT /api/v1/users/me`
- `PATCH /api/v1/users/me/password`
- `POST /api/v1/users/change-role-by-id`
- `GET /api/v1/users/lookup`
- `POST /api/v1/users/summaries`
- `POST /api/v1/users/briefs`

### Рестораны

Через `restaurant-service` и gateway доступны:

- `GET /api/v1/restaurants`
- `GET /api/v1/restaurants/{id}`
- `GET /api/v1/restaurants/categories`
- `POST /api/v1/restaurants`
- `PUT /api/v1/restaurants/{id}`
- `PATCH /api/v1/restaurants/{id}/active`
- `DELETE /api/v1/restaurants/{id}`
- `GET /api/v1/restaurants/my`
- `POST /api/v1/restaurants/{restId}/managers/{managerId}`
- `GET /api/v1/restaurants/{restId}/managers`
- `DELETE /api/v1/restaurants/{restId}/managers/{managerId}`
- `POST /api/v1/restaurants/{restId}/tables`
- `POST /api/v1/restaurants/{restId}/tables/all`
- `PUT /api/v1/restaurants/{restId}/tables/{id}`
- `PUT /api/v1/restaurants/{restId}/tables/layout`
- `DELETE /api/v1/restaurants/{restId}/tables/{id}`
- `GET /api/v1/restaurants/{restId}/tables/{id}`
- `POST /api/v1/restaurants/{restId}/dishes`
- `PUT /api/v1/restaurants/{restId}/dishes/{id}`
- `DELETE /api/v1/restaurants/{restId}/dishes/{id}`
- `GET /api/v1/restaurants/{restId}/dishes/{id}`
- `POST /api/v1/{container:restaurants|dishes}/{containerId}/photos/uploads`
- `POST /api/v1/{container:restaurants|dishes}/{containerId}/photos/confirm`
- `DELETE /api/v1/{container:restaurants|dishes}/{containerId}/photos/delete`

Для внутренних интеграций используются:

- `GET /api/v1/restaurants/{restId}/manager-access`
- `POST /api/v1/restaurants/{restId}/booking-snapshot`

### Бронирования

Через `booking-service` и gateway доступны:

- `POST /api/v1/bookings`
- `GET /api/v1/bookings/{id}`
- `GET /api/v1/bookings/me`
- `DELETE /api/v1/bookings/{id}/cancel`
- `GET /api/v1/bookings/manager/restaurants/{restId}`
- `GET /api/v1/bookings/public/restaurants/{restaurantId}/tables/{tableId}/availability?date=YYYY-MM-DD`

### Уведомления

`notification-service` в текущем коде не публикует публичный REST API. Он работает как фоновый consumer Kafka.

## Безопасность

- Access JWT валидируется в `api-gateway` и во внутренних сервисах.
- Issuer фиксирован как `user-service`.
- Gateway принимает только токены с `token_type=access_token`.
- Публично открыты:
  - `/api/v1/auth/**`
  - `GET /api/v1/restaurants/**`
  - `GET /api/v1/bookings/public/**`
  - `/actuator/health`
  - Swagger endpoints тех сервисов, где подключен springdoc

Роли в системе:

- `ROLE_USER`
- `ROLE_MANAGER`
- `ROLE_ADMIN`

## JWT-модель

`user-service` выпускает:

- Access token с claim'ами `roles`, `email`, `name`, `token_type=access_token`
- Refresh token с claim'ами `jti`, `token_type=refresh_token`

Текущие TTL из конфигурации:

- access token: `1h`
- refresh token: `1d`

`JWT_SECRET` и `JWT_REFRESH_SECRET` должны быть переданы как base64-строки.

## Kafka и фоновые задачи

- `booking-service` публикует событие `BookingCreatedEvent` в topic `booking-topic`.
- `notification-service` читает этот topic consumer group'ой `my-consumer`.
- `notification-service` хранит статусы сообщений: `CREATED`, `PROCESSING`, `FAILED`, `DONE`.
- Повторная отправка писем запускается каждые `10` минут.
- Очистка `DONE`-сообщений запускается каждые `60` минут.
- `restaurant-service` очищает просроченные и удаляемые фото каждые `10` минут.
- `user-service` очищает деактивированные и просроченные refresh JTI каждый `1` час.

## Запуск

### Полный стек в Docker

```bash
docker compose up --build
```

Поднимутся:

- PostgreSQL
- Redis
- Kafka
- MinIO
- `minio-init`
- все сервисы приложения

### Только инфраструктура

```bash
docker compose -f docker-compose.infra.yml up -d
```

Этот вариант поднимает только `postgres`, `redis`, `kafka` и `minio`. Создание bucket'ов и запуск сервисов нужно делать отдельно.

### Локальный запуск сервисов

В PowerShell:

```powershell
.\gradlew.bat :eureka-server:bootRun
.\gradlew.bat :user-service:bootRun
.\gradlew.bat :restaurant-service:bootRun
.\gradlew.bat :booking-service:bootRun
.\gradlew.bat :notification-service:bootRun
.\gradlew.bat :api-gateway:bootRun
```

## Минимальные переменные окружения

- `DB_HOST`
- `DB_PORT`
- `DB_USER`
- `DB_PASS`
- `EUREKA_URL`
- `JWT_SECRET`
- `JWT_REFRESH_SECRET`
- `KAFKA_BOOTSTRAP`
- `KAFKA_CLUSTER_ID`
- `REDIS_HOST`
- `REDIS_PORT`
- `MINIO_ENDPOINT`
- `MINIO_ROOT_USER`
- `MINIO_ROOT_PASSWORD`
- `MINIO_PUBLIC_BASE_URL`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`

## Документация и наблюдаемость

- Eureka UI: `http://localhost:8761`
- Gateway health: `http://localhost:8080/actuator/health`
- Swagger UI:
  - `http://localhost:8081/swagger-ui/index.html`
  - `http://localhost:8082/swagger-ui/index.html`
  - `http://localhost:8083/swagger-ui/index.html`

Actuator в сервисах открывает:

- `health`
- `info`
- `metrics`
- `prometheus`

## Common module

`common` не является отдельным сервисом. Сейчас модуль содержит:

- `ApiError`
- `ApiException` и набор типовых исключений
- `CommonExceptionHandler`
- `AuthUtil`
- `JwtDecoderFactory`, `JwtAuthConverterFactory`, `JwtClaims`
- общие сообщения локализации `messages_ru.properties` и `messages_en.properties`

## Лицензия

Проект не позиционируется как open-source. Подробные условия использования см. в [LICENSE](LICENSE).
