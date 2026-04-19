# eureka-server

Сервис регистрации и обнаружения микросервисов.

## Что делает сервис

- Поднимает Eureka Server.
- Принимает регистрацию от `api-gateway`, `user-service`, `restaurant-service`, `booking-service` и `notification-service`.
- Используется как service discovery для внутренних вызовов.

## Порт

```text
8761
```

## Конфигурация

В текущей версии сервер работает как standalone discovery node:

- `register-with-eureka=false`
- `fetch-registry=false`

Конфигурация находится в `src/main/resources/application.yml`.

## Проверка работы

- UI: `http://localhost:8761`
- Health: `http://localhost:8761/actuator/health`

## Технологии

- Spring Boot
- Spring Cloud Netflix Eureka Server
- Spring Boot Actuator
- Java 17

## Actuator

Открыты endpoints:

- `health`
- `info`
- `metrics`
- `prometheus`

## Лицензия

См. корневой `readme.md` и файл `LICENSE`.
