CREATE TABLE IF NOT EXISTS outbox_messages (
    id UUID PRIMARY KEY,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_outbox_status ON outbox_messages(status);
