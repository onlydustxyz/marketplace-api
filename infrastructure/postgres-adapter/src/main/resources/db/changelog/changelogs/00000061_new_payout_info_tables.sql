create type accounting.wallet_type as enum ('address', 'name');

create type accounting.network as enum ('ethereum', 'aptos', 'starknet', 'optimism', 'sepa', 'swift');

create table accounting.wallets
(
    billing_profile_id uuid                    not null,
    network            accounting.network      not null,
    type               accounting.wallet_type  not null,
    address            text                    not null,
    tech_created_at    timestamp default now() not null,
    tech_updated_at    timestamp default now() not null,
    primary key (billing_profile_id, network)
);

create table accounting.bank_accounts
(
    billing_profile_id uuid                    not null
        primary key,
    bic                text                    not null,
    number             text                    not null,
    tech_created_at    timestamp default now() not null,
    tech_updated_at    timestamp default now() not null
);

create table accounting.payout_infos
(
    billing_profile_id uuid                    not null
        primary key,
    tech_created_at    timestamp default now() not null,
    tech_updated_at    timestamp default now() not null
)
