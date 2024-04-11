create table accounting.sponsor_account_allowance_transactions
(
    id              UUID                        not null primary key,
    account_id      uuid                        not null references accounting.sponsor_accounts (id),
    type            accounting.transaction_type not null,
    amount          numeric                     not null,
    project_id      uuid references project_details (project_id),
    tech_created_at timestamp default now()     not null,
    tech_updated_at timestamp default now()     not null
);

CREATE TRIGGER update_sponsor_account_allowance_transactions_tech_updated_at
    BEFORE UPDATE
    ON accounting.sponsor_account_allowance_transactions
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

WITH data AS (SELECT tech_created_at                                       AS timestamp,
                     CAST(payload #>> '{event, from, id}' AS UUID)         AS sponsor_account_id,
                     CAST(payload #>> '{event, to, id}' AS UUID)           AS project_id,
                     CAST(payload #>> '{event, amount, value}' AS numeric) AS amount
              FROM accounting.account_books_events
              WHERE payload #>> '{event, @type}' = 'Transfer'
                AND payload #>> '{event, from, type}' = 'SPONSOR_ACCOUNT'
                AND payload #>> '{event, to, type}' = 'PROJECT')
INSERT
INTO accounting.sponsor_account_allowance_transactions(id, account_id, amount, type, tech_created_at, project_id)
SELECT gen_random_uuid(), sponsor_account_id, amount, 'ALLOCATION', timestamp, project_id
FROM data
;

WITH data AS (SELECT tech_created_at                                       AS timestamp,
                     CAST(payload #>> '{event, from, id}' AS UUID)         AS project_id,
                     CAST(payload #>> '{event, to, id}' AS UUID)           AS sponsor_account_id,
                     CAST(payload #>> '{event, amount, value}' AS numeric) AS amount
              FROM accounting.account_books_events
              WHERE payload #>> '{event, @type}' = 'Refund'
                AND payload #>> '{event, from, type}' = 'PROJECT'
                AND payload #>> '{event, to, type}' = 'SPONSOR_ACCOUNT')
INSERT
INTO accounting.sponsor_account_allowance_transactions(id, account_id, amount, type, tech_created_at, project_id)
SELECT gen_random_uuid(), sponsor_account_id, -amount, 'ALLOCATION', timestamp, project_id
FROM data
;

WITH data AS (SELECT tech_created_at                                       AS timestamp,
                     CAST(payload #>> '{event, account, id}' AS UUID)      AS sponsor_account_id,
                     CAST(payload #>> '{event, amount, value}' AS numeric) AS amount
              FROM accounting.account_books_events
              WHERE payload #>> '{event, @type}' = 'Mint'
                AND payload #>> '{event, account, type}' = 'SPONSOR_ACCOUNT')
INSERT
INTO accounting.sponsor_account_allowance_transactions(id, account_id, amount, type, tech_created_at)
SELECT gen_random_uuid(), sponsor_account_id, amount, 'ALLOWANCE', timestamp
FROM data
;


WITH data AS (SELECT tech_created_at                                       AS timestamp,
                     CAST(payload #>> '{event, account, id}' AS UUID)      AS sponsor_account_id,
                     CAST(payload #>> '{event, amount, value}' AS numeric) AS amount
              FROM accounting.account_books_events
              WHERE payload #>> '{event, @type}' = 'Burn'
                AND payload #>> '{event, account, type}' = 'SPONSOR_ACCOUNT')
INSERT
INTO accounting.sponsor_account_allowance_transactions(id, account_id, amount, type, tech_created_at)
SELECT gen_random_uuid(), sponsor_account_id, -amount, 'ALLOWANCE', timestamp
FROM data
;

CREATE VIEW accounting.all_sponsor_account_transactions AS
SELECT id              AS id,
       account_id      AS sponsor_account_id,
       tech_created_at AS timestamp,
       type            AS type,
       amount          AS amount,
       NULL            AS project_id
FROM accounting.sponsor_account_transactions
UNION
SELECT id              AS id,
       account_id      AS sponsor_account_id,
       tech_created_at AS timestamp,
       type            AS type,
       amount          AS amount,
       project_id      AS project_id
FROM accounting.sponsor_account_allowance_transactions;
