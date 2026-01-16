# Хранение данных и базы данных

Проект построен по принципам **микросервисной архитектуры**, где **каждый сервис владеет своей собственной базой данных**.
Прямой доступ к данным других сервисов запрещён — взаимодействие происходит через HTTP API или события (Kafka).

Для всех сервисов используется **PostgreSQL**, развёрнутый как один сервер с несколькими логическими базами данных.

---

### Общий подход к данным

* каждый микросервис имеет **свою отдельную базу данных**;
* между базами **нет внешних ключей**;
* связи между сущностями разных сервисов осуществляются **по идентификаторам (UUID)**;
* схема БД управляется через **Flyway migrations**;
* все базы создаются автоматически при запуске через `docker-compose`.

---

## User Service Database (`userdb`)

База данных `userdb` хранит пользователей системы, их роли и информацию о том, какими ресторанами управляют менеджеры.

### Таблица `users`

Хранит основные данные пользователя и его роль в системе.

| Поле            | Тип         | Описание                                 |
|-----------------|-------------|------------------------------------------|
| `id`            | UUID        | Уникальный идентификатор пользователя    |
| `name`          | varchar     | Имя пользователя                         | 
| `surname`       | varchar     | Фамилия пользователя                     |
| `date_of_birth` | date        | Дата рождения пользователя               |
| `phone`         | varchar     | Номер телефона пользователя              |
| `email`         | varchar     | Email пользователя (уникальный логин)    |
| `password_hash` | varchar     | Хэш пароля (bcrypt)                      |
| `role`          | varchar     | Роль пользователя: `USER`, `MANAGER`, `OWNER` |
| `enabled`       | boolean     | Активен ли аккаунт                       |
| `created_at`    | timestamptz | Дата создания                            |
| `updated_at`    | timestamptz | Дата последнего обновления               |

### Таблица `manager_restaurants`

Используется для реализации **ownership-модели**: какие рестораны принадлежат какому менеджеру.

| Поле            | Тип         | Описание                             |
| --------------- | ----------- | ------------------------------------ |
| `manager_id`    | UUID        | ID пользователя с ролью `MANAGER`    |
| `restaurant_id` | UUID        | ID ресторана (из Restaurant Service) |
| `created_at`    | timestamptz | Дата назначения                      |

* составной primary key `(manager_id, restaurant_id)`
* используется API Gateway для проверки прав менеджера

---

Отлично, это **очень правильный шаг** 👍
Ниже — **доработанная финальная спецификация схемы `restaurantdb`**, оформленная **как полноценная техническая документация**, с явным указанием:

* `PK / FK`
* `NOT NULL / NULL`
* `UNIQUE`
* индексов
* бизнес-ограничений

Такой документ спокойно можно:

* класть в `README.md`
* использовать как основу для **Liquibase**
* показывать на ревью / защите проекта

---



## Restaurant Service Database: `restaurantdb`

База данных `restaurantdb` хранит **справочные данные ресторанов**, их контактную информацию, столы, меню и **метаданные изображений**.

Изображения хранятся во **внешнем object storage (MinIO / S3-compatible)**.
В базе данных сохраняются **только метаданные и ссылки**.

---

## 📌 Table: `restaurants`

Хранит **основную информацию о ресторанах**.

| Column        | Type         | Constraints  | Description                        |
| ------------- | ------------ | ------------ | ---------------------------------- |
| `id`          | UUID         | PK, NOT NULL | Уникальный идентификатор ресторана |
| `name`        | varchar(255) | NOT NULL     | Название ресторана                 |
| `category`    | varchar(100) | NOT NULL     | Категория ресторана                |
| `description` | text         | NULL         | Описание ресторана                 |
| `address`     | varchar(255) | NOT NULL     | Физический адрес                   |
| `is_active`   | boolean      | NOT NULL     | Признак активности                 |
| `start_time`  | time         | NOT NULL     | Время открытия                     |
| `end_time`    | time         | NOT NULL     | Время закрытия                     |
| `created_at`  | timestamptz  | NOT NULL     | Дата создания                      |
| `updated_at`  | timestamptz  | NOT NULL     | Дата обновления                    |

**Indexes**

* PK: `restaurants_pkey (id)`

---

## 📞 Table: `restaurant_contacts`

Контактная информация ресторана.

| Column          | Type         | Constraints  | Description            |
| --------------- | ------------ | ------------ | ---------------------- |
| `id`            | UUID         | PK, NOT NULL | Идентификатор контакта |
| `restaurant_id` | UUID         | FK, NOT NULL | Ресторан               |
| `type`          | varchar(50)  | NOT NULL     | Тип контакта           |
| `value`         | varchar(255) | NOT NULL     | Значение               |
| `created_at`    | timestamptz  | NOT NULL     | Дата создания          |

**Constraints**

* FK: `restaurant_contacts.restaurant_id → restaurants.id`

**Indexes**

* `idx_restaurant_contacts_restaurant_id (restaurant_id)`

---

## 📸 Table: `restaurant_photos`

Метаданные изображений ресторана.

| Column          | Type         | Constraints      | Description               |
| --------------- | ------------ | ---------------- | ------------------------- |
| `id`            | UUID         | PK, NOT NULL     | Идентификатор изображения |
| `restaurant_id` | UUID         | FK, NOT NULL     | Ресторан                  |
| `object_key`    | varchar(512) | NOT NULL, UNIQUE | Ключ объекта в MinIO      |
| `url`           | varchar(500) | NULL             | Публичный URL             |
| `is_main`       | boolean      | NOT NULL         | Главное изображение       |
| `sort_order`    | integer      | NOT NULL         | Порядок                   |
| `content_type`  | varchar(100) | NOT NULL         | MIME-тип                  |
| `size_bytes`    | bigint       | NOT NULL         | Размер файла              |
| `created_at`    | timestamptz  | NOT NULL         | Дата загрузки             |

**Constraints**

* FK: `restaurant_photos.restaurant_id → restaurants.id`

**Indexes**

* `idx_restaurant_photos_restaurant_id (restaurant_id)`
* `uk_restaurant_photos_object_key (object_key)`

---

## 🍽 Table: `restaurant_tables`

Столы ресторана.

| Column          | Type        | Constraints  | Description         |
| --------------- | ----------- | ------------ | ------------------- |
| `id`            | UUID        | PK, NOT NULL | Идентификатор стола |
| `restaurant_id` | UUID        | FK, NOT NULL | Ресторан            |
| `table_number`  | integer     | NOT NULL     | Номер стола         |
| `capacity`      | integer     | NOT NULL     | Вместимость         |
| `is_active`     | boolean     | NOT NULL     | Активен             |
| `created_at`    | timestamptz | NOT NULL     | Дата создания       |
| `updated_at`    | timestamptz | NOT NULL     | Дата обновления     |

**Constraints**

* FK: `restaurant_tables.restaurant_id → restaurants.id`
* UNIQUE: `(restaurant_id, table_number)`

**Indexes**

* `idx_restaurant_tables_restaurant_id (restaurant_id)`

---

## 🍕 Table: `dishes`

Блюда ресторана.

| Column          | Type         | Constraints  | Description         |
| --------------- | ------------ | ------------ | ------------------- |
| `id`            | UUID         | PK, NOT NULL | Идентификатор блюда |
| `restaurant_id` | UUID         | FK, NOT NULL | Ресторан            |
| `name`          | varchar(255) | NOT NULL     | Название            |
| `category`      | varchar(100) | NOT NULL     | Категория           |
| `description`   | text         | NULL         | Описание            |
| `price_cents`   | integer      | NOT NULL     | Цена                |
| `weight`        | integer      | NOT NULL     | Вес (г)             |
| `is_available`  | boolean      | NOT NULL     | Доступно            |
| `created_at`    | timestamptz  | NOT NULL     | Дата создания       |
| `updated_at`    | timestamptz  | NOT NULL     | Дата обновления     |

**Constraints**

* FK: `dishes.restaurant_id → restaurants.id`

**Indexes**

* `idx_dishes_restaurant_id (restaurant_id)`

---

## 🖼 Table: `dish_photos`

Метаданные изображений блюд.

| Column         | Type         | Constraints      | Description   |
| -------------- | ------------ | ---------------- | ------------- |
| `id`           | UUID         | PK, NOT NULL     | Идентификатор |
| `dish_id`      | UUID         | FK, NOT NULL     | Блюдо         |
| `object_key`   | varchar(512) | NOT NULL, UNIQUE | Ключ MinIO    |
| `url`          | varchar(500) | NULL             | URL           |
| `is_main`      | boolean      | NOT NULL         | Главное       |
| `sort_order`   | integer      | NOT NULL         | Порядок       |
| `content_type` | varchar(100) | NOT NULL         | MIME          |
| `size_bytes`   | bigint       | NOT NULL         | Размер        |
| `created_at`   | timestamptz  | NOT NULL         | Дата          |

**Constraints**

* FK: `dish_photos.dish_id → dishes.id`

**Indexes**

* `idx_dish_photos_dish_id (dish_id)`
* `uk_dish_photos_object_key (object_key)`

---

## 🔗 Logical Model

```
restaurants
 ├── restaurant_contacts
 ├── restaurant_photos
 ├── restaurant_tables
 └── dishes
       └── dish_photos
```

---

## 🧠 Design Decisions

* Все PK — `UUID`
* Все FK — `NOT NULL`
* Межсервисные связи — **только через UUID**
* Изображения — через object storage
* Схема оптимизирована под **Liquibase + JPA**



## Booking Service Database (`bookingdb`)

База данных `bookingdb` хранит информацию о бронированиях столов и **предзаказах блюд**, связанных с бронированием.

Booking Service отвечает за:

* создание и отмену бронирований,
* контроль доступности столов по времени,
* хранение состава предзаказа (блюда + количество),
* публикацию событий о бронированиях.

---

### Таблица `bookings`

Основная таблица бронирований столов в ресторанах.

| Поле            | Тип         | Описание                              |
| --------------- | ----------- | ------------------------------------- |
| `id`            | UUID        | Идентификатор бронирования            |
| `user_id`       | UUID        | Пользователь, создавший бронирование  |
| `restaurant_id` | UUID        | Ресторан                              |
| `table_id`      | UUID        | Забронированный стол                  |
| `start_time`    | timestamptz | Время начала бронирования             |
| `end_time`      | timestamptz | Время окончания бронирования          |
| `status`        | varchar     | Статус брони (`CREATED`, `CANCELLED`) |
| `created_at`    | timestamptz | Дата создания                         |
| `updated_at`    | timestamptz | Дата обновления                       |

#### Бизнес-правила

* `end_time` всегда больше `start_time`
* запрещены пересечения бронирований по времени для одного стола
* пользователь может отменять **только свои** бронирования
* OWNER может управлять любыми бронированиями

---

### Таблица `booking_items`

Таблица предзаказанных блюд, связанных с конкретным бронированием.

Каждая запись означает:

> *«В рамках бронирования заказано N единиц конкретного блюда»*

| Поле           | Тип         | Описание                         |
| -------------- | ----------- | -------------------------------- |
| `id`           | UUID        | Идентификатор позиции предзаказа |
| `booking_id`   | UUID        | Бронирование                     |
| `menu_item_id` | UUID        | Блюдо из меню ресторана          |
| `quantity`     | int         | Количество заказанных блюд       |
| `price_cents`  | int         | Цена блюда на момент заказа      |
| `created_at`   | timestamptz | Дата добавления                  |

#### Важные моменты

* `price_cents` **фиксируется на момент бронирования**
  (изменение цены в меню не влияет на старые предзаказы)
* `menu_item_id` — это ID из Restaurant Service
* блюда можно:

  * добавить при создании бронирования,
  * изменить/удалить **до начала бронирования**

---

### Связь бронирования и предзаказа

* одно бронирование (`bookings`)
  → **несколько** позиций предзаказа (`booking_items`)
* предзаказ существует **только вместе с бронированием**
* при отмене бронирования все связанные `booking_items` считаются отменёнными

---

### Пример сценария с предзаказом

1. Пользователь выбирает ресторан и стол
2. Выбирает время бронирования
3. Добавляет блюда в предзаказ:

  * Паста × 2
  * Салат × 1
4. Создаёт бронирование
5. Booking Service:

  * сохраняет запись в `bookings`
  * сохраняет позиции в `booking_items`
  * публикует событие `booking.created`

---

### Ограничения и целостность данных

* предзаказанные блюда должны принадлежать тому же ресторану, что и бронирование
  (проверяется на уровне сервиса)
* количество блюд `quantity > 0`
* редактирование предзаказа запрещено после начала бронирования

---

## Notification Service Database (`notificationdb`)

База данных `notificationdb` используется для хранения истории уведомлений и обеспечения идемпотентности обработки Kafka-событий.

### Таблица `notifications`

| Поле         | Тип         | Описание                             |
| ------------ | ----------- | ------------------------------------ |
| `id`         | UUID        | Идентификатор уведомления            |
| `user_id`    | UUID        | Получатель                           |
| `booking_id` | UUID        | Связанное бронирование               |
| `channel`    | varchar     | Канал (`EMAIL`, `IN_APP`)            |
| `status`     | varchar     | Статус (`PENDING`, `SENT`, `FAILED`) |
| `message`    | text        | Текст уведомления                    |
| `created_at` | timestamptz | Дата создания                        |
| `sent_at`    | timestamptz | Дата отправки                        |

---

### Таблица `processed_messages`

Используется для **идемпотентности Kafka consumer’а** — предотвращает повторную обработку одного и того же события.

| Поле           | Тип         | Описание                  |
| -------------- | ----------- | ------------------------- |
| `message_key`  | varchar     | Уникальный ключ сообщения |
| `processed_at` | timestamptz | Время обработки           |

---

## Итоговая схема данных

* **userdb**
  `users`, `manager_restaurants`

* **restaurantdb**
  `restaurants`, `restaurant_tables`, `menu_items`

* **bookingdb**
  `bookings`

* **notificationdb**
  `notifications`, `processed_messages`

---
