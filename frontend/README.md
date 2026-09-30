# Link Tracker — фронтенд

SPA для сайта link-tracker. Стек: **Vite + React 19 + TypeScript**, роутинг — `react-router-dom` v7,
серверное состояние — `@tanstack/react-query` v5. Без UI-библиотек, стили — чистый CSS (`src/styles.css`),
все тексты интерфейса на русском.

## Запуск в dev-режиме

```bash
npm install
npm run dev
```

Откроется http://localhost:5173. Dev-сервер Vite проксирует `/api/*` и `/auth/*`
на `http://localhost:8081` (scrapper) — см. `vite.config.ts`. Бэкенд должен быть
запущен на порту 8081.

## Prod-сборка

```bash
npm run build    # tsc -b && vite build, результат в dist/
npm run preview  # локальный просмотр dist (прокси /api и /auth в preview не работает)
```

## Docker

```bash
docker build -t link-tracker-frontend .
docker run --rm -p 8080:80 link-tracker-frontend
```

Образ multi-stage: на `node:22-alpine` собирается статика (`npm ci` → `npm run build`),
затем `nginx:alpine` её раздаёт. В контейнере nginx проксирует `/api/` и `/auth/`
на `http://scrapper:8081` (см. `nginx.conf`), т.е. предполагается docker-compose,
в котором сервис бэкенда называется `scrapper`.

## Структура

- `src/api/client.ts` — типы и API-функции; токен хранится в `localStorage` (ключ `lt_token`);
  при ответе 401 токен удаляется и диспатчится событие `lt:unauthorized`, по которому
  `AuthContext` сбрасывает пользователя (Layout перебрасывает на `/login`)
- `src/auth/AuthContext.tsx` — контекст авторизации: восстановление сессии по токену,
  `login` / `register` (с автологином) / `logout`
- `src/components/Layout.tsx` — шапка с навигацией
- `src/pages/` — страницы: вход, регистрация, ссылки, уведомления, аккаунт
- `src/App.tsx` — маршруты