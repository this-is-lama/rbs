# notification-service

Фоновый сервис обработки событий о создании бронирования и отправки email.

## Что делает сервис

- Слушает Kafka topic с событиями бронирований.
- Дедуплицирует сообщения по `bookingId`.
- Сохраняет состояние обработки в собственной БД.
- Отправляет HTML-письмо через SMTP и Thymeleaf.
- Повторяет неуспешные отправки по расписанию.
- Чистит старые `DONE`-сообщения по расписанию.

## Порт

```text
8084
```

## HTTP API

В текущей версии сервис не публикует REST-контроллеры. Он работает как Kafka consumer и background worker.

## Kafka

- consumer group: `my-consumer`
- property topic: `app.kafka.topics.booking-created`
- текущее значение topic: `booking-topic`

## Статусы сообщений

- `CREATED`
- `PROCESSING`
- `FAILED`
- `DONE`

## Планировщики

- retry сообщений: каждые `10` минут
- очистка `DONE`-сообщений: каждые `60` минут

Текущие параметры:

- `app.notification.max-attempts=5`
- `app.notification.stuck-minutes=15` по умолчанию в коде

## Email

- используется `Spring Mail`
- шаблон письма: `src/main/resources/templates/booking-confirm.html`
- встроенный логотип: `src/main/resources/templates/logo.svg`
- SMTP сейчас настроен под Gmail (`smtp.gmail.com:587`)

## База данных

Собственная БД: `notificationdb`.

Основная сущность:

- `MessageEntity`

## Конфигурация

Обязательные переменные окружения:

- `DB_HOST`
- `DB_PORT`
- `DB_USER`
- `DB_PASS`
- `KAFKA_BOOTSTRAP`
- `EUREKA_URL`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`

Основные настройки лежат в `src/main/resources/application.yml`.

## Технологии

- Spring Boot
- Spring Data JPA
- Spring Kafka
- Spring Mail
- Thymeleaf
- Spring Cloud Netflix Eureka Client
- PostgreSQL
- MapStruct
- Java 17

## Actuator

Открыты endpoints:

- `health`
- `info`
- `metrics`
- `prometheus`

## Лицензия

См. корневой `readme.md` и файл `LICENSE`.
