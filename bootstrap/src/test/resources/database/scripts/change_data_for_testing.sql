-- Needed to make migrations pass
create schema if not exists auth;
create table if not exists auth.users
(
    id uuid primary key
);
create table if not exists auth.user_providers
(
    id uuid primary key
);
