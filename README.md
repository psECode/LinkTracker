# LinkTracker

LinkTracker – система отслеживания изменений на веб-страницах (GitHub, StackOverflow) с уведомлениями.
Веб-интерфейс (React SPA) + Telegram-бот как параллельный канал доставки.

## Состав сервисов

|   Сервис   | Назначение                                                                               |
|------------|------------------------------------------------------------------------------------------|
| `frontend` | React SPA + nginx: статика, прокси `/api` и `/auth` на scrapper                          |
| `scrapper` | REST API, авторизация JWT, БД (Postgres), планировщик проверок ссылок, inbox-уведомления |
| `bot`      | Telegram-бот; доставляет уведомления пользователям с привязанным Telegram                |
| `ai-agent` | Kafka-процессор: raw → processed (приоритизация, группировка)                            |

Инфраструктура: Postgres (`db`), Redis (Valkey), Kafka + Schema Registry, kafka-ui (8085), Prometheus (9090), Grafana (3000).

## Запуск в Docker

```bash
cp .env.example .env

docker compose up -d
```

Сайт: **http://localhost:8080** — регистрация, вход, список ссылок, уведомления.

Бот требует `TELEGRAM_TOKEN` в `.env`: без токена его контейнер стартует
и сразу завершается с ошибкой валидации (fail-fast) — сайт при этом работает.

## Локальный запуск без Docker

1. Поднять инфраструктуру: `docker compose up -d db redis kafka schema-registry`
2. scrapper: `./mvnw -pl scrapper spring-boot:run` (порт 8081; миграции Liquibase применяются автоматически)
3. bot: `TELEGRAM_TOKEN=... ./mvnw -pl bot spring-boot:run`
4. фронтенд dev-режим: `cd frontend && npm i && npm run dev` (Vite проксирует `/api` и `/auth` на localhost:8081)

## API scrapper (кратко)

- `POST /auth/register {email, password}` — регистрация веб-пользователя
- `POST /auth/login {email, password}` → `{accessToken}` (JWT HS256, TTL 24 ч)
- `GET /auth/me` — Bearer-токен
- `GET/POST/DELETE /api/links` — подписки, Bearer-токен
- `GET /api/notifications?unread=…&limit=…`, `POST /api/notifications/read {ids|all}` — Bearer-токен
- `/internal/**` — только для сервисов, заголовок `X-Internal-Token` (общий ключ bot<->scrapper)

Полезную для разработки проекта информацию вы можете найти в файле [HELP.md](./HELP.md),
план миграции с бота на сайт — в [PLAN.md](./PLAN.md).
