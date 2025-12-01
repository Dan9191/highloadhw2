# Highload HW2 — User Service

Простой реактивный CRUD-сервис для управления пользователями (Person).  
Реализован на **Kotlin + Spring WebFlux + R2DBC**, с метриками Micrometer, Prometheus-эндпоинтом и глобальным rate limiter’ом (1000 RPS).

## Стек

- Kotlin 1.9+
- Spring Boot 3.x (WebFlux)
- R2DBC + PostgreSQL (реактивный драйвер)
- Flyway — миграции БД
- Micrometer + Prometheus
- Resilience4j — rate limiting
- Jakarta Validation

## Сборка и запуск

```bash
# Сборка
./gradlew clean build

# Запуск (по умолчанию порт 8099)
./gradlew bootRun
```

Или через JAR:

```bash
java -jar build/libs/highloadhw2-0.0.1-SNAPSHOT.jar
```

## Конфигурация (application.yaml)

| Переменная                    | По умолчанию                                           | Описание                                |
|-------------------------------|--------------------------------------------------------|-----------------------------------------|
| `SERVER_PORT`                 | 8099                                                   | Порт сервиса                            |
| `DATASOURCE_R2DBC_URL`        | r2dbc:postgresql://localhost:5432/test?...             | R2DBC URL                               |
| `DATASOURCE_USER_NAME`        | test                                                   | Пользователь БД                         |
| `DATASOURCE_USER_PASSWORD`    | test                                                   | Пароль БД                               |
| `DATASOURCE_URL`              | jdbc:postgresql://localhost:5432/test                  | JDBC URL (для Flyway)                   |
| Rate limiter                  | 1000 запросов в секунду (global-api-limit)             | Resilience4j, сразу 429 при превышении  |

## API — Пользователи

Базовый путь: **/api/v1/users**

### 1. Создать пользователя
```http
POST /api/v1/users
Content-Type: application/json

{
  "name": "John Doe",
  "mail": "john@example.com",
  "password": "secret123"
}
```
**Ответ** `201 Created`
```json
{
  "id": 1,
  "name": "John Doe",
  "mail": "john@example.com"
}
```

### 2. Получить всех пользователей (поток)
```http
GET /api/v1/users
```
Возвращает `Flow<PersonViewDto>` → Server-Sent Events / JSON stream.

### 3. Получить пользователя по ID
```http
GET /api/v1/users/1
```
**Ответ** `200 OK` — `PersonViewDto`

### 4. Обновить пользователя (частично)
```http
PUT /api/v1/users/1
Content-Type: application/json

{
  "name": "John Updated",
  "mail": "john.updated@example.com",
  "password": "newPass"
}
```
Все поля опциональные.

### 5. Удалить пользователя
```http
DELETE /api/v1/users/1
```
**Ответ** `204 No Content`

## DTO

| DTO                | Поля                                    | Используется для                     |
|--------------------|-----------------------------------------|--------------------------------------|
| `PersonCreateDto`  | `name`, `mail`, `password` (обязательные) | POST                                 |
| `PersonUpdateDto`  | `name?`, `mail?`, `password?`           | PUT (частичное обновление)           |
| `PersonViewDto`    | `id`, `name`, `mail`                    | Ответы (пароль никогда не возвращается) |

## Метрики и мониторинг

- Prometheus endpoint: `http://localhost:8099/actuator/prometheus`
- Включены метрики `http.requests` с тегами `operation`, `status`, `exception` и т.д.
- Rate limiter: 1000 RPS глобально, при превышении — сразу `429 Too Many Requests`

## База данных

Схема `user_service` создаётся автоматически Flyway’ем при старте.  
Мигации ищутся в `src/main/resources/db/migration`.
