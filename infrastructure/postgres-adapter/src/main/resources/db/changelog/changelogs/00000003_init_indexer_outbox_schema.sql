create schema if not exists indexer_outbox;

create type indexer_outbox.outbox_event_status as enum ('PENDING', 'PROCESSED', 'FAILED', 'SKIPPED');

create table if not exists indexer_outbox.api_events
(
    id              bigserial
        primary key,
    payload         jsonb                                                                                    not null,
    status          indexer_outbox.outbox_event_status default 'PENDING'::indexer_outbox.outbox_event_status not null,
    error           text,
    tech_created_at timestamp                          default now()                                         not null,
    tech_updated_at timestamp                          default now()                                         not null
);

create index if not exists api_events_status_id_index
    on indexer_outbox.api_events (status, id);

create trigger indexer_outbox_api_events_set_tech_updated_at
    before update
    on indexer_outbox.api_events
    for each row
execute procedure public.set_tech_updated_at();
