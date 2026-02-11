# eureka-server

Сервис регистрации и обнаружения микросервисов.

---

## Назначение

`eureka-server` выполняет роль Service Discovery.
Все сервисы регистрируются в нём и получают возможность динамического обнаружения.

Используется:
Netflix Eureka

---

## Порт

```
8761
```

---

## Используемые технологии и зависимости

* Spring Boot
* Spring Cloud Netflix Eureka Server
* Spring Boot Actuator
* Micrometer
* Gradle (Kotlin DSL)
* Java 17

---

## Конфигурация

* `register-with-eureka=false`
* `fetch-registry=false`
* Actuator endpoints:

    * health
    * info
    * metrics
    * prometheus

---

## Проверка работоспособности

```
GET http://localhost:8761
GET http://localhost:8761/actuator/health
```

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