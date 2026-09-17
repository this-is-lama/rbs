# RBS — Restaurant Booking System

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2024.0.0-blue)
![Build](https://img.shields.io/badge/build-Gradle-02303A)
![License](https://img.shields.io/badge/license-proprietary-lightgrey)

`RBS` — микросервисный backend для бронирования столиков в ресторанах. Проект объединяет аутентификацию по JWT, каталог ресторанов с блюдами, столами и фотографиями, создание бронирований с динамическим сервисным сбором, событийную интеграцию через Kafka и email-уведомления.

## Содержание

- [Обзор возможностей](#обзор-возможностей)
- [Архитектура](#архитектура)
- [Технологический стек](#технологический-стек)
- [Быстрый старт](#быстрый-старт)
- [Переменные окружения](#переменные-окружения)
- [Сервисы подробно](#сервисы-подробно)
  - [api-gateway](#api-gateway)
  - [eureka-server](#eureka-server)
  - [user-service](#user-service)
  - [restaurant-service](#restaurant-service)
  - [booking-service](#booking-service)
  - [notification-service](#notification-service)
  - [common](#common)
- [Полный список API](#полный-список-api)
- [Безопасность и JWT-модель](#безопасность-и-jwt-модель)
- [Фоновые задачи](#фоновые-задачи)
- [Наблюдаемость](#наблюдаемость)
- [Структура проекта](#структура-проекта)
- [Нагрузочное тестирование](#нагрузочное-тестирование)
- [Известные ограничения](#известные-ограничения)
- [Лицензия](#лицензия)

## Обзор возможностей

- Регистрация, логин, access/refresh JWT-токены, смена пароля и ролей.
- Каталог ресторанов: категории, часы работы, контакты, менеджеры.
- Управление столами (в т.ч. массовое создание и редактирование layout) и блюдами.
- Загрузка фотографий ресторанов и блюд через presigned URL в MinIO.
- Проверка доступности стола на дату и создание бронирования с предзаказом блюд.
- Динамический сервисный сбор при бронировании с предзаказом.
- Событийная рассылка email-подтверждений бронирования через Kafka.
- Service discovery через Eureka и единая точка входа через API Gateway.

## Архитектура

| Сервис | Порт | Назначение |
|---|---|---|
| `eureka-server` | 8761 | Service discovery |
| `api-gateway` | 8080 | Единая точка входа, проверка JWT, маршрутизация, CORS |
| `user-service` | 8083 | Регистрация, логин, refresh/logout, профиль, роли |
| `restaurant-service` | 8081 | Рестораны, менеджеры, столы, блюда, фото, Redis, MinIO |
| `booking-service` | 8082 | Бронирования, доступность, динамический сбор, Kafka producer |
| `notification-service` | 8084 | Kafka consumer, статусы сообщений, email |
| `common` | — | Общие DTO, исключения, security-утилиты, локализация |

**Ключевые архитектурные решения:**

- `api-gateway` использует явные маршруты из конфигурации, `discovery locator` выключен; при этом все сервисы регистрируются в Eureka для service-to-service вызовов через OpenFeign.
- У каждого бизнес-сервиса своя база данных в общем контейнере PostgreSQL: `userdb`, `restaurantdb`, `bookingdb`, `notificationdb` (по схеме database-per-service).
- `restaurant-service` использует Redis только как кэш чтения (данные читаются из PostgreSQL, Redis не является источником истины).
- Медиафайлы хранятся в MinIO; бакеты `restaurant-media` и `dish-media` создаются автоматически при полном запуске через docker-compose.
- Создание бронирования публикует событие в Kafka topic `booking-topic`, которое обрабатывает `notification-service` и отправляет HTML-письмо через Thymeleaf/SMTP.
- Между сервисами используется синхронная интеграция через Spring Cloud OpenFeign (например, `booking-service` → `restaurant-service`/`user-service`) и асинхронная — через Kafka (`booking-service` → `notification-service`).

## Технологический стек

| Категория | Технологии |
|---|---|
| Язык / платформа | Java 17, Gradle (multi-module, Kotlin DSL) |
| Web / API | Spring Boot 3.4.1, Spring Web, Spring Cloud Gateway |
| Service discovery | Spring Cloud Netflix Eureka (Server/Client) |
| Межсервисное взаимодействие | Spring Cloud OpenFeign, Apache Kafka (Spring Kafka) |
| Безопасность | Spring Security, OAuth2 Resource Server (JWT/HS256), BCrypt |
| Данные | Spring Data JPA, PostgreSQL 16, Liquibase (booking-service) |
| Кэш | Spring Data Redis (Redis 7) |
| Файлы | MinIO Java SDK |
| Почта | Spring Mail, Thymeleaf-шаблоны |
| Маппинг | MapStruct |
| Документация API | springdoc-openapi (Swagger UI) |
| Наблюдаемость | Spring Boot Actuator, Micrometer/Prometheus |
| Инфраструктура | Docker, Docker Compose |
| Нагрузочное тестирование | k6 |

## Быстрый старт

### Требования

Docker и Docker Compose для запуска всего стека; JDK 17 — для локальной сборки/запуска отдельных сервисов через Gradle.

### Полный стек в Docker

```bash
docker compose -f docker-compose.local.yml up --build
```

Поднимаются:

- 4 контейнера PostgreSQL (по одному на `user`, `restaurant`, `booking`, `notification`);
- Redis;
- Kafka;
- MinIO + `minio-init` (автосоздание бакетов);
- все сервисы приложения (`eureka-server`, `api-gateway`, `user-service`, `restaurant-service`, `booking-service`, `notification-service`).

Для продакшен-конфигурации используется `docker-compose.prod.yml` (переменные — из `.env.prod`). Каждый сервис собирается общим `Dockerfile` с build-аргументом `MODULE` (multi-stage сборка на `gradle:8.8-jdk17`, рантайм — `eclipse-temurin:17-jre-jammy`).

### Только инфраструктура

```bash
docker compose -f docker-compose.infra.yml up -d
```

Поднимает только `postgres`, `redis`, `kafka` и `minio` (переменные — из `.env.infra`). Создание бакетов MinIO и запуск сервисов приложения в этом режиме нужно выполнять отдельно.

### Локальный запуск сервисов (Gradle)

```bash
./gradlew :eureka-server:bootRun
./gradlew :user-service:bootRun
./gradlew :restaurant-service:bootRun
./gradlew :booking-service:bootRun
./gradlew :notification-service:bootRun
./gradlew :api-gateway:bootRun
```

На Windows — аналогично через `gradlew.bat`. Рекомендуемый порядок запуска: `eureka-server` → бизнес-сервисы → `api-gateway`.

## Переменные окружения

Значения задаются в `.env.local` / `.env.prod` / `.env.infra` в зависимости от режима запуска.

| Группа | Переменные | Где используется |
|---|---|---|
| База данных | `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASS`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `USER_DB_HOST/PORT`, `RESTAURANT_DB_HOST/PORT`, `BOOKING_DB_HOST/PORT`, `NOTIFICATION_DB_HOST/PORT`, `MAX_POOL_SIZE`, `MIN_IDLE` | все бизнес-сервисы |
| Service discovery | `EUREKA_URL` | все сервисы |
| JWT | `JWT_SECRET`, `JWT_REFRESH_SECRET` (base64-строки) | `user-service`, `api-gateway`, `restaurant-service`, `booking-service` |
| Kafka | `KAFKA_BOOTSTRAP`, `KAFKA_CLUSTER_ID` | `booking-service`, `notification-service` |
| Redis | `REDIS_HOST`, `REDIS_PORT` | `restaurant-service` |
| MinIO | `MINIO_ENDPOINT`, `MINIO_ROOT_USER`, `MINIO_ROOT_PASSWORD`, `MINIO_PUBLIC_BASE_URL`, `MINIO_BUCKET` | `restaurant-service` |
| Почта | `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_SMTP_AUTH`, `MAIL_SMTP_STARTTLS_*`, `MAIL_DEBUG` | `notification-service` |
| Прочее | `SPRING_PROFILES_ACTIVE`, `CORS_ALLOWED_ORIGIN`, `MANAGEMENT_HEALTH_MAIL_ENABLED` | `api-gateway`, `notification-service` |

## Сервисы подробно

### api-gateway

Единая точка входа в систему.

- Проверяет access JWT по HS256, валидирует `issuer=user-service` и claim `token_type=access_token`.
- Маршрутизирует запросы в `user-service`, `restaurant-service`, `booking-service`, `notification-service` по явным маршрутам из `application.yml` (`discovery locator` выключен).
- Применяет CORS для фронтенда (`http://localhost:5173` в локальной конфигурации).
- Логирует входящие HTTP-запросы через `LoggingGlobalFilter`.

Маршрут для `notification-service` настроен, но сам сервис не публикует REST-контроллеры — работает только как фоновый consumer.

### eureka-server

Сервис регистрации и обнаружения. Принимает регистрацию от всех остальных сервисов и используется для service-to-service вызовов. Работает как standalone discovery node (`register-with-eureka=false`, `fetch-registry=false`) — сам себя в реестре не регистрирует.

### user-service

Аутентификация, пользователи, роли.

- Регистрация с проверкой уникальности email; напрямую зарегистрироваться как `ROLE_ADMIN` нельзя.
- Логин через `AuthenticationManager` + `UsernamePasswordAuthenticationToken`, выдача пары access/refresh токенов.
- Refresh-токен привязан к JTI, который хранится в БД и деактивируется при каждом использовании и при logout — защита от повторного использования украденного refresh-токена.
- Смена пароля, смена роли по id (для админ-операций), поиск/сводки пользователей для других сервисов (`lookup`, `summaries`, `briefs`).

### restaurant-service

Рестораны, менеджеры, столы, блюда, фотографии.

- CRUD ресторанов: базовые данные, часы работы (`WorkingHoursEntity`), контакты (`ContactEntity`), категории, активность.
- Управление менеджерами: при добавлении менеджера пользователю выставляется `ROLE_MANAGER` (вызов `user-service` через Feign); если после удаления связей менеджер не привязан ни к одному ресторану — роль возвращается на `ROLE_USER`.
- Столы: создание (в т.ч. массовое, `/tables/all`), обновление, обновление layout целиком, удаление.
- Блюда: CRUD с привязкой к ресторану.
- Фото: presigned upload → подтверждение → раздача; поддерживаемые типы — `image/jpeg`, `image/png`, `image/webp`; категории — `BANNER`, `SCHEME`, `GALLERY`; статусы жизненного цикла — `PENDING → ACTIVE`, а также `EXPIRED`/`DELETING` для неподтверждённых и удаляемых файлов.
- Кэширование в Redis (`restaurant-service::` prefix) с разными TTL: рестораны — 5 мин, блюда/столы — 2–5 мин, фото — 1 мин, `managerAccess` — 30 мин, `restaurantBookingTable` — 2 мин.
- Отдаёт `booking-service` snapshot данных ресторана/столов и данные для расчёта динамического сбора (`booking-pricing-data`, `booking-pricing-summary`).

### booking-service

Бронирования и ценообразование.

- Создание бронирования по ресторану, столу, временно́му интервалу и опциональному предзаказу блюд.
- Проверка доступности стола на дату (публичный эндпоинт).
- Расчёт и хранение pricing offer перед подтверждением бронирования.
- Статусы бронирования: `RESERVED` (создано, ожидает подтверждения) и `CANCELLED` (отменено, не участвует в исторической аналитике); текущая загрузка столов считается только по `RESERVED`.
- Динамический сервисный сбор: без предзаказа `pricingCharge = 0`; с предзаказом сбор рассчитывается по модели спроса и ограничивается настройками ресторана; сумма предзаказа не влияет на размер сбора; `totalAmount = preorderAmount + pricingCharge`.
- Публикует в Kafka события создания и отмены бронирования (`app.kafka.topics.booking-created`, `app.kafka.topics.booking-cancelled`).
- Получает данные ресторана/столов/блюд из `restaurant-service` и краткие данные пользователей из `user-service` через Feign.
- Хранение данных — PostgreSQL, миграции — Liquibase.

### notification-service

Фоновая обработка событий бронирования и отправка email.

- Слушает Kafka topic `booking-topic`, потребитель — consumer group `my-consumer`.
- Дедуплицирует входящие сообщения по `bookingId`.
- Хранит статус обработки каждого сообщения: `CREATED → PROCESSING → DONE`, либо `FAILED` при ошибке.
- Отправляет HTML-письмо (Thymeleaf-шаблон `booking-confirm.html`, встроенный логотип) через SMTP; в текущей конфигурации — под Gmail (`smtp.gmail.com:587`).
- Планировщики: повторная отправка неуспешных сообщений — каждые 10 минут (`max-attempts=5`); очистка сообщений в статусе `DONE` — каждые 60 минут.
- Не публикует собственных REST-эндпоинтов — работает исключительно как consumer и background worker.

### common

Не является отдельным сервисом, подключается как библиотека ко всем бизнес-сервисам:

- `ApiError`, `ApiException` и набор типовых исключений (`ConflictException`, `ForbiddenException` и др.), `CommonExceptionHandler` — единый формат ошибок.
- `AuthUtil`, `JwtDecoderFactory`, `JwtAuthConverterFactory`, `JwtClaims` — общая инфраструктура для проверки JWT в каждом сервисе.
- `CommonAuthenticationEntryPoint`, `CommonAccessDeniedHandler` — единая обработка 401/403.
- Локализация сообщений об ошибках: `messages_ru.properties`, `messages_en.properties`.

## Полный список API

Ниже — весь набор HTTP-эндпоинтов, доступных через `api-gateway`. Актуальная спецификация — в Swagger UI каждого сервиса (см. [Наблюдаемость](#наблюдаемость)).

### Аутентификация и пользователи (`user-service`)

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

### Рестораны (`restaurant-service`)

**Рестораны**
- `GET /api/v1/restaurants`
- `GET /api/v1/restaurants/{id}`
- `GET /api/v1/restaurants/categories`
- `GET /api/v1/restaurants/my`
- `POST /api/v1/restaurants`
- `PUT /api/v1/restaurants/{id}`
- `PATCH /api/v1/restaurants/{id}/active`
- `DELETE /api/v1/restaurants/{id}`

**Менеджеры**
- `POST /api/v1/restaurants/{restId}/managers/{managerId}`
- `GET /api/v1/restaurants/{restId}/managers`
- `DELETE /api/v1/restaurants/{restId}/managers/{managerId}`

**Столы**
- `POST /api/v1/restaurants/{restId}/tables`
- `POST /api/v1/restaurants/{restId}/tables/all`
- `PUT /api/v1/restaurants/{restId}/tables/{id}`
- `PUT /api/v1/restaurants/{restId}/tables/layout`
- `DELETE /api/v1/restaurants/{restId}/tables/{id}`
- `GET /api/v1/restaurants/{restId}/tables/{id}`

**Блюда**
- `POST /api/v1/restaurants/{restId}/dishes`
- `PUT /api/v1/restaurants/{restId}/dishes/{id}`
- `DELETE /api/v1/restaurants/{restId}/dishes/{id}`
- `GET /api/v1/restaurants/{restId}/dishes/{id}`

**Фото** (для `container` = `restaurants` или `dishes`)
- `POST /api/v1/{container}/{containerId}/photos/uploads`
- `POST /api/v1/{container}/{containerId}/photos/confirm`
- `DELETE /api/v1/{container}/{containerId}/photos/delete`

**Служебные (service-to-service)**
- `GET /api/v1/restaurants/{restId}/manager-access`
- `POST /api/v1/restaurants/{restId}/booking-snapshot`
- `GET /api/v1/restaurants/{restId}/booking-pricing-data`
- `GET /api/v1/restaurants/{restId}/booking-pricing-summary`

### Бронирования (`booking-service`)

- `POST /api/v1/bookings`
- `GET /api/v1/bookings/{id}`
- `GET /api/v1/bookings/me`
- `DELETE /api/v1/bookings/{id}/cancel`
- `GET /api/v1/bookings/manager/restaurants/{restId}`
- `GET /api/v1/bookings/public/restaurants/{restaurantId}/tables/{tableId}/availability?date=YYYY-MM-DD`
- `POST /api/v1/bookings/pricing/offers`

### Уведомления (`notification-service`)

Публичного REST API нет — сервис работает только как фоновый Kafka consumer.

## Безопасность и JWT-модель

- Access-токен валидируется в `api-gateway` и во внутренних сервисах (HS256), issuer зафиксирован как `user-service`, обязателен claim `token_type=access_token`.
- Access-токен содержит claim'ы: `roles`, `email`, `name` (`фамилия + имя`), `token_type=access_token`.
- Refresh-токен содержит: `jti`, `token_type=refresh_token`; подписывается отдельным секретом (`JWT_REFRESH_SECRET`), отличным от access.
- TTL из конфигурации: access — `1h`, refresh — `1d`. `JWT_SECRET`/`JWT_REFRESH_SECRET` передаются как base64-строки.
- Refresh-токен деактивируется по JTI при каждом обновлении (`refresh`) и при `logout` — использованный или отозванный refresh-токен повторно не работает.

**Публично доступные маршруты (без токена):**
- `/api/v1/auth/**`
- `GET /api/v1/restaurants/**`
- `GET /api/v1/bookings/public/**`
- `/actuator/health`
- Swagger-эндпоинты сервисов, где подключён springdoc

**Роли:** `ROLE_USER`, `ROLE_MANAGER`, `ROLE_ADMIN`. Операции управления ресторанами/столами/блюдами доступны `ROLE_MANAGER`/`ROLE_ADMIN`; подтверждение и завершение бронирований — тем же ролям, при условии, что менеджер привязан к конкретному ресторану.

## Фоновые задачи

| Сервис | Задача | Периодичность |
|---|---|---|
| `restaurant-service` | Очистка просроченных (`EXPIRED`) и удаляемых (`DELETING`) фото | каждые 10 минут |
| `user-service` | Очистка деактивированных и просроченных refresh JTI | каждый 1 час |
| `notification-service` | Повторная отправка неуспешных email (до `max-attempts=5`) | каждые 10 минут |
| `notification-service` | Очистка сообщений в статусе `DONE` | каждые 60 минут |

## Наблюдаемость

- Eureka UI: `http://localhost:8761`
- Gateway health: `http://localhost:8080/actuator/health`
- Swagger UI:
  - `user-service` — `http://localhost:8083/swagger-ui/index.html`
  - `restaurant-service` — `http://localhost:8081/swagger-ui/index.html`
  - `booking-service` — `http://localhost:8082/swagger-ui/index.html`

Actuator во всех сервисах открывает `health`, `info`, `metrics`, `prometheus` — метрики готовы к сбору Prometheus/Grafana.

## Структура проекта

```
RBS/
├── api-gateway/            # маршрутизация, проверка JWT, CORS
├── eureka-server/          # service discovery
├── user-service/           # аутентификация, пользователи, роли
├── restaurant-service/     # рестораны, столы, блюда, фото, Redis, MinIO
├── booking-service/        # бронирования, доступность, динамический сбор
├── notification-service/   # Kafka consumer, email-уведомления
├── common/                 # общий модуль: ошибки, security, локализация
├── k6-load-tests/          # сценарии нагрузочного тестирования (k6)
├── build.gradle.kts        # корневая Gradle-конфигурация (multi-module)
├── settings.gradle.kts     # список подмодулей
├── Dockerfile              # общий multi-stage Dockerfile (аргумент MODULE)
├── docker-compose.local.yml
├── docker-compose.prod.yml
├── docker-compose.infra.yml
└── LICENSE
```

Каждый сервис — отдельный Gradle-подмодуль со своим `readme.md`, где описаны его эндпоинты, сущности и конфигурация подробнее, чем здесь.

## Нагрузочное тестирование

В `k6-load-tests/` — готовые сценарии на k6 (по 100 виртуальных пользователей на сценарий): health-check, список ресторанов, детали ресторана, проверка доступности, расчёт pricing offer, создание бронирования, полный флоу бронирования, смешанный пользовательский флоу, проверка кэша, устойчивость уведомлений.

Запуск на Windows:

```powershell
./k6-load-tests/run-all-tests.ps1
```

или

```bat
run-all-tests.bat
```

## Известные ограничения

- Автоматических тестов (unit/integration) в текущей версии нет.
- `docker-compose.yml` без суффикса отсутствует — используются профильные файлы (`local`/`prod`/`infra`).
- SMTP в `notification-service` захардкожен под Gmail; для другого провайдера нужно менять `application.yml`.

## Лицензия

Проект не является open-source. Условия использования — в [LICENSE](LICENSE).