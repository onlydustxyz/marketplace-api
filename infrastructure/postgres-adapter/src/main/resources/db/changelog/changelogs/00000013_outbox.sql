ALTER TYPE notification_status RENAME TO outbox_event_status;

ALTER TABLE notifications
    RENAME TO notification_outbox_events;

ALTER SEQUENCE notifications_id_seq RENAME TO notification_outbox_events_id_seq;

CREATE TABLE indexer_outbox_events
(
    id         BIGSERIAL PRIMARY KEY,
    payload    jsonb               NOT NULL,
    status     outbox_event_status NOT NULL DEFAULT 'PENDING',
    error      TEXT,
    created_at TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP           NOT NULL
);