# 📄 `CHECKLIST.md` — Контроль выполнения проекта RBS

> Этот файл используется как **трекер прогресса разработки**.
> Каждый пункт считается выполненным только после прохождения проверки (DoD).

---

## 🟢 Этап 0. Подготовка и проектирование

* [x] Зафиксирован MVP сценарий (register → login → restaurants → booking → notification)
* [x] Определены роли `USER`, `MANAGER` и `OWNER`
* [x] Составлена таблица доступа (endpoint → role)
* [x] Утверждён вариант security: **JWT в Gateway (HS256)**

**DoD:**
MVP и роли описаны в README.

---

## 🟢 Этап 1. Скелет проекта (Gradle multi-module)

* [x] Создан multi-module Gradle проект (Kotlin DSL)
* [x] Модули:

    * [x] eureka-server
    * [x] api-gateway
    * [x] user-service
    * [x] restaurant-service
    * [x] booking-service
    * [x] notification-service
    * [x] common
* [x] Настроены BOM Spring Boot / Spring Cloud
* [x] `./gradlew clean build` проходит без ошибок

**DoD:**
Проект полностью собирается.

---

## 🟢 Этап 2. Eureka + Gateway маршрутизация

### Eureka Server

* [x] Eureka Server запускается на `8761`
* [x] UI доступен

### Eureka Client

* [x] Все сервисы регистрируются в Eureka
* [x] Все сервисы имеют статус `UP`

### API Gateway

* [x] Gateway запускается на `8080`
* [x] Настроены маршруты:

    * [x] `/auth/**` → user-service
    * [x] `/restaurants/**` → restaurant-service
    * [x] `/bookings/**` → booking-service

**DoD:**
Через Gateway доступны `/actuator/health` всех сервисов.

---

## 🟢 Этап 3. Базы данных и миграции

* [x] Docker Compose поднимает PostgreSQL
* [x] Созданы БД:

    * [x] userdb
    * [x] restaurantdb
    * [x] bookingdb
    * [x] notificationdb
* [x] Каждый сервис подключён к своей БД
* [ ] Подключён LiquiBase
* [x] Есть `V1__init.sql` в каждом сервисе

**DoD:**
При старте сервисов таблицы создаются автоматически.

---

## 🟢 Этап 4. Common module (контракты)

* [ ] DTO:

    * [x] UserDto
    * [ ] RestaurantDto
    * [ ] TableDto
    * [ ] MenuItemDto
    * [ ] BookingDto
* [ ] Enums:

    * [x] Role
    * [ ] BookingStatus
* [ ] Events:

    * [ ] BookingCreatedEvent

**DoD:**
DTO и события переиспользуются между сервисами.

---

## 🟢 Этап 5. User Service — Auth + JWT

* [x] Таблица `users` создана
* [x] Пароли хэшируются (bcrypt)
* [x] Эндпоинты:

    * [x] `POST /auth/register`
    * [x] `POST /auth/login`
* [x] JWT содержит:

    * [x] `sub`
    * [x] `role`
    * [x] `exp`

**DoD:**
Login возвращает валидный JWT.

---

## 🟢 Этап 6. API Gateway — Security (JWT HS256)

* [ ] Gateway валидирует JWT по `JWT_SECRET`
* [ ] Проверяется `exp`
* [ ] Реализованы правила доступа:

    * [ ] public
    * [ ] authenticated
    * [ ] admin only
* [ ] Gateway добавляет headers:

    * [ ] `X-User-Id`
    * [ ] `X-User-Role`

**DoD:**

* USER не может admin endpoints
* ADMIN может admin endpoints

---

## 🟢 Этап 7. Restaurant Service

* [ ] Таблицы:

    * [ ] restaurants
    * [ ] tables
    * [ ] menu_items
* [ ] Public endpoints:

    * [ ] GET /restaurants
    * [ ] GET /restaurants/{id}
    * [ ] GET /restaurants/{id}/tables
    * [ ] GET /restaurants/{id}/menu
* [ ] Admin CRUD endpoints

**DoD:**
Каталог доступен публично, админка защищена.

---

## 🟢 Этап 8. Booking Service

* [ ] Таблица `bookings`
* [ ] Используется `X-User-Id`
* [ ] Эндпоинты:

    * [ ] POST /bookings
    * [ ] GET /bookings/{id}
    * [ ] GET /bookings/me
    * [ ] DELETE /bookings/{id}
* [ ] Проверка владельца брони
* [ ] Проверка конфликтов времени

**DoD:**
Пользователь видит и управляет только своими бронированиями.

---

## 🟢 Этап 9. Kafka + Notification Service

* [ ] Kafka работает в docker-compose (KRaft)
* [ ] Booking публикует `booking.created`
* [ ] Notification Service consumer настроен
* [ ] Уведомление логируется или сохраняется

**DoD:**
Создание брони → событие → notification-service реагирует.

---

## 🟢 Этап 10. Redis кэширование

* [ ] Redis подключён
* [ ] Кэшируются:

    * [ ] список ресторанов
    * [ ] меню
    * [ ] столы
* [ ] Инвалидация при CRUD

**DoD:**
Повторные GET читаются из Redis.

---

## 🟢 Этап 11. Наблюдаемость

* [ ] Actuator включён
* [ ] `/actuator/health`
* [ ] `/actuator/info`
* [ ] `/actuator/metrics`

**DoD:**
Все сервисы возвращают статус `UP`.

---

## 🟢 Этап 12. Тестирование

* [ ] Unit tests:

    * [ ] User Service
    * [ ] Booking rules
    * [ ] Restaurant CRUD
* [ ] Integration tests:

    * [ ] Booking + Postgres (Testcontainers)
    * [ ] Kafka event → Notification

**DoD:**
`./gradlew test` проходит.

---

## 🟢 Этап 13. Документация и финал

* [ ] Swagger/OpenAPI подключён
* [ ] README содержит:

    * [ ] архитектуру
    * [ ] сценарий проверки
    * [ ] инструкции запуска
* [ ] `docker compose up --build` поднимает всё

**DoD:**
Проект готов к защите.
