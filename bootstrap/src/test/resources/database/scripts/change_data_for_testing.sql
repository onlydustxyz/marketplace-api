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


insert into currencies (id, code, name, type, decimals)
values ('562bbf65-8a71-4d30-ad63-520c0d68ba27', 'USDC', 'USD Coin', 'CRYPTO', 6),
       ('71bdfcf4-74ee-486b-8cfe-5d841dd93d5c', 'ETH', 'Ether', 'CRYPTO', 18),
       ('b9593e4e-61d3-440b-88ff-3410fd72a1eb', 'EUR', 'Euro', 'FIAT', 2)
on conflict do nothing;

insert into erc20(currency_id, blockchain, address, name, symbol, decimals, total_supply)
values ('562bbf65-8a71-4d30-ad63-520c0d68ba27', 'ethereum', '0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48', 'USD Coin', 'USDC', 6, 0);

INSERT INTO accounting.historical_quotes(timestamp, currency_id, base_id, price)
SELECT now(), c.id, usd.id, cuq.price
FROM crypto_usd_quotes cuq
         JOIN currencies c ON c.code = UPPER(cuq.currency::TEXT)
         JOIN currencies usd ON usd.code = 'USD';

INSERT INTO accounting.historical_quotes(timestamp, currency_id, base_id, price)
SELECT now(), usd.id, usd.id, 1
FROM currencies usd
WHERE usd.code = 'USD';

INSERT INTO accounting.historical_quotes(timestamp, currency_id, base_id, price)
SELECT now(), usd.id, eur.id, 0.92
FROM currencies usd
         JOIN currencies eur ON eur.code = 'EUR'
WHERE usd.code = 'USD';
