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
