-- Liquibase formatted sql
-- changeset author:web_auth

-- tgId становится опциональной привязкой канала доставки,
-- идентификация пользователя - UUID (users.id)
ALTER TABLE users ALTER COLUMN telegram_id DROP NOT NULL;

-- веб-идентификация; UNIQUE в Postgres допускает множество NULL (tg-only юзеры)
ALTER TABLE users ADD COLUMN email VARCHAR(255) UNIQUE;
ALTER TABLE users ADD COLUMN password_hash VARCHAR(100);

-- inbox для веб-канала доставки
CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    link_id UUID REFERENCES links(id) ON DELETE SET NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    read_at TIMESTAMPTZ
);

CREATE INDEX idx_notifications_user ON notifications(user_id, created_at DESC);