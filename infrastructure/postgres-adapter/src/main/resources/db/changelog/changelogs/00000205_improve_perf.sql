-- Latest quotes

create table accounting.latest_quotes
(
    timestamp       timestamp               not null,
    base_id         uuid                    not null references public.currencies,
    target_id       uuid                    not null references public.currencies,
    price           numeric                 not null,
    tech_created_at timestamp default now() not null,
    tech_updated_at timestamp default now() not null,
    primary key (base_id, target_id)
);

create unique index latest_quotes_target_id_base_id_uindex
    on accounting.latest_quotes (target_id, base_id);

CREATE TRIGGER update_latest_quotes_tech_updated_at
    BEFORE UPDATE
    ON accounting.latest_quotes
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

insert into accounting.latest_quotes (timestamp, base_id, target_id, price)
select q.timestamp, q.base_id, q.target_id, q.price
from accounting.historical_quotes q
         join (select distinct base_id, target_id, max(timestamp) OVER (PARTITION BY base_id, target_id) as timestamp
               from accounting.historical_quotes) latest ON latest.base_id = q.base_id AND latest.target_id = q.target_id AND latest.timestamp = q.timestamp;


-- Oldest quotes

create table accounting.oldest_quotes
(
    timestamp       timestamp               not null,
    base_id         uuid                    not null references public.currencies,
    target_id       uuid                    not null references public.currencies,
    price           numeric                 not null,
    tech_created_at timestamp default now() not null,
    tech_updated_at timestamp default now() not null,
    primary key (base_id, target_id)
);

create unique index oldest_quotes_target_id_base_id_uindex
    on accounting.oldest_quotes (target_id, base_id);

CREATE TRIGGER update_oldest_quotes_tech_updated_at
    BEFORE UPDATE
    ON accounting.oldest_quotes
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

insert into accounting.oldest_quotes (timestamp, base_id, target_id, price)
select q.timestamp, q.base_id, q.target_id, q.price
from accounting.historical_quotes q
         join (select distinct base_id, target_id, min(timestamp) OVER (PARTITION BY base_id, target_id) as timestamp
               from accounting.historical_quotes) latest ON latest.base_id = q.base_id AND latest.target_id = q.target_id AND latest.timestamp = q.timestamp;


-- Update views

CREATE OR REPLACE VIEW accounting.latest_usd_quotes AS
SELECT q.base_id   AS currency_id,
       q.price     AS price,
       q.timestamp AS timestamp
FROM accounting.latest_quotes q
         JOIN currencies usd ON usd.id = q.target_id AND usd.code = 'USD'
;


CREATE OR REPLACE VIEW accounting.oldest_usd_quotes AS
SELECT q.base_id   AS currency_id,
       q.price     AS price,
       q.timestamp AS timestamp
FROM accounting.oldest_quotes q
         JOIN currencies usd ON usd.id = q.target_id AND usd.code = 'USD'
;


CREATE OR REPLACE VIEW accounting.reward_usd_equivalent_data
            (reward_id, reward_created_at, reward_currency_id, kycb_verified_at, currency_quote_available_at,
             unlock_date, reward_amount)
AS
SELECT r.id               AS reward_id,
       r.requested_at     AS reward_created_at,
       r.currency_id      AS reward_currency_id,
       bp.tech_updated_at AS kycb_verified_at,
       ouq.timestamp      AS currency_quote_available_at,
       rs.unlock_date,
       r.amount           AS reward_amount
FROM accounting.reward_status_data rs
         JOIN rewards r ON r.id = rs.reward_id
         LEFT JOIN iam.users u ON u.github_user_id = r.recipient_id
         LEFT JOIN accounting.payout_preferences pp ON pp.project_id = r.project_id AND pp.user_id = u.id
         LEFT JOIN accounting.billing_profiles bp ON bp.id = coalesce(r.billing_profile_id, pp.billing_profile_id) AND
                                                     bp.verification_status = 'VERIFIED'::accounting.verification_status
         LEFT JOIN accounting.oldest_usd_quotes ouq ON ouq.currency_id = r.currency_id;


-- Add missing indexes

create index if not exists rewards_project_id_index
    on rewards (project_id);

create index if not exists rewards_billing_profile_id_index
    on rewards (billing_profile_id);

create index if not exists rewards_invoice_id_index
    on rewards (invoice_id);

create index if not exists rewards_recipient_id_index
    on rewards (recipient_id);

create index if not exists rewards_requestor_id_index
    on rewards (requestor_id);

create index if not exists batch_payments_status_index
    on accounting.batch_payments (status);

create unique index if not exists batch_payment_rewards_reward_id_batch_payment_id_uindex
    on accounting.batch_payment_rewards (reward_id, batch_payment_id);

create unique index if not exists batch_payment_invoices_invoice_id_batch_payment_id_uindex
    on accounting.batch_payment_invoices (invoice_id, batch_payment_id);

create unique index if not exists billing_profiles_user_invitations_user_id_bp_id_uindex
    on accounting.billing_profiles_user_invitations (github_user_id, billing_profile_id);

create unique index if not exists billing_profiles_users_user_id_billing_profile_id_uindex
    on accounting.billing_profiles_users (user_id, billing_profile_id);

create index if not exists kyb_billing_profile_id_index
    on accounting.kyb (billing_profile_id);

create index if not exists kyc_billing_profile_id_index
    on accounting.kyc (billing_profile_id);

create index if not exists children_kyc_parent_applicant_id_index
    on accounting.children_kyc (parent_applicant_id);

create unique index if not exists batch_payment_invoices_invoice_id_batch_payment_id_uindex
    on accounting.batch_payment_invoices (invoice_id, batch_payment_id);

create unique index if not exists batch_payment_rewards_reward_id_batch_payment_id_uindex
    on accounting.batch_payment_rewards (reward_id, batch_payment_id);

create index if not exists sponsor_accounts_sponsor_id_index
    on accounting.sponsor_accounts (sponsor_id);

create index if not exists sponsor_account_transactions_account_id_network_index
    on accounting.sponsor_account_transactions (account_id, network);

create index if not exists sponsor_account_transactions_reference_index
    on accounting.sponsor_account_transactions (reference);

create index if not exists receipts_transaction_reference_index
    on accounting.receipts (transaction_reference);

create unique index if not exists payout_preferences_user_id_project_id_billing_profile_id_uindex
    on accounting.payout_preferences (user_id, project_id, billing_profile_id);

create unique index if not exists payout_preferences_project_id_user_id_billing_profile_id_uindex
    on accounting.payout_preferences (project_id, user_id, billing_profile_id);

create unique index if not exists historical_quotes_target_id_base_id_timestamp_uindex
    on accounting.historical_quotes (target_id, base_id, timestamp);

create unique index if not exists historical_quotes_target_id_base_id_timestamp_uindex_desc
    on accounting.historical_quotes (target_id asc, base_id asc, timestamp desc);

create unique index if not exists historical_quotes_base_id_target_id_timestamp_uindex
    on accounting.historical_quotes (base_id, target_id, timestamp);

create unique index if not exists historical_quotes_base_id_target_id_timestamp_uindex_desc
    on accounting.historical_quotes (base_id asc, target_id asc, timestamp desc);




