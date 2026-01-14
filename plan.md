# RBS — Полный план разработки

---

## Содержание

* [1. Общая логика разработки](#1-общая-логика-разработки)
* [2. MVP сценарий (что должно работать первым)](#2-mvp-сценарий-что-должно-работать-первым)
* [3. Архитектура сервисов](#3-архитектура-сервисов)
* [4. Этапы разработки (0 → продакшен-демо)](#4-этапы-разработки-0--продакшен-демо)
* [5. Роутинг и security-правила Gateway (готовая спецификация)](#5-роутинг-и-security-правила-gateway-готовая-спецификация)
* [6. Definition of Done (DoD) — “готово” на каждом этапе](#6-definition-of-done-dod--готово-на-каждом-этапе)

---

## 1. Общая логика разработки

Разработка микросервисов не делается как “сначала полностью один сервис, потом следующий”. Самая эффективная схема:

1. **Скелет и инфраструктура** (чтобы всё стартовало и находило друг друга)
2. **MVP end-to-end** (одна цепочка “пользователь → действие → результат” через несколько сервисов)
3. **Наращивание**: безопасность, кэш, события, наблюдаемость
4. **Качество**: тесты, документация, стабильный запуск в Docker

---

## 2. MVP сценарий (что должно работать первым)

**MVP = один рабочий бизнес-поток**, который ты можешь показать на защите.

### MVP сценарий:

1. Пользователь **регистрируется**
2. Пользователь **логинится** и получает **JWT**
3. Пользователь **смотрит рестораны и столы**
4. Пользователь **создаёт бронирование**
5. Booking Service публикует событие `booking.created`
6. Notification Service “отправляет уведомление” (достаточно логирования или записи в БД)

---

## 3. Архитектура сервисов

* **Eureka Server** — сервис-дискавери
* **API Gateway** — единая точка входа, маршрутизация, JWT security
* **User Service** — пользователи, роли, регистрация/логин, выдача JWT
* **Restaurant Service** — рестораны/столы/меню (read-heavy, кэш Redis)
* **Booking Service** — бронирования, правила, события Kafka
* **Notification Service** — consumer Kafka, уведомления
* **Common module** — DTO, enum, event models

---

## 4. Этапы разработки (0 → продакшен-демо)

### 0) Подготовка и фиксация требований (1 день)

#### 0.1. Зафиксировать сущности и роли

* роли: `USER`, `MANAGER`, `OWNER`
* сущности: restaurant, table, menuItem, booking, user, notification

#### 0.2. Таблица доступа (очень полезно для защиты)

Составь таблицу:

* endpoint → метод → кто имеет доступ → комментарий

✅ **DoD**

* [x] MVP сценарий описан в README
* [x] роли и правила доступа описаны

---

### 1) Скелет проекта и сборка (1–2 дня)

#### 1.1. Multi-module Gradle (Kotlin DSL)

Модули: `eureka-server`, `api-gateway`, `user-service`, `restaurant-service`, `booking-service`, `notification-service`, `common`

#### 1.2. Единые версии и BOM

* единая версия Spring Boot
* единая версия Spring Cloud (BOM)

✅ **DoD**

* [x] `./gradlew clean build` проходит
* [x] каждый сервис запускается локально (пусть даже с заглушками)

---

### 2) Инфраструктура: Eureka + Gateway маршрутизация (2–3 дня)

#### 2.1. Eureka Server

* порт `8761`
* UI доступен

#### 2.2. Регистрация сервисов в Eureka

* каждый сервис = Eureka Client
* проверка по UI что сервисы `UP`

#### 2.3. API Gateway маршруты

* `/user/**` → `lb://user-service`
* `/restaurants/**` → `lb://restaurant-service`
* `/bookings/**` → `lb://booking-service`
* `/notifications/**` (опционально) → `lb://notification-service`

✅ **DoD**

* [x] Eureka UI показывает все сервисы
* [x] через Gateway можно дергать `/actuator/health` сервисов

---

### 3) Базы данных + миграции (2–4 дня)

#### 3.1. Docker Compose: Postgres + базы

* `userdb`, `restaurantdb`, `bookingdb`, `notificationdb`

#### 3.2. Миграции

В каждом сервисе:

* `V1__init.sql`

✅ **DoD**

* [x] сервисы подключаются к своим базам
* [ ] миграции применяются автоматически на старте

---

### 4) Common модуль и контракты (1–2 дня)

#### 4.1. Общие DTO / Events / Enums

* DTO: `RestaurantDto`, `TableDto`, `MenuItemDto`, `BookingDto`, `AuthResponseDto`
* enums: `Role`, `BookingStatus`
* events: `BookingCreatedEvent`, `BookingCancelledEvent` (опционально)

✅ **DoD**

* [ ] сервисы переиспользуют common-модели (нет копипасты)

---

### 5) User Service: регистрация/логин + выдача JWT (3–5 дней)

> User Service **только выдаёт JWT**, проверять токен будет Gateway.

#### 5.1. Таблица `users`

* `id`
* `email` unique
* `password_hash` (bcrypt)
* `role` (USER/ADMIN)
* `created_at`

#### 5.2. Эндпоинты

* `POST /auth/register`
* `POST /auth/login` → возвращает JWT

#### 5.3. JWT payload

Минимум:

* `sub` = email
* `role` = USER/MANAGER/OWNER
* `iat`, `exp`

✅ **DoD**

* [x] регистрация создаёт пользователя
* [x] логин возвращает токен
* [x] токен валиден и содержит role/sub

---

### 6) API Gateway: JWT валидация по секрету + роли (2–4 дня)

#### 6.1. JWT валидация в Gateway (HS256)

Gateway:

* читает `Authorization: Bearer <token>`
* валидирует подпись по `JWT_SECRET`
* проверяет `exp`
* достаёт `sub` и `role`

#### 6.2. Правила доступа по ролям

* публичные маршруты не требуют токен
* защищённые требуют токен`

#### 6.3. Передача контекста в сервисы

Gateway добавляет заголовки:

* `X-User-Id: <sub>`
* `X-User-Role: <role>`

Дальше Booking Service использует `X-User-Id` как “текущий пользователь”.

✅ **DoD**

* [ ] без токена защищённые маршруты дают 401
* [ ] с USER токеном можно бронировать
* [ ] USER не может админские методы (403)
* [ ] ADMIN может админские методы
* [ ] booking-service получает `X-User-Id`

---

### 7) Restaurant Service: справочники + админ CRUD (3–6 дней)

#### 7.1. Таблицы

* restaurants
* tables
* menu_items

#### 7.2. Публичные GET

* `GET /restaurants`
* `GET /restaurants/{id}`
* `GET /restaurants/{id}/tables`
* `GET /restaurants/{id}/menu`

#### 7.3. Admin CRUD

* `POST/PUT/DELETE /restaurants/**`
* добавление/изменение меню и столов

✅ **DoD**

* [ ] каталог отдаёт данные
* [ ] админ CRUD доступен только ADMIN через Gateway

---

### 8) Booking Service: бронирования + правила (4–7 дней)

#### 8.1. Таблица bookings

* id
* user_id (из `X-User-Id`)
* restaurant_id
* table_id
* date_time
* status
* created_at

#### 8.2. Минимальные правила

* нельзя 2 брони на один стол в одно время
* отменить бронь может только создатель (user_id)

#### 8.3. Эндпоинты

* `POST /bookings`
* `GET /bookings/{id}`
* `GET /bookings/me`
* `DELETE /bookings/{id}`

✅ **DoD**

* [ ] бронирование создаётся и хранится
* [ ] `GET /bookings/me` отдаёт только свои брони
* [ ] отмена работает и проверяет владельца

---

### 9) Kafka + Notification Service (event-driven) (2–5 дней)

#### 9.1. Kafka в docker-compose (KRaft)

* брокер стартует
* сервисы подключаются

#### 9.2. Booking публикует событие

Topic: `booking.created`

* после создания брони отправляем `BookingCreatedEvent`

#### 9.3. Notification слушает и “отправляет”

* consumer читает событие
* уведомление: лог/запись в таблицу `notifications` (на выбор)

✅ **DoD**

* [ ] после создания брони появляется событие в Kafka
* [ ] notification-service получает событие и реагирует

---

### 10) Redis кэширование Restaurant Service (2–4 дня)

#### 10.1. Что кэшировать

* список ресторанов
* меню ресторана
* столы ресторана

#### 10.2. Паттерн Cache-aside

* read: redis → db → redis
* write: invalidate cache

✅ **DoD**

* [ ] повторные GET быстрее (или видно по логам, что идёт из кэша)
* [ ] после изменения данных кэш инвалидируется

---

### 11) Наблюдаемость (Actuator) (1–2 дня)

* `/actuator/health`
* `/actuator/info`
* `/actuator/metrics`

✅ **DoD**

* [ ] у всех сервисов actuator endpoints доступны

---

### 12) Тестирование (unit + интеграция) (3–6 дней)

#### 12.1. Unit tests (минимум)

* user: регистрация/логин
* restaurant: CRUD + валидации
* booking: конфликт брони + отмена владельцем

#### 12.2. Integration tests (Testcontainers)

* BookingService + Postgres: create booking → запись в БД
* Kafka: publish → consume (можно embedded kafka или testcontainers)

✅ **DoD**

* [ ] тесты запускаются `./gradlew test`
* [ ] есть хотя бы 1 интеграционный тест на Booking
* [ ] есть хотя бы 1 тест на Kafka поток

---

### 13) Документация + Swagger + финальная упаковка (2–4 дня)

#### 13.1. Swagger / OpenAPI

* подключить springdoc-openapi (хотя бы в сервисы)
* ссылки на swagger в README

#### 13.2. README “как проверить”

* команды запуска
* примеры запросов (curl / Postman)
* “демо сценарий”

#### 13.3. Docker Compose “одной командой”

* `docker compose up --build`
* healthchecks (желательно)

✅ **DoD**

* [ ] проект запускается в Docker одной командой
* [ ] есть понятная инструкция проверки

---

### 14) Полировка + сценарий защиты (2–3 дня)

#### 14.1. Демо сценарий (готовый)

1. Register
2. Login → JWT
3. GET restaurants (без токена)
4. POST bookings (без токена → 401)
5. POST bookings (с токеном → 201)
6. Kafka событие → notification-service реагирует
7. ADMIN создаёт ресторан (USER → 403, ADMIN → 201)

#### 14.2. “ответы на вопросы преподавателя”

* почему микросервисы?
* почему gateway и eureka?
* почему kafka?
* почему redis?
* какие проблемы микросервисов (сеть, согласованность, наблюдаемость)?

✅ **DoD**

* [ ] демонстрация занимает 3–5 минут и проходит без сюрпризов
* [ ] диаграмма архитектуры в README/презентации

---

## 5. Роутинг и security-правила Gateway (готовая спецификация)

### Публичные маршруты (без JWT)

* `POST /auth/register`
* `POST /auth/login`
* `GET /restaurants/**` *(только чтение)*

### Требуют JWT (USER или ADMIN)

* `POST /bookings`
* `GET /bookings/**`
* `DELETE /bookings/**`

### Требуют JWT + роль ADMIN

* `POST /restaurants/**`
* `PUT /restaurants/**`
* `DELETE /restaurants/**`
* любые admin-эндпоинты меню и столов

### Передача пользователя дальше

Gateway добавляет headers:

* `X-User-Id`
* `X-User-Role`

---

## 6. Definition of Done (DoD) — “готово” на каждом этапе

Ты считаешь этап завершённым только если:

1. **код собран и запущен**
2. **есть проверка руками** (curl/Postman)
3. **есть фиксация в git** (коммит/PR)
4. **описано в README/CHANGELOG (коротко)**

Рекомендуемый формат контроля:

* `Этап → DoD → ссылка на коммит/PR`

---
