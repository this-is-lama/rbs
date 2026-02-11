# notification-service

Сервис обработки событий и отправки email.

---

## Назначение

* Kafka consumer
* Идемпотентность
* Retry
* Отправка email

---

## Порт

```
8084
```

---

## Используемые технологии и зависимости

* Spring Boot
* Spring Web
* Spring Data JPA
* Spring Kafka
* Spring Mail
* Thymeleaf
* Spring Cloud Netflix Eureka Client
* PostgreSQL Driver
* Liquibase
* Lombok
* Spring Boot Actuator
* Micrometer
* Gradle (Kotlin DSL)
* Java 17

Используется:
Apache Kafka

---

## База данных

Отдельная БД: `notificationdb`

Сущность:

* MessageEntity

---

## Kafka

Consumer group:

```
notification-service-group
```

Topic:

```
booking-created
```

---

## Статусы сообщений

* CREATED
* PROCESSING
* FAILED
* DONE

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