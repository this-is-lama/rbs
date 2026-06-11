# Настройка HikariCP при запуске нескольких реплик

В проекте используется один сервер PostgreSQL с отдельными базами данных для сервисов:

- `userdb`;
- `restaurantdb`;
- `bookingdb`;
- `notificationdb`.

При горизонтальном масштабировании важно учитывать, что каждая запущенная реплика микросервиса создает собственный пул соединений с базой данных. Поэтому итоговое количество подключений к PostgreSQL считается по формуле:

```text
общее количество подключений = количество реплик × maximum-pool-size
```

Например, если `booking-service` запущен в 3 репликах и у каждой реплики задано `maximum-pool-size: 5`, то сервис может открыть до 15 подключений к базе данных:

```text
3 × 5 = 15 подключений
```

Из-за этого нельзя просто увеличивать количество реплик и оставлять большие значения пула соединений. Чем больше реплик запускается, тем меньше должен быть пул соединений у каждой отдельной реплики.

## Назначение параметров HikariCP

Основные параметры пула соединений:

| Параметр | Назначение |
| --- | --- |
| `maximum-pool-size` | Максимальное количество подключений к базе данных, которое может открыть одна реплика сервиса |
| `minimum-idle` | Минимальное количество свободных подключений, которые сервис старается держать заранее готовыми |
| `connection-timeout` | Максимальное время ожидания свободного подключения из пула |
| `idle-timeout` | Время, после которого лишнее простаивающее подключение может быть закрыто |
| `max-lifetime` | Максимальное время жизни одного подключения перед его пересозданием |

Главное правило настройки:

```text
Больше реплик сервиса — меньше maximum-pool-size на одну реплику.
Меньше реплик сервиса — можно дать больший maximum-pool-size.
```

## Нужно ли делать отдельный PostgreSQL-сервер для каждой базы данных

На текущем этапе отдельный PostgreSQL-сервер для каждой базы данных не требуется.

Текущая схема:

```text
один PostgreSQL-сервер
├── userdb
├── restaurantdb
├── bookingdb
└── notificationdb
```

является нормальной для локальной разработки, нагрузочного тестирования и дипломного проекта.

Отдельные серверы PostgreSQL для каждой базы данных имеет смысл использовать только тогда, когда нагрузочные тесты покажут, что именно база данных стала главным узким местом системы.

До этого эффективнее выполнить более простые действия:

- настроить индексы в сущностях;
- уменьшить количество подключений на одну реплику;
- запускать несколько реплик наиболее нагруженных сервисов;
- использовать Redis-кэш для часто читаемых данных;
- отключать лишнее логирование в профиле нагрузочного тестирования.

## Что лучше: больше реплик или больше подключений

Более эффективный подход:

```text
увеличивать количество реплик важных сервисов
+
уменьшать maximum-pool-size у каждой реплики
```

Менее эффективный подход:

```text
оставлять мало реплик
+
ставить очень большой maximum-pool-size
```

Реплики помогают распределять не только работу с базой данных, но и остальную нагрузку:

- обработку HTTP-запросов;
- проверку JWT;
- валидацию входных данных;
- маппинг DTO;
- Feign-вызовы между сервисами;
- расчёт динамического сервисного сбора;
- работу с Redis-кэшем;
- обработку ошибок.

Большой пул соединений помогает только при работе с базой данных. Если подключений слишком много, PostgreSQL начинает тратить ресурсы на обслуживание соединений, а не на выполнение запросов.

# Рекомендуемые настройки HikariCP

## Вариант 1. Запуск по одной реплике каждого сервиса

Этот режим подходит для обычной локальной разработки и небольших проверок.

| Сервис | Количество реплик | `maximum-pool-size` | `minimum-idle` | Максимум подключений |
| --- | ---: | ---: | ---: | ---: |
| `user-service` | 1 | 4 | 1 | 4 |
| `restaurant-service` | 1 | 8 | 2 | 8 |
| `booking-service` | 1 | 10 | 2 | 10 |
| `notification-service` | 1 | 3 | 1 | 3 |
| **Итого** |  |  |  | **25** |

### `user-service`

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 4
      minimum-idle: 1
      connection-timeout: 3000
      idle-timeout: 30000
      max-lifetime: 600000
```

### `restaurant-service`

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 8
      minimum-idle: 2
      connection-timeout: 3000
      idle-timeout: 30000
      max-lifetime: 600000
```

### `booking-service`

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 3000
      idle-timeout: 30000
      max-lifetime: 600000
```

### `notification-service`

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 3
      minimum-idle: 1
      connection-timeout: 3000
      idle-timeout: 30000
      max-lifetime: 600000
```

## Вариант 2. Запуск по три реплики каждого сервиса

Этот режим подходит для основного нагрузочного тестирования.

| Сервис | Количество реплик | `maximum-pool-size` | `minimum-idle` | Максимум подключений |
| --- | ---: | ---: | ---: | ---: |
| `user-service` | 3 | 3 | 1 | 9 |
| `restaurant-service` | 3 | 4 | 1 | 12 |
| `booking-service` | 3 | 5 | 1 | 15 |
| `notification-service` | 3 | 2 | 1 | 6 |
| **Итого** |  |  |  | **42** |

### `user-service`

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 3
      minimum-idle: 1
      connection-timeout: 3000
      idle-timeout: 30000
      max-lifetime: 600000
```

### `restaurant-service`

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 4
      minimum-idle: 1
      connection-timeout: 3000
      idle-timeout: 30000
      max-lifetime: 600000
```

### `booking-service`

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 5
      minimum-idle: 1
      connection-timeout: 3000
      idle-timeout: 30000
      max-lifetime: 600000
```

### `notification-service`

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 2
      minimum-idle: 1
      connection-timeout: 3000
      idle-timeout: 30000
      max-lifetime: 600000
```

## Вариант 3. Запуск по пять реплик каждого сервиса

Этот режим является более тяжелым для локального PostgreSQL. При таком запуске необходимо сильнее ограничивать размер пула соединений у каждой реплики.

| Сервис | Количество реплик | `maximum-pool-size` | `minimum-idle` | Максимум подключений |
| --- | ---: | ---: | ---: | ---: |
| `user-service` | 5 | 2 | 1 | 10 |
| `restaurant-service` | 5 | 3 | 1 | 15 |
| `booking-service` | 5 | 4 | 1 | 20 |
| `notification-service` | 5 | 1-2 | 1 | 5-10 |
| **Итого** |  |  |  | **50-55** |

### `user-service`

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 2
      minimum-idle: 1
      connection-timeout: 3000
      idle-timeout: 30000
      max-lifetime: 600000
```

### `restaurant-service`

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 3
      minimum-idle: 1
      connection-timeout: 3000
      idle-timeout: 30000
      max-lifetime: 600000
```

### `booking-service`

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 4
      minimum-idle: 1
      connection-timeout: 3000
      idle-timeout: 30000
      max-lifetime: 600000
```

### `notification-service`

Если уведомления не являются основной частью нагрузочного теста:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 1
      minimum-idle: 1
      connection-timeout: 3000
      idle-timeout: 30000
      max-lifetime: 600000
```

Если активно тестируется обработка Kafka-событий и отправка уведомлений:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 2
      minimum-idle: 1
      connection-timeout: 3000
      idle-timeout: 30000
      max-lifetime: 600000
```

# Общая таблица рекомендаций

| Сервис | 1 реплика | 3 реплики | 5 реплик |
| --- | ---: | ---: | ---: |
| `user-service` | 4 | 3 | 2 |
| `restaurant-service` | 8 | 4 | 3 |
| `booking-service` | 10 | 5 | 4 |
| `notification-service` | 3 | 2 | 1-2 |
| **Итого подключений** | **25** | **42** | **50-55** |

В таблице указаны значения `maximum-pool-size` для одной реплики сервиса.

# Практическая рекомендация для нагрузочного тестирования

Для обычной разработки достаточно запускать по одной реплике каждого сервиса.

Для основного нагрузочного тестирования рекомендуется запускать по 3 реплики:

```text
user-service: 3 реплики, maximum-pool-size: 3
restaurant-service: 3 реплики, maximum-pool-size: 4
booking-service: 3 реплики, maximum-pool-size: 5
notification-service: 3 реплики, maximum-pool-size: 2
```

Для стресс-тестирования не обязательно запускать по 5 реплик всех сервисов. Более эффективно увеличить количество реплик только у наиболее нагруженных сервисов:

```text
api-gateway: 1 реплика
eureka-server: 1 реплика
user-service: 1-2 реплики
restaurant-service: 3-5 реплик
booking-service: 3-5 реплик
notification-service: 1 реплика
```

Такой подход эффективнее, потому что основная нагрузка в системе приходится на:

- получение данных ресторанов;
- получение столов и блюд;
- проверку доступности столов;
- создание бронирований;
- расчёт динамического сервисного сбора.

`notification-service` работает как фоновый обработчик Kafka-событий, поэтому в локальном стресс-тесте ему обычно не требуется большое количество реплик.

# Как понять, что пул соединений настроен неправильно

Признаки слишком маленького пула:

```text
растёт время ответа;
появляются ошибки получения соединения из HikariCP;
в логах появляется сообщение connection is not available;
в k6 растут http_req_duration, p95 и p99.
```

Признаки слишком большого пула:

```text
PostgreSQL начинает сильно нагружать CPU;
увеличивается количество активных соединений;
добавление новых подключений не ускоряет систему;
при увеличении реплик производительность перестает расти или падает.
```

Если пул слишком маленький, увеличивать `maximum-pool-size` нужно постепенно:

```text
4 → 5 → 6
```

Не рекомендуется сразу ставить большие значения вроде `20`, `30` или `50` на одну реплику сервиса.

# Итоговое правило

Для данной микросервисной системы рекомендуется использовать один PostgreSQL-сервер с отдельными базами данных, несколько реплик наиболее нагруженных сервисов и ограниченный размер пула соединений у каждой реплики.

Оптимальная стратегия:

```text
больше реплик важных сервисов
+
меньше подключений на одну реплику
+
Redis-кэш
+
индексы в базе данных
+
отключение лишнего логирования при нагрузочном тестировании
```

Такой подход позволяет распределить нагрузку между экземплярами микросервисов и не перегружать PostgreSQL чрезмерным количеством одновременных подключений.

# Заготовки профилей запуска для сервисов

Ниже приведены готовые заготовки профилей для `application.yml` каждого сервиса.

Идея такая:

- `dev` — обычная локальная разработка, чаще всего по одной реплике;
- `perf-3` — нагрузочное тестирование при запуске примерно 3 реплик сервиса;
- `perf-5` — стресс-тестирование при запуске примерно 5 реплик сервиса;
- `prod` — условный профиль для стабильного запуска, без подробного debug-логирования.

Профиль можно включать через переменную окружения:

```text
SPRING_PROFILES_ACTIVE=perf-3
```

или через аргумент запуска:

```bash
java -jar app.jar --spring.profiles.active=perf-3
```

При запуске из IntelliJ IDEA профиль можно указать в настройках конфигурации запуска в поле `Active profiles`.

## `user-service`

```yaml
spring:
  profiles:
    default: dev

  datasource:
    hikari:
      connection-timeout: 3000
      idle-timeout: 30000
      max-lifetime: 600000

---

spring:
  config:
    activate:
      on-profile: dev

  datasource:
    hikari:
      maximum-pool-size: 4
      minimum-idle: 1

logging:
  level:
    root: INFO
    my.project.userservice: DEBUG
    org.springframework.security: INFO
    org.hibernate.SQL: WARN

---

spring:
  config:
    activate:
      on-profile: perf-3

  datasource:
    hikari:
      maximum-pool-size: 3
      minimum-idle: 1

logging:
  level:
    root: WARN
    my.project.userservice: WARN
    org.springframework.security: WARN
    org.hibernate.SQL: WARN

---

spring:
  config:
    activate:
      on-profile: perf-5

  datasource:
    hikari:
      maximum-pool-size: 2
      minimum-idle: 1

logging:
  level:
    root: WARN
    my.project.userservice: WARN
    org.springframework.security: WARN
    org.hibernate.SQL: WARN

---

spring:
  config:
    activate:
      on-profile: prod

  datasource:
    hikari:
      maximum-pool-size: 4
      minimum-idle: 1

logging:
  level:
    root: INFO
    my.project.userservice: INFO
    org.springframework.security: WARN
    org.hibernate.SQL: WARN
```

## `restaurant-service`

```yaml
spring:
  profiles:
    default: dev

  datasource:
    hikari:
      connection-timeout: 3000
      idle-timeout: 30000
      max-lifetime: 600000

---

spring:
  config:
    activate:
      on-profile: dev

  datasource:
    hikari:
      maximum-pool-size: 8
      minimum-idle: 2

logging:
  level:
    root: INFO
    my.project.restaurantservice: DEBUG
    org.hibernate.SQL: WARN

---

spring:
  config:
    activate:
      on-profile: perf-3

  datasource:
    hikari:
      maximum-pool-size: 4
      minimum-idle: 1

logging:
  level:
    root: WARN
    my.project.restaurantservice: WARN
    org.hibernate.SQL: WARN

---

spring:
  config:
    activate:
      on-profile: perf-5

  datasource:
    hikari:
      maximum-pool-size: 3
      minimum-idle: 1

logging:
  level:
    root: WARN
    my.project.restaurantservice: WARN
    org.hibernate.SQL: WARN

---

spring:
  config:
    activate:
      on-profile: prod

  datasource:
    hikari:
      maximum-pool-size: 8
      minimum-idle: 2

logging:
  level:
    root: INFO
    my.project.restaurantservice: INFO
    org.hibernate.SQL: WARN
```

## `booking-service`

```yaml
spring:
  profiles:
    default: dev

  datasource:
    hikari:
      connection-timeout: 3000
      idle-timeout: 30000
      max-lifetime: 600000

---

spring:
  config:
    activate:
      on-profile: dev

  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2

logging:
  level:
    root: INFO
    my.project.bookingservice: DEBUG
    org.hibernate.SQL: WARN

---

spring:
  config:
    activate:
      on-profile: perf-3

  datasource:
    hikari:
      maximum-pool-size: 5
      minimum-idle: 1

logging:
  level:
    root: WARN
    my.project.bookingservice: WARN
    org.hibernate.SQL: WARN

---

spring:
  config:
    activate:
      on-profile: perf-5

  datasource:
    hikari:
      maximum-pool-size: 4
      minimum-idle: 1

logging:
  level:
    root: WARN
    my.project.bookingservice: WARN
    org.hibernate.SQL: WARN

---

spring:
  config:
    activate:
      on-profile: prod

  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2

logging:
  level:
    root: INFO
    my.project.bookingservice: INFO
    org.hibernate.SQL: WARN
```

## `notification-service`

```yaml
spring:
  profiles:
    default: dev

  datasource:
    hikari:
      connection-timeout: 3000
      idle-timeout: 30000
      max-lifetime: 600000

---

spring:
  config:
    activate:
      on-profile: dev

  datasource:
    hikari:
      maximum-pool-size: 3
      minimum-idle: 1

logging:
  level:
    root: INFO
    my.project.notificationservice: DEBUG
    org.hibernate.SQL: WARN
    org.springframework.kafka: INFO

---

spring:
  config:
    activate:
      on-profile: perf-3

  datasource:
    hikari:
      maximum-pool-size: 2
      minimum-idle: 1

logging:
  level:
    root: WARN
    my.project.notificationservice: WARN
    org.hibernate.SQL: WARN
    org.springframework.kafka: WARN

---

spring:
  config:
    activate:
      on-profile: perf-5

  datasource:
    hikari:
      maximum-pool-size: 1
      minimum-idle: 1

logging:
  level:
    root: WARN
    my.project.notificationservice: WARN
    org.hibernate.SQL: WARN
    org.springframework.kafka: WARN

---

spring:
  config:
    activate:
      on-profile: prod

  datasource:
    hikari:
      maximum-pool-size: 3
      minimum-idle: 1

logging:
  level:
    root: INFO
    my.project.notificationservice: INFO
    org.hibernate.SQL: WARN
    org.springframework.kafka: WARN
```

## Как выбирать профиль

Для обычной разработки:

```text
SPRING_PROFILES_ACTIVE=dev
```

Для нагрузочного тестирования с 3 репликами:

```text
SPRING_PROFILES_ACTIVE=perf-3
```

Для стресс-тестирования с 5 репликами:

```text
SPRING_PROFILES_ACTIVE=perf-5
```

Для стабильного запуска без подробного debug-логирования:

```text
SPRING_PROFILES_ACTIVE=prod
```

## Пример запуска из командной строки

```bash
java -jar booking-service.jar --spring.profiles.active=perf-3
```

```bash
java -jar restaurant-service.jar --spring.profiles.active=perf-3
```

## Пример для Docker Compose

В `docker-compose.yml` можно добавить переменную окружения для нужного сервиса:

```yaml
booking-service:
  environment:
    SPRING_PROFILES_ACTIVE: perf-3
```

Для стресс-теста:

```yaml
booking-service:
  environment:
    SPRING_PROFILES_ACTIVE: perf-5
```

## Важное замечание

Эти профили являются стартовыми заготовками. После нагрузочных тестов значения можно корректировать:

- если появляются ошибки `Connection is not available`, можно немного увеличить `maximum-pool-size`;
- если PostgreSQL сильно нагружен и увеличение пула не ускоряет систему, нужно уменьшить `maximum-pool-size`;
- если сервис запускается в большем количестве реплик, пул на одну реплику лучше уменьшать;
- если сервис запускается в одной реплике, пул можно сделать немного больше.
