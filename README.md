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
