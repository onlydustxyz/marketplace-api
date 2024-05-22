create table public.mail_outbox_events
(
    id         bigserial
        primary key,
    payload    jsonb                                                                    not null,
    status     public.outbox_event_status default 'PENDING'::public.outbox_event_status not null,
    error      text,
    created_at timestamp                  default now()                                 not null,
    updated_at timestamp                                                                not null
);