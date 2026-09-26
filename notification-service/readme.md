# notification-service

Фоновый сервис обработки событий о создании бронирования и отправки email.

## Что делает сервис

- Слушает Kafka-топики с событиями создания и отмены бронирований.
- Дедуплицирует сообщения по паре (тип события, `bookingId`).
- Сохраняет состояние обработки в собственной БД.
- Отправляет HTML-письмо через SMTP и Thymeleaf: подтверждение бронирования или уведомление об отмене.
- Повторяет неуспешные отправки по расписанию.
- Чистит старые `DONE`-сообщения по расписанию.

## Порт

```text
8084
```

## HTTP API

В текущей версии сервис не публикует REST-контроллеры. Он работает как Kafka consumer и background worker.

## Kafka

- consumer group: `notification-service`
- `app.kafka.topics.booking-created` → `booking-created-topic` (слушатель `listenBookingCreated`)
- `app.kafka.topics.booking-cancelled` → `booking-cancelled-topic` (слушатель `listenBookingCancelled`)
- 3 потока чтения (`concurrency = 3`) — по одному на партицию

### Повторы и dead-letter topic

Если обработка сообщения падает (например, недоступна БД), сообщение не теряется:

1. `@RetryableTopic` перекладывает его в retry-топики с паузами **1 с → 10 с → 100 с** (`<topic>-retry-1000`, `-retry-10000`, `-retry-100000`).
2. После 4-й неудачной попытки — в `<topic>-dlt`. Ошибки, которые повтором не лечатся (битый JSON, `NullPointerException`), уходят в DLT сразу.
3. `@DltHandler` пишет ERROR «Сообщение ушло в DLT» и увеличивает метрику `notification_kafka_dead_letter_total{topic}` → алерт Grafana `rbs-kafka-dead-letter`.

DLT-топики хранят сообщения 30 дней (ставится при старте сервиса). Переотправка после устранения причины:

```bash
./scripts/kafka-dlt-replay.sh booking-created-topic          # посмотреть, что лежит в DLT
./scripts/kafka-dlt-replay.sh booking-created-topic --send   # переотправить в основной топик
```

Повторы Kafka дополняют, а не заменяют retry через БД: ошибки отправки почты по-прежнему ловятся в `NotificationServiceImpl` и повторяются планировщиком (см. ниже).

## Статусы сообщений

- `CREATED`
- `PROCESSING`
- `FAILED`
- `DONE`

Тип сообщения (`MessageType`): `BOOKING_CREATED`, `BOOKING_CANCELLED` — участвует в ключе дедупликации.

## Планировщики

- retry сообщений: каждые `10` минут
- очистка `DONE`-сообщений: каждые `60` минут

Текущие параметры:

- `app.notification.max-attempts=5`
- `app.notification.stuck-minutes=15` по умолчанию в коде

## Email

- используется `Spring Mail`
- шаблоны писем: `src/main/resources/templates/booking-confirm.html` (создание) и `booking-cancelled.html` (отмена)
- встроенный логотип: `src/main/resources/templates/logo.svg`
- SMTP-хост/порт и шифрование (STARTTLS/SSL) задаются переменными окружения без изменения `application.yml`; локально — Mailpit, в проде по умолчанию — Gmail (`smtp.gmail.com:587`)

## База данных

Собственная БД: `notificationdb`, схема версионируется через Liquibase (`db/changelog/db.changelog-master.yaml`); Hibernate работает в режиме `ddl-auto: validate`.

Основная сущность:

- `MessageEntity` (таблица `processed_messages`)

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
- Liquibase
- MapStruct
- Java 21

## Actuator

Открыты endpoints:

- `health`
- `info`
- `metrics`
- `prometheus`

## Лицензия

См. корневой `readme.md` и файл `LICENSE`.
