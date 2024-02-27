CREATE TYPE accounting.outbox_event_status as enum ('PENDING', 'PROCESSED', 'FAILED', 'SKIPPED');

CREATE TABLE accounting.billing_profile_verification_outbox_events
(
    id         BIGSERIAL PRIMARY KEY,
    payload    jsonb                          NOT NULL,
    status     accounting.outbox_event_status NOT NULL DEFAULT 'PENDING',
    error      TEXT,
    created_at TIMESTAMP                      NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP                      NOT NULL
);

INSERT INTO accounting.billing_profile_verification_outbox_events (id, payload, error, created_at, updated_at, status)
SELECT old_event.id,
       old_event.payload,
       old_event.error,
       old_event.created_at,
       old_event.updated_at,
       case
           when old_event.status = 'PENDING' then cast('PENDING' as accounting.outbox_event_status)
           when old_event.status = 'PROCESSED' then cast('PROCESSED' as accounting.outbox_event_status)
           when old_event.status = 'SKIPPED' then cast('SKIPPED' as accounting.outbox_event_status)
           when old_event.status = 'FAILED' then cast('FAILED' as accounting.outbox_event_status)
           end
FROM public.user_verification_outbox_events old_event;

DROP TABLE public.user_verification_outbox_events;


