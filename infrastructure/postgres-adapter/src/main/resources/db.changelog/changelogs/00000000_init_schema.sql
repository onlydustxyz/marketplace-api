create schema iam;

create table iam.users
(
    id                uuid primary key,
    github_user_id    bigint    not null,
    github_login      text      not null,
    github_avatar_url text      not null,
    created_at        timestamp not null default now(),
    updated_at        timestamp not null default now()
);