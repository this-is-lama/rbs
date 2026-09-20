# RBS — Restaurant Booking System

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.8-brightgreen)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.3-blue)
![Build](https://img.shields.io/badge/build-Gradle-02303A)
![License](https://img.shields.io/badge/license-proprietary-lightgrey)

`RBS` — микросервисный backend для бронирования столиков в ресторанах. Проект объединяет аутентификацию по JWT, каталог ресторанов с блюдами, столами и фотографиями, создание бронирований с сервисным сбором по фиксированному коэффициенту, событийную интеграцию через Kafka и email-уведомления.

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
- Сервисный сбор по фиксированному коэффициенту при бронировании с предзаказом.
- Запрет пересекающихся бронирований одного стола на уровне БД (exclusion constraint PostgreSQL).
- Событийная рассылка email-подтверждений и уведомлений об отмене бронирования через Kafka.
- Service discovery через Eureka и единая точка входа через API Gateway.

## Архитектура

| Сервис | Порт | Назначение |
|---|---|---|
| `eureka-server` | 8761 | Service discovery |
| `api-gateway` | 8080 | Единая точка входа, проверка JWT, маршрутизация, CORS |
| `user-service` | 8083 | Регистрация, логин, refresh/logout, профиль, роли |
| `restaurant-service` | 8081 | Рестораны, менеджеры, столы, блюда, фото, Redis, MinIO |
| `booking-service` | 8082 | Бронирования, доступность, сервисный сбор, Kafka producer |
| `notification-service` | 8084 | Kafka consumer, статусы сообщений, email |
| `common` | — | Общие DTO, исключения, security-утилиты, локализация |

**Ключевые архитектурные решения:**

- `api-gateway` использует явные маршруты из конфигурации, `discovery locator` выключен; при этом все сервисы регистрируются в Eureka для service-to-service вызовов через OpenFeign.
- У каждого бизнес-сервиса своя база данных в общем контейнере PostgreSQL: `userdb`, `restaurantdb`, `bookingdb`, `notificationdb` (по схеме database-per-service).
- `restaurant-service` использует Redis только как кэш чтения (данные читаются из PostgreSQL, Redis не является источником истины).
- Медиафайлы хранятся в MinIO; бакеты `restaurant-media` и `dish-media` создаются автоматически при полном запуске через docker-compose.
- Создание и отмена бронирования публикуют отдельные события в Kafka (`booking-created-topic`, `booking-cancelled-topic`), которые обрабатывает `notification-service` и отправляет соответствующее HTML-письмо через Thymeleaf/SMTP.
- Пересечение бронирований одного стола по времени исключается на уровне PostgreSQL exclusion constraint (`booking-service`, расширение `btree_gist`), а не только проверкой в коде.
- Схема каждой БД версионируется через Liquibase (`booking-service`, `restaurant-service`, `user-service`, `notification-service`); Hibernate работает в режиме `ddl-auto: validate` и схему не создаёт.
- Между сервисами используется синхронная интеграция через Spring Cloud OpenFeign (например, `booking-service` → `restaurant-service`/`user-service`) и асинхронная — через Kafka (`booking-service` → `notification-service`).

## Технологический стек

| Категория | Технологии |
|---|---|
| Язык / платформа | Java 21, Gradle (multi-module, Kotlin DSL) |
| Web / API | Spring Boot 4.0.8, Spring Web, Spring Cloud Gateway |
| Service discovery | Spring Cloud Netflix Eureka (Server/Client) |
| Межсервисное взаимодействие | Spring Cloud OpenFeign, Apache Kafka (Spring Kafka) |
| Безопасность | Spring Security, OAuth2 Resource Server (JWT/HS256), BCrypt |
| Данные | Spring Data JPA, PostgreSQL 16, Liquibase (`booking-service`, `restaurant-service`, `user-service`, `notification-service`) |
| Кэш | Spring Data Redis (Redis 7, `restaurant-service`); Caffeine — кэш Spring Cloud LoadBalancer во всех сервисах |
| Файлы | MinIO Java SDK |
| Почта | Spring Mail, Thymeleaf-шаблоны, Mailpit (локальный SMTP-инбокс для разработки) |
| Маппинг | MapStruct |
| Документация API | springdoc-openapi (Swagger UI) |
| Наблюдаемость | Spring Boot Actuator, Micrometer/Prometheus, Grafana, Loki, Grafana Alloy |
| Инфраструктура | Docker, Docker Compose |
| Нагрузочное тестирование | k6 |

## Быстрый старт

### Требования

Docker и Docker Compose для запуска всего стека; JDK 21 — для локальной сборки/запуска отдельных сервисов через Gradle.

### Полный стек в Docker

```bash
docker compose -f docker-compose.local.yml up --build
```

Поднимаются:

- 4 контейнера PostgreSQL (по одному на `user`, `restaurant`, `booking`, `notification`);
- Redis;
- Kafka;
- MinIO + `minio-init` (автосоздание бакетов);
- Mailpit — локальный SMTP-инбокс для проверки писем (`http://localhost:8025`), почта сервисов в этом режиме никуда, кроме него, не уходит;
- Prometheus, Grafana, Loki, Grafana Alloy — стек мониторинга и логирования (см. [Наблюдаемость](#наблюдаемость));
- все сервисы приложения (`eureka-server`, `api-gateway`, `user-service`, `restaurant-service`, `booking-service`, `notification-service`).

Для продакшен-конфигурации используется `docker-compose.prod.yml` (переменные — из `.env.prod`). Каждый сервис собирается общим `Dockerfile` с build-аргументом `MODULE` (multi-stage сборка на `gradle:8.14.5-jdk21`, рантайм — `eclipse-temurin:21-jre-jammy`).

### Только инфраструктура

```bash
docker compose -f docker-compose.infra.yml up -d
```

Поднимает `postgres` (4 базы), `redis`, `kafka`, `minio`, `mailpit` и стек мониторинга (`prometheus`, `grafana`, `loki`, `alloy`) — переменные из `.env.infra`. Создание бакетов MinIO и запуск сервисов приложения в этом режиме нужно выполнять отдельно.

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
| Почта | `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_SMTP_AUTH`, `MAIL_SMTP_STARTTLS_*`, `MAIL_SMTP_SSL_ENABLE`, `MAIL_DEBUG` | `notification-service` |
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
- Смена пароля, смена роли по id (для админ-операций), поиск пользователя по email/id и batch-получение пользователей по списку id для других сервисов.

### restaurant-service

Рестораны, менеджеры, столы, блюда, фотографии.

- CRUD ресторанов: базовые данные, часы работы (`WorkingHoursEntity`), контакты (`ContactEntity`), категории, активность.
- Управление менеджерами: при добавлении менеджера пользователю выставляется `ROLE_MANAGER` (вызов `user-service` через Feign); если после удаления связей менеджер не привязан ни к одному ресторану — роль возвращается на `ROLE_USER`.
- Столы: создание (в т.ч. массовое, `/tables/all`), обновление, обновление layout целиком, удаление.
- Блюда: CRUD с привязкой к ресторану.
- Фото: presigned upload → подтверждение → раздача; поддерживаемые типы — `image/jpeg`, `image/png`, `image/webp`; категории — `BANNER`, `SCHEME`, `GALLERY`; статусы жизненного цикла — `PENDING → ACTIVE`, а также `EXPIRED`/`DELETING` для неподтверждённых и удаляемых файлов.
- Кэширование в Redis (`restaurant-service::` prefix) с разными TTL: рестораны — 5 мин, блюда/столы — 2–5 мин, фото — 1 мин, `managerAccess` — 30 мин, `restaurantBookingTable` — 2 мин.
- Отдаёт `booking-service` snapshot данных ресторана, стола и блюд для создания бронирования, а также проверяет доступ менеджера к ресторану (`booking-snapshot`, `manager-access`).

### booking-service

Бронирования и ценообразование.

- Создание бронирования по ресторану, столу, временно́му интервалу и опциональному предзаказу блюд.
- Проверка доступности стола на дату (публичный эндпоинт).
- Статусы бронирования: `RESERVED` (создано, ожидает подтверждения) и `CANCELLED` (отменено, не участвует в исторической аналитике); текущая загрузка столов считается только по `RESERVED`.
- Сервисный сбор считается по фиксированному коэффициенту (`pricing.charge-coefficient`, сейчас `0.1`): без предзаказа `preorderAmount = 0` и `pricingCharge = 0`; с предзаказом `pricingCharge = preorderAmount * coefficient`; `totalAmount = preorderAmount + pricingCharge`.
- Пересекающиеся по времени бронирования одного стола запрещены на уровне БД (PostgreSQL exclusion constraint), а не только проверкой в коде.
- Публикует в Kafka отдельные события создания и отмены бронирования (`app.kafka.topics.booking-created` → `booking-created-topic`, `app.kafka.topics.booking-cancelled` → `booking-cancelled-topic`).
- Получает данные ресторана/стола/блюд из `restaurant-service` (`booking-snapshot`, `manager-access`) и данные пользователей из `user-service` через Feign.
- Хранение данных — PostgreSQL, миграции — Liquibase.

### notification-service

Фоновая обработка событий бронирования и отправка email.

- Слушает два Kafka-топика: `booking-created-topic` (создание) и `booking-cancelled-topic` (отмена), потребитель — consumer group `my-consumer`.
- Дедуплицирует входящие сообщения по паре (тип события, `bookingId`).
- Хранит статус обработки каждого сообщения: `CREATED → PROCESSING → DONE`, либо `FAILED` при ошибке.
- Отправляет HTML-письмо через SMTP: подтверждение бронирования (`booking-confirm.html`) или уведомление об отмене (`booking-cancelled.html`), оба со встроенным логотипом; SMTP-хост/порт и шифрование (STARTTLS/SSL) задаются переменными окружения (в проде — Gmail, локально — Mailpit).
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
- `GET /api/v1/users/email?email=`
- `GET /api/v1/users/{id}`
- `POST /api/v1/users` (batch-получение по списку id)

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

### Бронирования (`booking-service`)

- `POST /api/v1/bookings`
- `GET /api/v1/bookings/{id}`
- `GET /api/v1/bookings/me`
- `DELETE /api/v1/bookings/{id}/cancel`
- `GET /api/v1/bookings/manager/restaurants/{restId}`
- `GET /api/v1/bookings/public/restaurants/{restaurantId}/tables/{tableId}/availability?date=YYYY-MM-DD`

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

**Роли:** `ROLE_USER`, `ROLE_MANAGER`, `ROLE_ADMIN`. Операции управления ресторанами/столами/блюдами доступны `ROLE_MANAGER`/`ROLE_ADMIN`; отмена чужого бронирования (с обязательным указанием причины) — тем же ролям, при условии, что менеджер привязан к конкретному ресторану.

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

При запуске через `docker-compose.local.yml` или `docker-compose.infra.yml` поднимается стек мониторинга и логирования (конфигурация — в `monitoring/`):

- Prometheus: `http://localhost:9090` (скрейпит `/actuator/prometheus` сервисов, конфиг — `monitoring/prometheus.yml`).
- Grafana: `http://localhost:3000` (логин/пароль по умолчанию — `admin`/`admin`); датасорсы Prometheus и Loki провижинятся автоматически из `monitoring/grafana/provisioning`.
- Loki: `http://localhost:3100` — хранилище логов.
- Grafana Alloy (`http://localhost:12345`) — собирает логи контейнеров через Docker-сокет и отправляет их в Loki (`monitoring/alloy/config.alloy`).
- Mailpit: `http://localhost:8025` — веб-интерфейс для писем, отправленных `notification-service` в локальном режиме (SMTP на `1025`), реальная почта наружу не уходит.

## Структура проекта

```
RBS/
├── api-gateway/            # маршрутизация, проверка JWT, CORS
├── eureka-server/          # service discovery
├── user-service/           # аутентификация, пользователи, роли
├── restaurant-service/     # рестораны, столы, блюда, фото, Redis, MinIO
├── booking-service/        # бронирования, доступность, сервисный сбор
├── notification-service/   # Kafka consumer, email-уведомления
├── common/                 # общий модуль: ошибки, security, локализация
├── monitoring/             # конфигурация Prometheus, Grafana, Loki, Alloy
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
- SMTP в `notification-service` настраивается только переменными окружения, без изменения `application.yml`: локально (`.env.local`) письма уходят в Mailpit и никуда наружу не отправляются, в проде (`.env.prod`) по умолчанию используется Gmail (`smtp.gmail.com:587`). Gmail как отправитель для прод-нагрузки не рекомендуется (лимит ~500 писем/сутки на обычный аккаунт, письма шлются с личного email) — стоит перейти на выделенный SMTP-провайдер (Amazon SES, SendGrid, Yandex Cloud Postbox и т.п.).

## Лицензия

Проект не является open-source. Условия использования — в [LICENSE](LICENSE).