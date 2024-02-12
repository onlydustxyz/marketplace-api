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

insert into currencies (id, name, code, decimals, type, standard)
values ('f35155b5-6107-4677-85ac-23f8c2a63193', 'US Dollar', 'USD', 2, 'FIAT', 'ISO4217');
