# user-service

Сервис аутентификации, управления профилем пользователя и ролями.

## Что делает сервис

- Регистрирует пользователей.
- Выполняет логин и выдает пару `access + refresh`.
- Обновляет токены по refresh token.
- Делает logout через деактивацию refresh JTI.
- Отдает и обновляет профиль текущего пользователя.
- Меняет пароль текущего пользователя.
- Позволяет менеджеру или администратору менять роли и запрашивать краткие данные пользователей.
- Периодически очищает неактивные и просроченные refresh JTI.

## Порт

```text
8083
```

## REST API

### Auth

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`

### Users

- `GET /api/v1/users/me`
- `PUT /api/v1/users/me`
- `PATCH /api/v1/users/me/password`
- `POST /api/v1/users/change-role-by-id`
- `GET /api/v1/users/lookup`
- `POST /api/v1/users/summaries`
- `POST /api/v1/users/briefs`

Публичными являются только `/api/v1/auth/**` и `/actuator/health`.

## JWT-модель

### Access token

Содержит:

- `sub`
- `roles`
- `email`
- `name`
- `token_type=access_token`
- `issuer=user-service`

### Refresh token

Содержит:

- `sub`
- `jti`
- `token_type=refresh_token`
- `issuer=user-service`

Текущие lifetimes из конфигурации:

- access: `1h`
- refresh: `1d`

`JWT_SECRET` и `JWT_REFRESH_SECRET` должны быть base64-строками.

## Роли

Реальные enum-значения:

- `ROLE_USER`
- `ROLE_MANAGER`
- `ROLE_ADMIN`

## Данные

Собственная БД: `userdb`.

Основные таблицы:

- `users`
- `refresh_jtis`

`UserEntity` хранит:

- `name`
- `surname`
- `dateOfBirth`
- `phone`
- `email`
- `passwordHash`
- `role`
- `enabled`
- `createdAt`
- `updatedAt`

Фоновая очистка refresh JTI запускается каждый `1` час.

## Интеграции

Сервис не использует внешние REST-клиенты, но предоставляет внутренние endpoints для других модулей:

- `POST /api/v1/users/change-role-by-id`
- `GET /api/v1/users/lookup`
- `POST /api/v1/users/summaries`
- `POST /api/v1/users/briefs`

Их используют `restaurant-service` и `booking-service`.

## Конфигурация

Обязательные переменные окружения:

- `DB_HOST`
- `DB_PORT`
- `DB_USER`
- `DB_PASS`
- `JWT_SECRET`
- `JWT_REFRESH_SECRET`
- `EUREKA_URL`

Основные настройки лежат в `src/main/resources/application.yml`.

## Технологии

- Spring Boot
- Spring Web
- Spring Security OAuth2 Resource Server
- Spring Data JPA
- Spring Validation
- Spring Cloud Netflix Eureka Client
- JJWT
- PostgreSQL
- MapStruct
- Springdoc OpenAPI
- Java 17

## Swagger и Actuator

- Swagger UI: `http://localhost:8083/swagger-ui/index.html`
- OpenAPI: `http://localhost:8083/v3/api-docs`

Открыты actuator endpoints:

- `health`
- `info`
- `metrics`
- `prometheus`

## Лицензия

См. корневой `readme.md` и файл `LICENSE`.
