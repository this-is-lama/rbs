# user-service

Сервис аутентификации и управления пользователями.

---

## Назначение

* Регистрация
* Логин
* Refresh
* Logout
* Управление ролями
* Хранение refresh JTI

---

## Порт

```
8083
```

---

## Используемые технологии и зависимости

* Spring Boot
* Spring Web
* Spring Security
* Spring Data JPA
* Spring Validation
* Spring Cloud Netflix Eureka Client
* PostgreSQL Driver
* Liquibase
* jjwt (JWT generation / parsing)
* Spring Boot Actuator
* Micrometer
* Gradle (Kotlin DSL)
* Java 17

---

## База данных

Отдельная БД: `userdb`

Таблицы:

* users
* refresh_jtis

---

## JWT модель

### Access Token

Содержит:

* sub
* email
* roles
* token_type
* issuer

### Refresh Token

Содержит:

* sub
* jti
* token_type

JTI хранится в БД в виде хэша.

---

## REST API

```
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
PATCH /api/v1/users/change-role
GET   /api/v1/users/{id}
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
