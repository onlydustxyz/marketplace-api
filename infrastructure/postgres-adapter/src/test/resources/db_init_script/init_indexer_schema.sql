create schema indexer;

create type indexer.job_status as enum ('PENDING', 'RUNNING', 'SUCCESS', 'FAILED');

create table indexer.databasechangeloglock
(
    id          integer not null
        primary key,
    locked      boolean not null,
    lockgranted timestamp,
    lockedby    varchar(255)
);

create table indexer.databasechangelog
(
    id            varchar(255) not null,
    author        varchar(255) not null,
    filename      varchar(255) not null,
    dateexecuted  timestamp    not null,
    orderexecuted integer      not null,
    exectype      varchar(10)  not null,
    md5sum        varchar(35),
    description   varchar(255),
    comments      varchar(255),
    tag           varchar(255),
    liquibase     varchar(20),
    contexts      varchar(255),
    labels        varchar(255),
    deployment_id varchar(10)
);

create table indexer.repo_indexing_jobs
(
    repo_id         bigint                                                   not null
        constraint repo_indexing_job_triggers_pkey
            primary key,
    installation_id bigint,
    tech_created_at timestamp          default now()                         not null,
    tech_updated_at timestamp          default now()                         not null,
    suspended_at    timestamp,
    status          indexer.job_status default 'PENDING'::indexer.job_status not null,
    started_at      timestamp,
    finished_at     timestamp
);

create trigger indexer_repo_indexing_jobs_set_tech_updated_at
    before update
    on indexer.repo_indexing_jobs
    for each row
execute procedure public.set_tech_updated_at();

create table indexer.user_indexing_jobs
(
    user_id         bigint                                                   not null
        constraint user_indexing_job_triggers_pkey
            primary key,
    tech_created_at timestamp          default now()                         not null,
    tech_updated_at timestamp          default now()                         not null,
    status          indexer.job_status default 'PENDING'::indexer.job_status not null,
    started_at      timestamp,
    finished_at     timestamp
);

create trigger indexer_user_indexing_jobs_set_tech_updated_at
    before update
    on indexer.user_indexing_jobs
    for each row
execute procedure public.set_tech_updated_at();

create table indexer.notifier_jobs
(
    id                serial
        primary key,
    tech_created_at   timestamp          default now()                         not null,
    tech_updated_at   timestamp          default now()                         not null,
    last_notification timestamp,
    status            indexer.job_status default 'PENDING'::indexer.job_status not null,
    started_at        timestamp,
    finished_at       timestamp
);

create trigger indexer_notifier_jobs_set_tech_updated_at
    before update
    on indexer.notifier_jobs
    for each row
execute procedure public.set_tech_updated_at();

