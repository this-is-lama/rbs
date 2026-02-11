# booking-service

Сервис управления бронированиями.

---

## Назначение

* Создание бронирования
* Отмена бронирования
* Получение списка бронирований
* Публикация события

---

## Порт

```
8082
```

---

## Используемые технологии и зависимости

* Spring Boot
* Spring Web
* Spring Security
* Spring Data JPA
* Spring Validation
* Spring Cloud Netflix Eureka Client
* Spring Cloud OpenFeign
* Spring Kafka
* PostgreSQL Driver
* Liquibase
* MapStruct
* Lombok
* Spring Boot Actuator
* Micrometer
* Gradle (Kotlin DSL)
* Java 17

Используется:
Apache Kafka

---

## База данных

Отдельная БД: `bookingdb`

Сущность:

* Booking

---

## REST API

```
POST   /api/v1/bookings
GET    /api/v1/bookings/{id}
GET    /api/v1/bookings/my
DELETE /api/v1/bookings/{id}
```

---

## Kafka

Topic:

```
booking-created
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