CREATE TYPE notification_status AS ENUM ('PENDING', 'PROCESSED', 'FAILED');

CREATE TABLE notifications
(
    id         BIGSERIAL PRIMARY KEY,
    payload    jsonb               NOT NULL,
    status     notification_status NOT NULL DEFAULT 'PENDING',
    error      TEXT,
    created_at TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP           NOT NULL
)