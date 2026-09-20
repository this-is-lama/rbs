# booking-service

Сервис управления бронированиями в RBS.

## Что делает сервис

- Создает бронирования по ресторану, столу, временному интервалу и предзаказу блюд.
- Рассчитывает сервисный сбор для бронирований с предзаказом по фиксированному коэффициенту.
- Запрещает пересекающиеся по времени бронирования одного стола на уровне БД (exclusion constraint PostgreSQL).
- Отдает бронирования пользователя и список бронирований ресторана для менеджера или администратора.
- Позволяет отменять бронирования (владельцем или менеджером/администратором ресторана).
- Публикует события создания и отмены бронирования в Kafka.
- Получает данные ресторана, стола и блюд из `restaurant-service`, а данные пользователей из `user-service`.

## REST API

- `POST /api/v1/bookings`
- `GET /api/v1/bookings/{id}`
- `GET /api/v1/bookings/me`
- `DELETE /api/v1/bookings/{id}/cancel`
- `GET /api/v1/bookings/manager/restaurants/{restId}`
- `GET /api/v1/bookings/public/restaurants/{restaurantId}/tables/{tableId}/availability?date=YYYY-MM-DD`

Публичным является только маршрут доступности столика:

- `GET /api/v1/bookings/public/**`

Остальные маршруты требуют access token. Отмена чужого бронирования с обязательным указанием причины доступна `ROLE_MANAGER` и `ROLE_ADMIN`; менеджер должен иметь доступ к ресторану бронирования.

## Статусы бронирования

- `RESERVED` — бронирование создано пользователем и ожидает подтверждения рестораном.
- `CANCELLED` — бронирование отменено и не участвует в исторической аналитике.

## Сервисный сбор

- Коэффициент задаётся конфигурацией `pricing.charge-coefficient` (сейчас `0.1`, то есть 10%).
- Без предзаказа `preorderAmount = 0` и `pricingCharge = 0`.
- С предзаказом `pricingCharge = preorderAmount * chargeCoefficient`.
- Итоговая стоимость всегда считается как `totalAmount = preorderAmount + pricingCharge`.
- Историческая аналитика строится только по бронированиям в статусе `RESERVED`; `CANCELLED` не участвует в истории.
- Текущая загрузка столов считается по статусу `RESERVED`.

## Целостность данных

Пересечение бронирований одного стола по времени исключается PostgreSQL exclusion constraint (`excl_bookings_table_time_overlap`, расширение `btree_gist`) — конфликт по времени невозможен даже при гонке запросов, не только на уровне бизнес-логики.

## Интеграции

### Kafka

- `app.kafka.topics.booking-created` → `booking-created-topic`
- `app.kafka.topics.booking-cancelled` → `booking-cancelled-topic`

### Feign

- `restaurant-service`
  - `GET /api/v1/restaurants/{restId}/manager-access`
  - `POST /api/v1/restaurants/{restId}/booking-snapshot`
- `user-service`
  - `GET /api/v1/users/{id}`
  - `POST /api/v1/users` (batch-получение по списку id)

## Технологии

- Java 21
- Spring Boot
- Spring Web
- Spring Security OAuth2 Resource Server
- Spring Data JPA
- Spring Cloud OpenFeign
- Spring Kafka
- PostgreSQL
- Liquibase
- Lombok
- MapStruct
