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

INSERT INTO public.currencies (id, type, name, code, logo_url, decimals, description, tech_created_at, tech_updated_at)
VALUES ('b027f7af-e6e6-45c0-a78e-fead6aacd10c', 'CRYPTO', 'Ethereum', 'ETH', 'https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png', 18,
        'Ethereum (ETH) is a cryptocurrency . Ethereum has a current supply of 120,176,767.96525423. The last known price of Ethereum is 2,487.17042251 USD and is up 2.57 over the last 24 hours. It is currently trading on 8393 active market(s) with $12,727,537,091.38 traded over the last 24 hours. More information can be found at https://www.ethereum.org/.',
        '2024-02-09 17:06:48.874786', '2024-02-09 17:06:48.874786');
INSERT INTO public.currencies (id, type, name, code, logo_url, decimals, description, tech_created_at, tech_updated_at)
VALUES ('e083f069-d6e3-4516-be0c-f89b1a8098f9', 'CRYPTO', 'USD Coin', 'USDC', 'https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png', 6,
        'USDC (USDC) is a cryptocurrency and operates on the Ethereum platform. USDC has a current supply of 27,762,630,610.068386. The last known price of USDC is 1.00008721 USD and is up 0.01 over the last 24 hours. It is currently trading on 16640 active market(s) with $7,536,291,666.48 traded over the last 24 hours. More information can be found at https://www.centre.io/usdc.',
        '2024-02-09 17:09:37.090863', '2024-02-09 17:09:37.090863');

