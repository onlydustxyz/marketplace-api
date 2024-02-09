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
values ('81b7e948-954f-4718-bad3-b70a0edd27e1', 'StarkNet Token', 'STRK', 18, 'CRYPTO', 'ERC20');

insert into erc20(blockchain, address, name, symbol, decimals, total_supply, currency_id)
values ('ethereum', '0xCa14007Eff0dB1f8135f4C25B34De49AB0d42766', 'StarkNet Token', 'STRK', 18, 10000000000, '81b7e948-954f-4718-bad3-b70a0edd27e1');