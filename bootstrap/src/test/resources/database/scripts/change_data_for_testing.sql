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


insert into accounting.historical_quotes
SELECT now(), aptos.id, usd.id, 0.30
FROM currencies aptos
         JOIN currencies usd on usd.code = 'USD'
where aptos.code = 'APT';

insert into accounting.latest_quotes
SELECT now(), aptos.id, usd.id, 0.30
FROM currencies aptos
         JOIN currencies usd on usd.code = 'USD'
where aptos.code = 'APT';

insert into accounting.latest_usd_quotes
SELECT now(), aptos.id, 0.30
FROM currencies aptos
where aptos.code = 'APT';