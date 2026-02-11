# api-gateway

Единая точка входа во всю систему RBS.

---

## Назначение

* Проверка access JWT
* Проверка issuer
* Проверка claim `token_type`
* Маршрутизация через discovery

Используется:
Spring Cloud Gateway

---

## Порт

```
8080
```

---

## Используемые технологии и зависимости

* Spring Boot
* Spring WebFlux
* Spring Security
* Spring OAuth2 Resource Server
* Spring Cloud Gateway
* Spring Cloud Netflix Eureka Client
* Spring Boot Actuator
* Micrometer
* Gradle (Kotlin DSL)
* Java 17

---

## Безопасность

* HS256 проверка подписи
* Проверка issuer
* Проверка `token_type=access_token`

Публичные маршруты:

* `/api/v1/auth/**`
* `/actuator/health`
* `/actuator/info`

---

## Маршрутизация

Используется discovery locator.

Формат:

```
http://localhost:8080/{service-name}/...
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

