# restaurant-service

Сервис управления ресторанами, менеджерами, столами, блюдами и фотографиями.

## Что делает сервис

- Создает и редактирует рестораны.
- Хранит категории, контакты и часы работы ресторана.
- Управляет менеджерами ресторана.
- Управляет столами, включая массовое создание и обновление layout.
- Управляет блюдами.
- Подготавливает presigned upload в MinIO и подтверждает загрузку фотографий.
- Кэширует данные чтения в Redis.
- Отдает snapshot данных для `booking-service`.
- Периодически очищает просроченные и удаляемые фото.

## Порт

```text
8081
```

## REST API

### Restaurants

- `POST /api/v1/restaurants`
- `PUT /api/v1/restaurants/{id}`
- `PATCH /api/v1/restaurants/{id}/active`
- `GET /api/v1/restaurants/my`
- `GET /api/v1/restaurants/{id}`
- `GET /api/v1/restaurants`
- `DELETE /api/v1/restaurants/{id}`
- `GET /api/v1/restaurants/categories`

### Managers

- `POST /api/v1/restaurants/{restId}/managers/{managerId}`
- `GET /api/v1/restaurants/{restId}/managers`
- `DELETE /api/v1/restaurants/{restId}/managers/{managerId}`

### Tables

- `POST /api/v1/restaurants/{restId}/tables`
- `POST /api/v1/restaurants/{restId}/tables/all`
- `PUT /api/v1/restaurants/{restId}/tables/{id}`
- `PUT /api/v1/restaurants/{restId}/tables/layout`
- `DELETE /api/v1/restaurants/{restId}/tables/{id}`
- `GET /api/v1/restaurants/{restId}/tables/{id}`

### Dishes

- `POST /api/v1/restaurants/{restId}/dishes`
- `PUT /api/v1/restaurants/{restId}/dishes/{id}`
- `DELETE /api/v1/restaurants/{restId}/dishes/{id}`
- `GET /api/v1/restaurants/{restId}/dishes/{id}`

### Photos

Для обоих container types `restaurants` и `dishes`:

- `POST /api/v1/{container}/{containerId}/photos/uploads`
- `POST /api/v1/{container}/{containerId}/photos/confirm`
- `DELETE /api/v1/{container}/{containerId}/photos/delete`

### Service-to-service endpoints

- `GET /api/v1/restaurants/{restId}/manager-access`
- `POST /api/v1/restaurants/{restId}/booking-snapshot`

## Доступ

- Все `GET /api/v1/restaurants/**` разрешены без токена.
- Операции записи требуют access token.
- Методы управления защищены ролями `ROLE_MANAGER` или `ROLE_ADMIN`.

## Данные

Собственная БД: `restaurantdb`.

Ключевые сущности:

- `RestaurantEntity`
- `ManagerEntity`
- `TableEntity`
- `DishEntity`
- `PhotoEntity`
- `WorkingHoursEntity`
- `ContactEntity`

`RestaurantDto` в текущей версии содержит:

- базовые данные ресторана
- `workingHours`
- `contacts`
- вложенные `dishes`
- вложенные `tables`
- `photos`

## Фото

Поддерживаемые content type:

- `image/jpeg`
- `image/png`
- `image/webp`

Категории фото:

- `BANNER`
- `SCHEME`
- `GALLERY`

Статусы фото:

- `PENDING`
- `ACTIVE`
- `EXPIRED`
- `DELETING`

Фоновая очистка фото запускается каждые `10` минут.

## Кэширование

Используется Redis с key prefix `restaurant-service::`.

В конфигурации заданы отдельные cache names и TTL:

- рестораны: `5` минут
- блюда и столы: `2-5` минут
- фото: `1` минута
- `managerAccess`: `30` минут
- `restaurantBookingTable`: `2` минуты

## Интеграции

- MinIO для хранения медиа
- Redis для кэша
- `user-service` через OpenFeign:
  - `POST /api/v1/users/change-role-by-id`
  - `POST /api/v1/users/summaries`

При добавлении менеджера сервис переводит пользователя в `ROLE_MANAGER`. Если после удаления связей менеджер больше не прикреплен ни к одному ресторану, роль переводится обратно в `ROLE_USER`.

## Конфигурация

Обязательные переменные окружения:

- `DB_HOST`
- `DB_PORT`
- `DB_USER`
- `DB_PASS`
- `REDIS_HOST`
- `REDIS_PORT`
- `MINIO_ENDPOINT`
- `MINIO_ROOT_USER`
- `MINIO_ROOT_PASSWORD`
- `MINIO_PUBLIC_BASE_URL`
- `JWT_SECRET`
- `EUREKA_URL`

Основные настройки лежат в `src/main/resources/application.yml`.

## Технологии

- Spring Boot
- Spring Web
- Spring Security OAuth2 Resource Server
- Spring Data JPA
- Spring Validation
- Spring Cache
- Spring Data Redis
- Spring Cloud OpenFeign
- Spring Cloud Netflix Eureka Client
- MinIO Java SDK
- MapStruct
- Springdoc OpenAPI
- Java 17

## Swagger и Actuator

- Swagger UI: `http://localhost:8081/swagger-ui/index.html`
- OpenAPI: `http://localhost:8081/v3/api-docs`

Открыты actuator endpoints:

- `health`
- `info`
- `metrics`
- `prometheus`

## Лицензия

См. корневой `readme.md` и файл `LICENSE`.
