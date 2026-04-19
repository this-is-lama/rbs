# api-gateway

Единая точка входа в систему `RBS`.

## Что делает сервис

- Проверяет access JWT по HS256.
- Валидирует `issuer=user-service`.
- Проверяет claim `token_type=access_token`.
- Маршрутизирует запросы в `user-service`, `restaurant-service`, `booking-service` и `notification-service`.
- Применяет CORS для фронтенда `http://localhost:5173`.
- Логирует входящие HTTP-запросы через `LoggingGlobalFilter`.

## Порт

```text
8080
```

## Маршрутизация

`discovery locator` в текущей версии выключен. Используются явные маршруты из `application.yml`:

- `/api/v1/auth/** -> user-service`
- `/api/v1/users/** -> user-service`
- `/api/v1/restaurants/** -> restaurant-service`
- `/api/v1/bookings/** -> booking-service`
- `/api/v1/notifications/** -> notification-service`

Маршрут для `notification-service` настроен, но в текущем коде сам сервис не публикует REST-контроллеры.

## Публичные маршруты

- `OPTIONS /**`
- `/actuator/health`
- `/actuator/info`
- `/api/v1/auth/**`
- `GET /api/v1/restaurants/**`
- `GET /api/v1/bookings/public/**`
- `/v3/api-docs/**`
- `/swagger-ui/**`
- `/swagger-ui.html`

Все остальные запросы требуют `Authorization: Bearer <access_token>`.

## Конфигурация

Обязательные переменные:

- `JWT_SECRET` — base64-секрет access token.
- `EUREKA_URL` — адрес Eureka.

Основные настройки находятся в `src/main/resources/application.yml`.

## Actuator

Открыты endpoints:

- `health`
- `info`
- `metrics`
- `prometheus`

## Стек

- Spring Boot
- Spring WebFlux
- Spring Cloud Gateway
- Spring Security OAuth2 Resource Server
- Spring Cloud Netflix Eureka Client
- JJWT
- Spring Boot Actuator
- Java 17

## Лицензия

См. корневой `readme.md` и файл `LICENSE`.
