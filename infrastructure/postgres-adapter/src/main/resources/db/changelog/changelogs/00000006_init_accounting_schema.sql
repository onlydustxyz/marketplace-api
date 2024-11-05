create schema if not exists accounting;

create type accounting.invoice_status as enum ('DRAFT', 'TO_REVIEW', 'REJECTED', 'APPROVED', 'PAID');

create type accounting.batch_payment_status as enum ('TO_PAY', 'PAID');

create type accounting.billing_profile_role as enum ('ADMIN', 'MEMBER');

create type accounting.billing_profile_type as enum ('INDIVIDUAL', 'COMPANY', 'SELF_EMPLOYED');

create type accounting.id_document_type as enum ('PASSPORT', 'ID_CARD', 'RESIDENCE_PERMIT', 'DRIVER_LICENSE');

create type accounting.verification_status as enum ('NOT_STARTED', 'STARTED', 'UNDER_REVIEW', 'VERIFIED', 'REJECTED', 'CLOSED');

create type accounting.wallet_type as enum ('address', 'name');

create type accounting.outbox_event_status as enum ('PENDING', 'PROCESSED', 'FAILED', 'SKIPPED');

create type accounting.transaction_type as enum ('DEPOSIT', 'SPEND', 'MINT', 'BURN', 'TRANSFER', 'REFUND', 'WITHDRAW');

create type accounting.network as enum ('ETHEREUM', 'APTOS', 'STARKNET', 'OPTIMISM', 'SEPA', 'STELLAR', 'NEAR');

create type accounting.reward_status as enum ('PENDING_SIGNUP', 'PENDING_BILLING_PROFILE', 'PENDING_VERIFICATION', 'GEO_BLOCKED', 'INDIVIDUAL_LIMIT_REACHED', 'PAYOUT_INFO_MISSING', 'LOCKED', 'PENDING_REQUEST', 'PROCESSING', 'COMPLETE');

create type accounting.deposit_status as enum ('DRAFT', 'PENDING', 'COMPLETED', 'REJECTED');

create table if not exists accounting.account_books
(
    id              uuid                    not null
        primary key,
    currency_id     uuid                    not null
        unique
        references public.currencies,
    tech_created_at timestamp default now() not null,
    tech_updated_at timestamp default now() not null
);

create trigger account_books_events_set_tech_updated_at
    before update
    on accounting.account_books
    for each row
execute procedure public.set_tech_updated_at();

create table if not exists accounting.account_books_events
(
    id              bigint                  not null,
    account_book_id uuid                    not null
        references accounting.account_books,
    payload         jsonb                   not null,
    tech_created_at timestamp default now() not null,
    tech_updated_at timestamp default now() not null,
    timestamp       timestamp               not null,
    primary key (account_book_id, id)
);

create index if not exists account_books_events_tech_created_at_idx
    on accounting.account_books_events (account_book_id, tech_created_at);

create index if not exists account_books_events_timestamp_idx
    on accounting.account_books_events (account_book_id, timestamp, id);

create trigger account_books_events_set_tech_updated_at
    before update
    on accounting.account_books_events
    for each row
execute procedure public.set_tech_updated_at();

create table if not exists accounting.batch_payments
(
    id               uuid                                                                              not null
        primary key,
    csv              text                                                                              not null,
    network          accounting.network                                                                not null,
    status           accounting.batch_payment_status default 'TO_PAY'::accounting.batch_payment_status not null,
    transaction_hash text,
    tech_created_at  timestamp                       default CURRENT_TIMESTAMP,
    tech_updated_at  timestamp                       default CURRENT_TIMESTAMP
);

create index if not exists batch_payments_status_index
    on accounting.batch_payments (status);

create table if not exists accounting.billing_profiles
(
    id                          uuid                                                                                 not null
        primary key,
    name                        text                                                                                 not null,
    type                        accounting.billing_profile_type                                                      not null,
    invoice_mandate_accepted_at timestamp,
    tech_created_at             timestamp                      default now()                                         not null,
    tech_updated_at             timestamp                      default now()                                         not null,
    verification_status         accounting.verification_status default 'NOT_STARTED'::accounting.verification_status not null,
    enabled                     boolean                        default true                                          not null
);

create table if not exists accounting.bank_accounts
(
    billing_profile_id uuid                    not null
        primary key
        constraint bank_accounts_billing_profile_id_fk
            references accounting.billing_profiles,
    bic                text                    not null,
    number             text                    not null,
    tech_created_at    timestamp default now() not null,
    tech_updated_at    timestamp default now() not null
);

create unique index if not exists billing_profiles_id_type_verification_status_idx
    on accounting.billing_profiles (id, type, verification_status);

create table if not exists accounting.billing_profiles_user_invitations
(
    billing_profile_id uuid                            not null
        references accounting.billing_profiles,
    github_user_id     bigint                          not null,
    role               accounting.billing_profile_role not null,
    invited_at         timestamp                       not null,
    invited_by         uuid                            not null
        references iam.users,
    tech_created_at    timestamp default now()         not null,
    tech_updated_at    timestamp default now()         not null,
    accepted           boolean   default false,
    primary key (billing_profile_id, github_user_id)
);

create unique index if not exists billing_profiles_user_invitations_user_id_bp_id_uindex
    on accounting.billing_profiles_user_invitations (github_user_id, billing_profile_id);

create trigger accounting_bp_user_invitations_set_tech_updated_at
    before update
    on accounting.billing_profiles_user_invitations
    for each row
execute procedure public.set_tech_updated_at();

create table if not exists accounting.billing_profiles_users
(
    billing_profile_id uuid                            not null
        constraint billing_profiles_users_billing_profile_id_fk
            references accounting.billing_profiles,
    user_id            uuid                            not null,
    role               accounting.billing_profile_role not null,
    tech_created_at    timestamp default now()         not null,
    tech_updated_at    timestamp default now()         not null,
    joined_at          timestamp,
    constraint billing_profile_id_user_id
        primary key (billing_profile_id, user_id)
);

create unique index if not exists billing_profiles_users_user_id_billing_profile_id_uindex
    on accounting.billing_profiles_users (user_id, billing_profile_id);

create unique index if not exists billing_profiles_users_user_id_billing_profile_id_role_idx
    on accounting.billing_profiles_users (user_id, billing_profile_id, role);

create table if not exists accounting.historical_quotes
(
    timestamp timestamp not null,
    base_id   uuid      not null
        constraint historical_quotes_currency_id_fkey
            references public.currencies,
    target_id uuid      not null
        constraint historical_quotes_base_id_fkey
            references public.currencies,
    price     numeric   not null,
    primary key (timestamp, base_id, target_id)
);

create index if not exists historical_quotes_currency_idx
    on accounting.historical_quotes (timestamp desc, base_id asc, target_id asc, price asc);

create unique index if not exists historical_quotes_target_id_base_id_timestamp_uindex
    on accounting.historical_quotes (target_id, base_id, timestamp);

create unique index if not exists historical_quotes_target_id_base_id_timestamp_uindex_desc
    on accounting.historical_quotes (target_id asc, base_id asc, timestamp desc);

create unique index if not exists historical_quotes_base_id_target_id_timestamp_uindex
    on accounting.historical_quotes (base_id, target_id, timestamp);

create unique index if not exists historical_quotes_base_id_target_id_timestamp_uindex_desc
    on accounting.historical_quotes (base_id asc, target_id asc, timestamp desc);

create table if not exists accounting.invoices
(
    id                 uuid                      not null
        primary key,
    billing_profile_id uuid                      not null,
    number             text                      not null,
    created_at         timestamp                 not null,
    status             accounting.invoice_status not null,
    amount             numeric                   not null,
    currency_id        uuid                      not null
        references public.currencies,
    url                text,
    data               jsonb                     not null,
    tech_created_at    timestamp default now()   not null,
    tech_updated_at    timestamp default now()   not null,
    original_file_name text,
    rejection_reason   text,
    created_by         uuid                      not null,
    unique (billing_profile_id, number)
);

create trigger accounting_invoices_set_tech_updated_at
    before update
    on accounting.invoices
    for each row
execute procedure public.set_tech_updated_at();

create table if not exists accounting.kyb
(
    id                  uuid                           not null
        primary key,
    owner_id            uuid                           not null,
    billing_profile_id  uuid                           not null
        constraint kyb_billing_profile_id_fk
            references accounting.billing_profiles,
    verification_status accounting.verification_status not null,
    name                text,
    registration_number text,
    registration_date   timestamp,
    address             text,
    country             text,
    us_entity           boolean,
    subject_to_eu_vat   boolean,
    eu_vat_number       text,
    review_message      text,
    applicant_id        text,
    tech_created_at     timestamp default now()        not null,
    tech_updated_at     timestamp default now()        not null
);

create index if not exists kyb_billing_profile_id_index
    on accounting.kyb (billing_profile_id);

create unique index if not exists kyb_billing_profile_id_us_entity_country_idx
    on accounting.kyb (billing_profile_id, us_entity, country);

create table if not exists accounting.kyc
(
    id                                 uuid                           not null
        primary key,
    owner_id                           uuid                           not null,
    billing_profile_id                 uuid                           not null
        constraint kyc_billing_profile_id_fk
            references accounting.billing_profiles,
    verification_status                accounting.verification_status not null,
    first_name                         text,
    last_name                          text,
    address                            text,
    country                            text,
    birthdate                          timestamp,
    valid_until                        timestamp,
    id_document_number                 text,
    id_document_type                   accounting.id_document_type,
    id_document_country_code           text,
    considered_us_person_questionnaire boolean,
    review_message                     text,
    applicant_id                       text,
    tech_created_at                    timestamp default now()        not null,
    tech_updated_at                    timestamp default now()        not null,
    us_citizen                         boolean
);

create index if not exists kyc_billing_profile_id_index
    on accounting.kyc (billing_profile_id);

create unique index if not exists kyc_billing_profile_id_considered_us_person_questionnaire_c_idx
    on accounting.kyc (billing_profile_id, considered_us_person_questionnaire, country, id_document_country_code);

create table if not exists accounting.payout_infos
(
    billing_profile_id uuid                    not null
        primary key,
    tech_created_at    timestamp default now() not null,
    tech_updated_at    timestamp default now() not null
);

create table if not exists accounting.payout_preferences
(
    billing_profile_id uuid                    not null
        constraint payout_preferences_billing_profile_id_fk
            references accounting.billing_profiles,
    project_id         uuid                    not null,
    user_id            uuid                    not null,
    tech_created_at    timestamp default now() not null,
    tech_updated_at    timestamp default now() not null,
    primary key (project_id, user_id)
);

create unique index if not exists payout_preferences_user_id_project_id_billing_profile_id_uindex
    on accounting.payout_preferences (user_id, project_id, billing_profile_id);

create unique index if not exists payout_preferences_project_id_user_id_billing_profile_id_uindex
    on accounting.payout_preferences (project_id, user_id, billing_profile_id);

create table if not exists public.rewards
(
    id                  uuid                    not null
        primary key,
    project_id          uuid                    not null
        references projects,
    requestor_id        uuid                    not null
        references iam.users,
    recipient_id        bigint                  not null,
    amount              numeric                 not null,
    requested_at        timestamp               not null,
    tech_created_at     timestamp default now() not null,
    tech_updated_at     timestamp default now() not null,
    invoice_id          uuid
        references accounting.invoices,
    currency_id         uuid                    not null
        references currencies,
    payment_notified_at timestamp,
    billing_profile_id  uuid
        references accounting.billing_profiles
);

create table if not exists accounting.reward_status_data
(
    reward_id               uuid                    not null
        constraint reward_statuses_pkey
            primary key
        references public.rewards
            deferrable initially deferred,
    sponsor_has_enough_fund boolean,
    unlock_date             timestamp,
    invoice_received_at     timestamp,
    paid_at                 timestamp,
    tech_created_at         timestamp default now() not null,
    tech_updated_at         timestamp default now() not null,
    amount_usd_equivalent   numeric,
    networks                accounting.network[]    not null,
    usd_conversion_rate     numeric
);

create unique index if not exists reward_status_data_reward_id_invoice_received_at_paid_at_am_idx
    on accounting.reward_status_data (reward_id, invoice_received_at, paid_at, amount_usd_equivalent);

create trigger accounting_reward_status_data_set_tech_updated_at
    before update
    on accounting.reward_status_data
    for each row
execute procedure public.set_tech_updated_at();

create table if not exists accounting.sponsor_accounts
(
    id              uuid                    not null
        primary key,
    currency_id     uuid                    not null
        references public.currencies,
    sponsor_id      uuid                    not null
        references public.sponsors,
    locked_until    timestamp,
    tech_created_at timestamp default now() not null,
    tech_updated_at timestamp default now() not null
);

create index if not exists sponsor_accounts_sponsor_id_index
    on accounting.sponsor_accounts (sponsor_id);

create trigger sponsor_accounts_set_tech_updated_at
    before update
    on accounting.sponsor_accounts
    for each row
execute procedure public.set_tech_updated_at();

create table if not exists accounting.wallets
(
    billing_profile_id uuid                    not null
        constraint wallets_billing_profile_id_fk
            references accounting.billing_profiles,
    network            accounting.network      not null,
    type               accounting.wallet_type  not null,
    address            text                    not null,
    tech_created_at    timestamp default now() not null,
    tech_updated_at    timestamp default now() not null,
    primary key (billing_profile_id, network)
);

create table if not exists accounting.billing_profile_verification_outbox_events
(
    id              bigserial
        primary key,
    payload         jsonb                                                                            not null,
    status          accounting.outbox_event_status default 'PENDING'::accounting.outbox_event_status not null,
    error           text,
    tech_created_at timestamp                      default now()                                     not null,
    tech_updated_at timestamp                      default now()                                     not null
);

create trigger accounting_bbp_verification_outbox_events_set_tech_updated_at
    before update
    on accounting.billing_profile_verification_outbox_events
    for each row
execute procedure public.set_tech_updated_at();

create table if not exists accounting.children_kyc
(
    applicant_id        text                           not null
        primary key,
    parent_applicant_id text                           not null,
    verification_status accounting.verification_status not null,
    tech_created_at     timestamp default now()        not null,
    tech_updated_at     timestamp default now()        not null
);

create index if not exists children_kyc_parent_applicant_id_index
    on accounting.children_kyc (parent_applicant_id);

create table if not exists accounting.receipts
(
    id                         uuid                    not null
        primary key,
    created_at                 timestamp               not null,
    network                    accounting.network      not null,
    third_party_name           text                    not null,
    third_party_account_number text                    not null,
    transaction_reference      text                    not null,
    tech_created_at            timestamp default now() not null,
    tech_updated_at            timestamp default now() not null
);

create index if not exists receipts_transaction_reference_index
    on accounting.receipts (transaction_reference);

create trigger accounting_receipts_set_tech_updated_at
    before update
    on accounting.receipts
    for each row
execute procedure public.set_tech_updated_at();

create table if not exists accounting.rewards_receipts
(
    reward_id  uuid not null
        references public.rewards,
    receipt_id uuid not null
        references accounting.receipts,
    primary key (reward_id, receipt_id)
);

create table if not exists accounting.batch_payment_rewards
(
    batch_payment_id uuid                    not null
        references accounting.batch_payments,
    reward_id        uuid                    not null
        references public.rewards,
    amount           numeric                 not null,
    tech_created_at  timestamp default now() not null,
    tech_updated_at  timestamp default now() not null,
    primary key (batch_payment_id, reward_id)
);

create unique index if not exists batch_payment_rewards_reward_id_batch_payment_id_uindex
    on accounting.batch_payment_rewards (reward_id, batch_payment_id);

create trigger batch_payment_rewards_set_tech_updated_at
    before update
    on accounting.batch_payment_rewards
    for each row
execute procedure public.set_tech_updated_at();

create table if not exists accounting.latest_quotes
(
    timestamp       timestamp               not null,
    base_id         uuid                    not null
        references public.currencies,
    target_id       uuid                    not null
        references public.currencies,
    price           numeric                 not null,
    tech_created_at timestamp default now() not null,
    tech_updated_at timestamp default now() not null,
    primary key (base_id, target_id)
);

create unique index if not exists latest_quotes_target_id_base_id_uindex
    on accounting.latest_quotes (target_id, base_id);

create trigger update_latest_quotes_tech_updated_at
    before update
    on accounting.latest_quotes
    for each row
execute procedure public.set_tech_updated_at();

create table if not exists accounting.oldest_quotes
(
    timestamp       timestamp               not null,
    base_id         uuid                    not null
        references public.currencies,
    target_id       uuid                    not null
        references public.currencies,
    price           numeric                 not null,
    tech_created_at timestamp default now() not null,
    tech_updated_at timestamp default now() not null,
    primary key (base_id, target_id)
);

create unique index if not exists oldest_quotes_target_id_base_id_uindex
    on accounting.oldest_quotes (target_id, base_id);

create table if not exists accounting.mail_outbox_events
(
    id              bigserial
        primary key,
    payload         jsonb                                                      not null,
    status          outbox_event_status default 'PENDING'::outbox_event_status not null,
    error           text,
    tech_created_at timestamp           default now()                          not null,
    tech_updated_at timestamp           default now()                          not null
);

create trigger accounting_mail_outbox_events_set_tech_updated_at
    before update
    on accounting.mail_outbox_events
    for each row
execute procedure public.set_tech_updated_at();

create table if not exists accounting.country_individual_payment_limits
(
    country_code                text    not null
        primary key,
    usd_yearly_individual_limit numeric not null
);

create unique index if not exists country_individual_payment_li_country_code_usd_yearly_indiv_idx
    on accounting.country_individual_payment_limits (country_code, usd_yearly_individual_limit);

create table if not exists accounting.sumsub_rejection_reasons
(
    id                         uuid default gen_random_uuid() not null
        primary key,
    button                     text                           not null,
    group_id                   text,
    button_id                  text                           not null,
    associated_rejection_label text                           not null,
    description                text                           not null,
    constraint sumsub_rejection_reasons_group_id_button_id_associated_reje_key
        unique (group_id, button_id, associated_rejection_label)
);

create table if not exists accounting.all_transactions
(
    id             uuid                        not null
        constraint account_book_transactions_pkey
            primary key,
    timestamp      timestamp with time zone    not null,
    currency_id    uuid                        not null
        constraint account_book_transactions_currency_id_fkey
            references public.currencies,
    type           accounting.transaction_type not null,
    sponsor_id     uuid,
    program_id     uuid,
    project_id     uuid,
    reward_id      uuid,
    payment_id     uuid,
    amount         numeric                     not null,
    deposit_status accounting.deposit_status
);

create index if not exists account_book_transactions_currency_sponsor
    on accounting.all_transactions (currency_id, sponsor_id);

create index if not exists account_book_transactions_currency_program
    on accounting.all_transactions (currency_id, program_id);

create index if not exists account_book_transactions_currency_project
    on accounting.all_transactions (currency_id, project_id);

create index if not exists account_book_transactions_currency_reward
    on accounting.all_transactions (currency_id, reward_id, amount);

create index if not exists account_book_transactions_currency_payment
    on accounting.all_transactions (currency_id, payment_id);

create index if not exists account_book_transactions_currency_sponsor_program_type_amount
    on accounting.all_transactions (currency_id, sponsor_id, program_id, type, amount);

create index if not exists account_book_transactions_currency_program_project_type_amount
    on accounting.all_transactions (currency_id, program_id, project_id, type, amount);

create index if not exists account_book_transactions_currency_project_reward_type_amount
    on accounting.all_transactions (currency_id, project_id, reward_id, type, amount);

create index if not exists account_book_transactions_currency_reward_type_amount
    on accounting.all_transactions (currency_id, reward_id, type, amount);

create index if not exists all_transactions_program_id_project_id_idx
    on accounting.all_transactions (program_id, project_id);

create table if not exists accounting.transfer_transactions
(
    id                uuid                                   not null
        primary key,
    blockchain        accounting.network                     not null,
    reference         text                                   not null,
    index             serial,
    timestamp         timestamp with time zone               not null,
    sender_address    text                                   not null,
    recipient_address text                                   not null,
    amount            numeric                                not null,
    contract_address  text,
    tech_created_at   timestamp with time zone default now() not null,
    tech_updated_at   timestamp with time zone default now() not null,
    unique (blockchain, reference, index)
);

create table if not exists accounting.sponsor_account_transactions
(
    account_id                 uuid                        not null,
    network                    accounting.network          not null,
    reference                  text                        not null,
    amount                     numeric                     not null,
    third_party_name           text                        not null,
    third_party_account_number text                        not null,
    tech_created_at            timestamp default now()     not null,
    tech_updated_at            timestamp default now()     not null,
    id                         uuid                        not null
        primary key,
    type                       accounting.transaction_type not null,
    timestamp                  timestamp                   not null,
    transaction_id             uuid
        unique
        references accounting.transfer_transactions
);

create index if not exists sponsor_account_transactions_reference_index
    on accounting.sponsor_account_transactions (reference);

create index if not exists sponsor_account_transactions_account_id_network_index
    on accounting.sponsor_account_transactions (account_id, network);

create trigger sponsor_account_transactions_set_tech_updated_at
    before update
    on accounting.sponsor_account_transactions
    for each row
execute procedure public.set_tech_updated_at();

create trigger accounting_transfer_transactions_set_tech_updated_at
    before update
    on accounting.transfer_transactions
    for each row
execute procedure public.set_tech_updated_at();

create table if not exists accounting.deposits
(
    id                  uuid                                   not null
        primary key,
    transaction_id      uuid                                   not null
        references accounting.transfer_transactions,
    sponsor_id          uuid                                   not null
        references public.sponsors,
    currency_id         uuid                                   not null
        references public.currencies,
    status              accounting.deposit_status              not null,
    billing_information jsonb,
    tech_created_at     timestamp with time zone default now() not null,
    tech_updated_at     timestamp with time zone default now() not null
);

create trigger accounting_deposits_set_tech_updated_at
    before update
    on accounting.deposits
    for each row
execute procedure public.set_tech_updated_at();

create or replace view accounting.oldest_usd_quotes(currency_id, price, timestamp) as
SELECT q.base_id AS currency_id,
       q.price,
       q."timestamp"
FROM accounting.oldest_quotes q
         JOIN currencies usd ON usd.id = q.target_id AND usd.code = 'USD'::text;

create or replace view accounting.reward_usd_equivalent_data
            (reward_id, reward_created_at, reward_currency_id, kycb_verified_at, currency_quote_available_at,
             unlock_date, reward_amount)
as
SELECT r.id               AS reward_id,
       r.requested_at     AS reward_created_at,
       r.currency_id      AS reward_currency_id,
       bp.tech_updated_at AS kycb_verified_at,
       ouq."timestamp"    AS currency_quote_available_at,
       rs.unlock_date,
       r.amount           AS reward_amount
FROM accounting.reward_status_data rs
         JOIN public.rewards r ON r.id = rs.reward_id
         LEFT JOIN iam.users u ON u.github_user_id = r.recipient_id
         LEFT JOIN accounting.payout_preferences pp ON pp.project_id = r.project_id AND pp.user_id = u.id
         LEFT JOIN accounting.billing_profiles bp ON bp.id = COALESCE(r.billing_profile_id, pp.billing_profile_id) AND
                                                     bp.verification_status = 'VERIFIED'::accounting.verification_status
         LEFT JOIN accounting.oldest_usd_quotes ouq ON ouq.currency_id = r.currency_id;

create or replace view accounting.latest_usd_quotes(currency_id, price, timestamp) as
SELECT q.base_id AS currency_id,
       q.price,
       q."timestamp"
FROM accounting.latest_quotes q
         JOIN currencies usd ON usd.id = q.target_id AND usd.code = 'USD'::text;

create or replace function accounting.billing_profile_current_year_usd_amount(billingprofileid uuid) returns numeric
    stable
    parallel safe
    language sql
as
$$
SELECT sum((select rsd.amount_usd_equivalent
            from accounting.reward_status_data rsd
            where rsd.reward_id = r.id
              AND (rsd.paid_at >= date_trunc('year'::text, now()) AND rsd.paid_at <= (date_trunc('year'::text, now()) + '1 year'::interval) OR
                   rsd.invoice_received_at IS NOT NULL AND rsd.paid_at IS NULL))) AS yearly_usd_total
FROM rewards r
where r.billing_profile_id = billingProfileId
group by r.billing_profile_id
$$;

create or replace view accounting.reward_statuses
            (reward_id, status, project_id, requestor_id, recipient_id, amount, requested_at, invoice_id, currency_id,
             payment_notified_at, billing_profile_id, sponsor_has_enough_fund, unlock_date, invoice_received_at,
             paid_at, amount_usd_equivalent, networks, usd_conversion_rate, recipient_user_id, recipient_is_registered,
             recipient_bp_is_individual, recipient_bp_current_year_usd_total, recipient_bp_usd_yearly_individual_limit,
             recipient_kycb_verified, recipient_kycb_countries, currency_country_restrictions,
             recipient_payout_info_filled)
as
WITH aggregated_reward_status_data AS (SELECT r.id,
                                              r.project_id,
                                              r.requestor_id,
                                              r.recipient_id,
                                              r.amount,
                                              r.requested_at,
                                              r.tech_created_at,
                                              r.tech_updated_at,
                                              r.invoice_id,
                                              r.currency_id,
                                              r.payment_notified_at,
                                              r.billing_profile_id,
                                              rs.reward_id,
                                              rs.sponsor_has_enough_fund,
                                              rs.unlock_date,
                                              rs.invoice_received_at,
                                              rs.paid_at,
                                              rs.tech_created_at,
                                              rs.tech_updated_at,
                                              rs.amount_usd_equivalent,
                                              rs.networks,
                                              rs.usd_conversion_rate,
                                              u.id                                                                 AS recipient_user_id,
                                              u.id IS NOT NULL                                                     AS is_registered,
                                              bp.type = 'INDIVIDUAL'::accounting.billing_profile_type              AS is_individual,
                                              bp.verification_status = 'VERIFIED'::accounting.verification_status  AS kycb_verified,
                                              c.country_restrictions,
                                              ARRAY [
                                                  CASE
                                                      WHEN kyc.considered_us_person_questionnaire OR kyb.us_entity
                                                          THEN 'USA'::text
                                                      ELSE NULL::text
                                                      END, kyc.country, kyc.id_document_country_code, kyb.country] AS kycb_countries,
                                              CASE
                                                  WHEN bp.type = 'INDIVIDUAL'::accounting.billing_profile_type
                                                      THEN COALESCE(
                                                          accounting.billing_profile_current_year_usd_amount(bp.id),
                                                          0::numeric)
                                                  ELSE NULL::numeric
                                                  END                                                              AS current_year_usd_total,
                                              (((SELECT array_agg(w.network) AS array_agg
                                                 FROM accounting.wallets w
                                                 WHERE w.billing_profile_id = bp.id)) ||
                                               ((SELECT '{SEPA}'::accounting.network[] AS network
                                                 FROM accounting.bank_accounts ba
                                                 WHERE ba.billing_profile_id = bp.id))) @>
                                              rs.networks                                                          AS payout_info_filled,
                                              COALESCE(cipl.usd_yearly_individual_limit, 5001::numeric)            AS usd_yearly_individual_limit
                                       FROM accounting.reward_status_data rs
                                                JOIN rewards r ON r.id = rs.reward_id
                                                JOIN currencies c ON c.id = r.currency_id
                                                LEFT JOIN iam.users u ON u.github_user_id = r.recipient_id
                                                LEFT JOIN accounting.payout_preferences pp
                                                          ON pp.project_id = r.project_id AND pp.user_id = u.id
                                                LEFT JOIN accounting.billing_profiles bp
                                                          ON bp.id = COALESCE(r.billing_profile_id, pp.billing_profile_id)
                                                LEFT JOIN accounting.kyc kyc ON kyc.billing_profile_id = bp.id
                                                LEFT JOIN accounting.kyb kyb ON kyb.billing_profile_id = bp.id
                                                LEFT JOIN accounting.country_individual_payment_limits cipl
                                                          ON cipl.country_code = kyc.country)
SELECT s.reward_id,
       CASE
           WHEN s.paid_at IS NOT NULL THEN 'COMPLETE'::accounting.reward_status
           WHEN s.invoice_received_at IS NOT NULL THEN 'PROCESSING'::accounting.reward_status
           WHEN NOT s.is_registered THEN 'PENDING_SIGNUP'::accounting.reward_status
           WHEN s.is_individual IS NULL THEN 'PENDING_BILLING_PROFILE'::accounting.reward_status
           WHEN s.kycb_verified IS NOT TRUE THEN 'PENDING_VERIFICATION'::accounting.reward_status
           WHEN s.kycb_countries && s.country_restrictions THEN 'GEO_BLOCKED'::accounting.reward_status
           WHEN s.is_individual IS TRUE AND
                (s.current_year_usd_total + s.amount_usd_equivalent) >= s.usd_yearly_individual_limit
               THEN 'INDIVIDUAL_LIMIT_REACHED'::accounting.reward_status
           WHEN s.payout_info_filled IS NOT TRUE THEN 'PAYOUT_INFO_MISSING'::accounting.reward_status
           WHEN s.unlock_date IS NOT NULL AND s.unlock_date > now() THEN 'LOCKED'::accounting.reward_status
           WHEN s.sponsor_has_enough_fund IS NOT TRUE THEN 'LOCKED'::accounting.reward_status
           ELSE 'PENDING_REQUEST'::accounting.reward_status
           END                       AS status,
       s.project_id,
       s.requestor_id,
       s.recipient_id,
       s.amount,
       s.requested_at,
       s.invoice_id,
       s.currency_id,
       s.payment_notified_at,
       s.billing_profile_id,
       s.sponsor_has_enough_fund,
       s.unlock_date,
       s.invoice_received_at,
       s.paid_at,
       s.amount_usd_equivalent,
       s.networks,
       s.usd_conversion_rate,
       s.recipient_user_id,
       s.is_registered               AS recipient_is_registered,
       s.is_individual               AS recipient_bp_is_individual,
       s.current_year_usd_total      AS recipient_bp_current_year_usd_total,
       s.usd_yearly_individual_limit AS recipient_bp_usd_yearly_individual_limit,
       s.kycb_verified               AS recipient_kycb_verified,
       s.kycb_countries              AS recipient_kycb_countries,
       s.country_restrictions        AS currency_country_restrictions,
       s.payout_info_filled          AS recipient_payout_info_filled
FROM aggregated_reward_status_data s(id, project_id, requestor_id, recipient_id, amount, requested_at, tech_created_at,
                                     tech_updated_at, invoice_id, currency_id, payment_notified_at, billing_profile_id,
                                     reward_id, sponsor_has_enough_fund, unlock_date, invoice_received_at, paid_at,
                                     tech_created_at_1, tech_updated_at_1, amount_usd_equivalent, networks,
                                     usd_conversion_rate, recipient_user_id, is_registered, is_individual,
                                     kycb_verified, country_restrictions, kycb_countries, current_year_usd_total,
                                     payout_info_filled, usd_yearly_individual_limit);

create or replace view accounting.billing_profile_stats
            (billing_profile_id, reward_count, invoiceable_reward_count, missing_payout_info, missing_verification,
             individual_limit_reached, current_year_payment_amount, mandate_acceptance_outdated,
             current_year_payment_limit)
as
SELECT bp.id                                                                                                 AS billing_profile_id,
       count(r.reward_id)                                                                                    AS reward_count,
       count(r.reward_id)
       FILTER (WHERE r.status = 'PENDING_REQUEST'::accounting.reward_status)                                 AS invoiceable_reward_count,
       count(r.reward_id) FILTER (WHERE r.status = 'PAYOUT_INFO_MISSING'::accounting.reward_status) >
       0                                                                                                     AS missing_payout_info,
       count(r.reward_id) FILTER (WHERE r.status = 'PENDING_VERIFICATION'::accounting.reward_status) >
       0                                                                                                     AS missing_verification,
       count(r.reward_id) FILTER (WHERE r.status = 'INDIVIDUAL_LIMIT_REACHED'::accounting.reward_status) >
       0                                                                                                     AS individual_limit_reached,
       COALESCE(max(r.recipient_bp_current_year_usd_total), 0::numeric)                                      AS current_year_payment_amount,
       bp.type <> 'INDIVIDUAL'::accounting.billing_profile_type AND (bp.invoice_mandate_accepted_at IS NULL OR
                                                                     bp.invoice_mandate_accepted_at <
                                                                     gs.invoice_mandate_latest_version_date) AS mandate_acceptance_outdated,
       CASE
           WHEN kyc.country IS NOT NULL THEN COALESCE(cipl.usd_yearly_individual_limit, 5001::numeric)
           ELSE NULL::numeric
           END                                                                                               AS current_year_payment_limit
FROM accounting.billing_profiles bp
         LEFT JOIN accounting.reward_statuses r ON r.billing_profile_id = bp.id
         LEFT JOIN accounting.kyc kyc ON kyc.billing_profile_id = bp.id
         LEFT JOIN accounting.country_individual_payment_limits cipl ON cipl.country_code = kyc.country
         CROSS JOIN global_settings gs
GROUP BY bp.id, gs.invoice_mandate_latest_version_date, kyc.country, cipl.usd_yearly_individual_limit;

create or replace view accounting.all_billing_profile_users
            (github_user_id, billing_profile_id, role, user_id, invitation_accepted) as
SELECT u.github_user_id,
       bpu.billing_profile_id,
       bpu.role,
       u.id AS user_id,
       true AS invitation_accepted
FROM accounting.billing_profiles_users bpu
         JOIN iam.users u ON u.id = bpu.user_id
UNION
SELECT bpui.github_user_id,
       bpui.billing_profile_id,
       bpui.role,
       u.id  AS user_id,
       false AS invitation_accepted
FROM accounting.billing_profiles_user_invitations bpui
         LEFT JOIN iam.users u ON u.github_user_id = bpui.github_user_id
WHERE bpui.accepted IS FALSE;

create or replace function accounting.usd_quote_at(currency_id uuid, at timestamp with time zone) returns numeric
    stable
    parallel safe
    language sql
as
$$
SELECT price
FROM accounting.historical_quotes hq
         JOIN currencies usd ON usd.id = hq.target_id and usd.code = 'USD'
WHERE hq.base_id = currency_id
  AND hq.timestamp <= at
ORDER BY hq.timestamp DESC
LIMIT 1
$$;

create or replace function accounting.usd_equivalent_at(amount numeric, currency_id uuid, at timestamp with time zone) returns numeric
    stable
    parallel safe
    language sql
as
$$
SELECT amount * accounting.usd_quote_at(currency_id, at);
$$;


create or replace function accounting.insert_account_books_event(_id bigint, _account_book_id uuid, _payload jsonb, _timestamp timestamp without time zone) returns void
    language plpgsql
as
$$
BEGIN
    alter table accounting.account_books_events
        drop constraint account_books_events_pkey;
    update accounting.account_books_events set id = id + 1 where account_book_id = _account_book_id and id >= _id;
    alter table accounting.account_books_events
        add primary key (account_book_id, id);
    insert into accounting.account_books_events (id, account_book_id, payload, timestamp) values (_id, _account_book_id, _payload, _timestamp);
END
$$;

create or replace function accounting.delete_account_books_event(_id bigint, _account_book_id uuid) returns void
    language plpgsql
as
$$
BEGIN
    delete from accounting.account_books_events where account_book_id = _account_book_id and id = _id;

    alter table accounting.account_books_events
        drop constraint account_books_events_pkey;
    update accounting.account_books_events set id = id - 1 where account_book_id = _account_book_id and id > _id;
    alter table accounting.account_books_events
        add primary key (account_book_id, id);
END
$$;

create or replace function accounting.update_invoice_status() returns trigger
    language plpgsql
as
$$
BEGIN
    UPDATE accounting.invoices
    SET status = 'PAID'
    WHERE id = (select pr.invoice_id from payment_requests pr where pr.id = NEW.request_id)
      AND NOT EXISTS (select 1
                      from payment_requests pr
                               left join payments p on pr.id = p.request_id
                      where pr.invoice_id = accounting.invoices.id
                        and p is null);

    RETURN NEW;
END;
$$;