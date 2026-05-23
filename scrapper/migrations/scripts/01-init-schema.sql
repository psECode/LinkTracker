-- Liquibase formatted sql
-- changeset author:init_schema

CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       telegram_id BIGINT UNIQUE NOT NULL,
                       is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE links (
                       id UUID PRIMARY KEY,
                       url TEXT UNIQUE NOT NULL,
                       type VARCHAR(50) NOT NULL,
                       last_updated TIMESTAMPTZ,
                       next_check_at TIMESTAMPTZ,
                       check_interval VARCHAR(50) NOT NULL
);

CREATE TABLE subscriptions (
                               id UUID PRIMARY KEY,
                               user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               link_id UUID NOT NULL REFERENCES links(id) ON DELETE CASCADE,
                               UNIQUE(user_id, link_id)
);

CREATE TABLE subscription_tags (
                                   subscription_id UUID NOT NULL REFERENCES subscriptions(id) ON DELETE CASCADE,
                                   tag VARCHAR(255) NOT NULL,
                                   PRIMARY KEY (subscription_id, tag)
);
