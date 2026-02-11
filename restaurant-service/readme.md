# restaurant-service

Сервис управления ресторанами, столами, блюдами и медиа.

---

## Назначение

* CRUD ресторанов
* CRUD столов
* CRUD блюд
* Управление фото
* Presigned upload
* Redis кэширование

---

## Порт

```
8081
```

---

## Используемые технологии и зависимости

* Spring Boot
* Spring Web
* Spring Security
* Spring Data JPA
* Spring Validation
* Spring Cache
* Spring Data Redis
* Spring Cloud Netflix Eureka Client
* PostgreSQL Driver
* Liquibase
* MinIO Java SDK
* MapStruct
* Lombok
* Spring Boot Actuator
* Micrometer
* Gradle (Kotlin DSL)
* Java 17

Используется объектное хранилище:
MinIO

---

## База данных

Отдельная БД: `restaurantdb`

Основные сущности:

* Restaurant
* Table
* Dish
* Photo
* Manager (composite key)

---

## Фото

Статусы:

* PENDING
* ACTIVE
* EXPIRED
* DELETING

Категории:

* BANNER
* SCHEME
* GALLERY

---

## REST API

### Restaurants

```
POST   /api/v1/restaurants
GET    /api/v1/restaurants/my
GET    /api/v1/restaurants/{id}
PATCH  /api/v1/restaurants/{id}
DELETE /api/v1/restaurants/{id}
```

### Tables

```
POST   /api/v1/restaurants/{restaurantId}/tables
GET    /api/v1/restaurants/{restaurantId}/tables
PATCH  /api/v1/restaurants/{restaurantId}/tables/{tableId}
DELETE /api/v1/restaurants/{restaurantId}/tables/{tableId}
```

### Dishes

```
POST   /api/v1/restaurants/{restaurantId}/dishes
GET    /api/v1/restaurants/{restaurantId}/dishes
PATCH  /api/v1/restaurants/{restaurantId}/dishes/{dishId}
DELETE /api/v1/restaurants/{restaurantId}/dishes/{dishId}
```

### Internal

```
GET  /api/v1/internal/restaurants/{restId}/manager-access
POST /api/v1/internal/restaurants/booking-snapshot
```

---

## Actuator

* health
* info
* metrics
* prometheus

---

## Лицензия и условия использования

Данный проект **НЕ является open-source**.

Исходный код размещён в открытом доступе **исключительно
для ознакомления**.

Любое использование кода, включая (но не ограничиваясь):
запуск, компиляцию, модификацию, копирование, распространение,
деплой или включение в другие проекты, **запрещено** без
предварительного письменного согласия автора.

© 2026 this-is-lama. Все права защищены.

---