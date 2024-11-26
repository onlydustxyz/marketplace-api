create table if not exists github_outbox_commands
(
    id              bigserial primary key,
    payload         jsonb                                                      not null,
    status          outbox_event_status default 'PENDING'::outbox_event_status not null,
    error           text,
    tech_created_at timestamptz         default now()                          not null,
    tech_updated_at timestamptz         default now()                          not null
);

create trigger github_outbox_commands_set_tech_updated_at
    before update
    on github_outbox_commands
    for each row
execute procedure set_tech_updated_at();