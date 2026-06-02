# booking-service

Сервис управления бронированиями в RBS.

## Что делает сервис

- Создает бронирования по ресторану, столу, временному интервалу и предзаказу блюд.
- Рассчитывает динамический сервисный сбор для бронирований с предзаказом.
- Хранит и проверяет pricing offer перед созданием бронирования.
- Отдает бронирования пользователя и список бронирований ресторана для менеджера или администратора.
- Позволяет отменять, подтверждать и завершать бронирования.
- Публикует события создания и отмены бронирования в Kafka.
- Получает данные ресторана, столов и блюд из `restaurant-service`, а краткие данные пользователей из `user-service`.

## REST API

- `POST /api/v1/bookings`
- `GET /api/v1/bookings/{id}`
- `GET /api/v1/bookings/me`
- `DELETE /api/v1/bookings/{id}/cancel`
- `GET /api/v1/bookings/manager/restaurants/{restId}`
- `GET /api/v1/bookings/public/restaurants/{restaurantId}/tables/{tableId}/availability?date=YYYY-MM-DD`
- `POST /api/v1/bookings/pricing/offers`

Публичным является только маршрут доступности столика:

- `GET /api/v1/bookings/public/**`

Остальные маршруты требуют access token. Подтверждение и завершение бронирований доступны `ROLE_MANAGER` и `ROLE_ADMIN`; менеджер должен иметь доступ к ресторану бронирования.

## Статусы бронирования

- `RESERVED` — бронирование создано пользователем и ожидает подтверждения рестораном.
- `CANCELLED` — бронирование отменено и не участвует в исторической аналитике.

## Динамический сервисный сбор

- Без предзаказа `pricingCharge = 0`, `preorderAmount = 0`, `totalAmount = 0`.
- С предзаказом сервисный сбор рассчитывается по модели спроса и ограничивается границами ресторана.
- Сумма предзаказа не влияет на размер сервисного сбора.
- Итоговая стоимость всегда считается как `totalAmount = preorderAmount + pricingCharge`.
- Историческая аналитика строится только по бронированиям в статусе `RESERVED`; `CANCELLED` не участвует в истории.
- Текущая загрузка столов считается по статусу `RESERVED`.

## Интеграции

### Kafka

- `app.kafka.topics.booking-created`
- `app.kafka.topics.booking-cancelled`

### Feign

- `restaurant-service`
  - `GET /api/v1/restaurants/{restId}/manager-access`
  - `POST /api/v1/restaurants/{restId}/booking-snapshot`
  - `GET /api/v1/restaurants/{restId}/booking-pricing-data`
  - `GET /api/v1/restaurants/{restId}/booking-pricing-summary`
- `user-service`
  - `POST /api/v1/users/briefs`

## Технологии

- Java 17
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
