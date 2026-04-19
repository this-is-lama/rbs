# booking-service

Сервис управления бронированиями.

## Что делает сервис

- Создает бронирование по ресторану, столу, интервалу времени и списку блюд.
- Отдает бронирование по `id` и список бронирований текущего пользователя.
- Позволяет отменить бронирование.
- Отдает список бронирований ресторана для менеджера или администратора.
- Публикует событие `BookingCreatedEvent` в Kafka.
- Получает данные о ресторане из `restaurant-service` и краткие данные о пользователях из `user-service` через OpenFeign.

## Порт

```text
8082
```

## REST API

- `POST /api/v1/bookings`
- `GET /api/v1/bookings/{id}`
- `GET /api/v1/bookings/me`
- `DELETE /api/v1/bookings/{id}/cancel`
- `GET /api/v1/bookings/manager/restaurants/{restId}`
- `GET /api/v1/bookings/public/restaurants/{restaurantId}/tables/{tableId}/availability?date=YYYY-MM-DD`

Публичным является только маршрут доступности столика:

- `GET /api/v1/bookings/public/**`

Все остальные маршруты требуют access token.

## Данные и модель

В сервисе есть собственная БД `bookingdb`.

Основные сущности текущей версии:

- `BookingEntity`
- `RestaurantEntity`
- `TableEntity`
- `DishEntity`

Статусы бронирования:

- `RESERVED`
- `CANCELLED`

При создании брони валидируется:

- наличие `restaurantId` и `tableId`
- интервал `startAt/endAt`
- бронирование минимум за 1 час до начала
- длительность минимум 1 час
- число гостей от `1` до `50`
- список блюд не больше `50` позиций

## Интеграции

### Kafka

- property: `app.kafka.topics.booking-created`
- текущее значение topic: `booking-topic`

### Feign

- `restaurant-service`
  - `GET /api/v1/restaurants/{restId}/manager-access`
  - `POST /api/v1/restaurants/{restId}/booking-snapshot`
- `user-service`
  - `POST /api/v1/users/briefs`

## Конфигурация

Обязательные переменные окружения:

- `DB_HOST`
- `DB_PORT`
- `DB_USER`
- `DB_PASS`
- `JWT_SECRET`
- `KAFKA_BOOTSTRAP`
- `EUREKA_URL`

Основные настройки лежат в `src/main/resources/application.yml`.

## Технологии

- Spring Boot
- Spring Web
- Spring Security OAuth2 Resource Server
- Spring Data JPA
- Spring Validation
- Spring Cloud OpenFeign
- Spring Cloud Netflix Eureka Client
- Spring Kafka
- PostgreSQL
- Liquibase
- MapStruct
- Springdoc OpenAPI
- Java 17

## Swagger и Actuator

- Swagger UI: `http://localhost:8082/swagger-ui/index.html`
- OpenAPI: `http://localhost:8082/v3/api-docs`

Открыты actuator endpoints:

- `health`
- `info`
- `metrics`
- `prometheus`

## Лицензия

См. корневой `readme.md` и файл `LICENSE`.
