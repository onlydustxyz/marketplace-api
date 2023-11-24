create table public.project_more_infos
(
    project_id uuid      not null,
    url        text      not null,
    name       text,
    created_at timestamp not null default now(),
    updated_at timestamp
);