create schema if not exists iam;

create type iam.user_role as enum ('ADMIN', 'USER');

create type iam.backoffice_user_role as enum ('BO_FINANCIAL_ADMIN', 'BO_MARKETING_ADMIN', 'BO_READER');

create type iam.notification_category as enum ('MAINTAINER_PROJECT_CONTRIBUTOR', 'CONTRIBUTOR_REWARD', 'CONTRIBUTOR_PROJECT', 'MAINTAINER_PROJECT_PROGRAM', 'GLOBAL_BILLING_PROFILE', 'GLOBAL_MARKETING', 'SPONSOR_LEAD', 'PROGRAM_LEAD');

create type iam.notification_channel as enum ('IN_APP', 'EMAIL', 'SUMMARY_EMAIL');

create table if not exists iam.users
(
    id                uuid                    not null
        primary key,
    github_user_id    bigint                  not null,
    github_login      text                    not null,
    github_avatar_url text                    not null,
    roles             iam.user_role[]         not null,
    created_at        timestamp default now() not null,
    updated_at        timestamp default now() not null,
    email             text                    not null,
    last_seen_at      timestamp default now() not null
);

create unique index if not exists users_github_user_id_uindex
    on iam.users (github_user_id);

create unique index if not exists users_id_github_user_id_idx
    on iam.users (id, github_user_id);

create unique index if not exists users_github_user_id_id_idx
    on iam.users (github_user_id, id);

create table if not exists iam.backoffice_users
(
    id         uuid                       not null
        primary key,
    email      text                       not null,
    name       text                       not null,
    avatar_url text,
    roles      iam.backoffice_user_role[] not null,
    created_at timestamp default CURRENT_TIMESTAMP,
    updated_at timestamp default CURRENT_TIMESTAMP
);

create unique index if not exists backoffice_users_email_idx
    on iam.backoffice_users (email);


create table if not exists iam.user_notification_settings_channels
(
    user_id         uuid                      not null
        references iam.users,
    category        iam.notification_category not null,
    tech_created_at timestamp default now()   not null,
    channel         iam.notification_channel  not null
);

create table if not exists iam.notifications
(
    id              uuid                      not null
        primary key,
    recipient_id    uuid                      not null
        references iam.users,
    category        iam.notification_category not null,
    data            jsonb                     not null,
    created_at      timestamp                 not null,
    tech_created_at timestamp default now()   not null
);

create table if not exists iam.notification_channels
(
    notification_id uuid                     not null,
    sent_at         timestamp,
    tech_created_at timestamp default now()  not null,
    read_at         timestamp,
    channel         iam.notification_channel not null,
    constraint notification_channels_pk
        primary key (notification_id, channel)
);


