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

INSERT INTO accounting.historical_quotes(timestamp, base_id, target_id, price)
SELECT now(), c.id, usd.id, cuq.price
FROM crypto_usd_quotes cuq
         JOIN currencies c ON c.code = UPPER(cuq.currency::TEXT)
         JOIN currencies usd ON usd.code = 'USD';

INSERT INTO accounting.historical_quotes(timestamp, base_id, target_id, price)
SELECT now(), usd.id, usd.id, 1
FROM currencies usd
WHERE usd.code = 'USD';

INSERT INTO accounting.historical_quotes(timestamp, base_id, target_id, price)
SELECT now(), usd.id, eur.id, 0.92
FROM currencies usd
         JOIN currencies eur ON eur.code = 'EUR'
WHERE usd.code = 'USD';

insert into currencies (id, type, name, code, logo_url, decimals, description)
values ('48388edb-fda2-4a32-b228-28152a147500', 'CRYPTO', 'Aptos Coin', 'APT', null, 8, null),
       ('00ca98a5-0197-4b76-a208-4bfc55ea8256', 'CRYPTO', 'Optimism', 'OP', null, 18, null);