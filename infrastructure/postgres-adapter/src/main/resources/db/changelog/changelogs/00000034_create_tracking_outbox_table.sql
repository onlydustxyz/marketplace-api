CREATE TABLE tracking_outbox_events
(
    id         BIGSERIAL PRIMARY KEY,
    payload    jsonb               NOT NULL,
    status     outbox_event_status NOT NULL DEFAULT 'PENDING',
    error      TEXT,
    created_at TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP           NOT NULL
);