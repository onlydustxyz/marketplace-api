-- ------------------------------------------------------------------------
-- Update timestamp in account book events for migrated data

-- Mint on sponsor account = budget creation date
-- Look for budget created event on the correct currency that has at least an allocation with the same sponsor
with budget_allocated as (select cast(aggregate_id as uuid)                          as budget_id,
                                 CAST(payload #>> '{Allocated, sponsor_id}' as uuid) as sponsor_id
                          from events
                          where aggregate_name = 'BUDGET'
                            and payload -> 'Allocated' is not null),
     budget_created as (select cast(aggregate_id as uuid)        as budget_id,
                               timestamp                         as timestamp,
                               payload #>> '{Created, currency}' as currency_code
                        from events
                        where aggregate_name = 'BUDGET'
                          and payload -> 'Created' is not null)
update accounting.account_books_events abe
set timestamp = bc.timestamp
from budget_created bc
         join budget_allocated ba on bc.budget_id = ba.budget_id
         join currencies c on c.code ilike bc.currency_code
         join accounting.sponsor_accounts sa on sa.sponsor_id = ba.sponsor_id and sa.currency_id = c.id
         join accounting.account_books ab on ab.currency_id = c.id
where abe.account_book_id = ab.id
  and CAST(abe.payload #>> '{event,account,id}' AS UUID) = sa.id
  and abe.payload #>> '{event,account,type}' = 'SPONSOR_ACCOUNT'
  and abe.payload #>> '{event, @type}' = 'Mint'
  and abe.timestamp < (select min(timestamp) + interval '1 day' from accounting.account_books_events);

-- Transfer from sponsor account to project = first budget allocated linked to this project
-- Look for budget linked event on the correct currency to the correct project that has at least an allocation with the same sponsor
with budget_allocated as (select cast(aggregate_id as uuid)                          as budget_id,
                                 CAST(payload #>> '{Allocated, sponsor_id}' as uuid) as sponsor_id
                          from events
                          where aggregate_name = 'BUDGET'
                            and payload -> 'Allocated' is not null),
     budget_linked as (select cast(aggregate_id as uuid)                            as project_id,
                              CAST(payload #>> '{BudgetLinked, budget_id}' as uuid) as budget_id,
                              timestamp                                             as timestamp,
                              payload #>> '{BudgetLinked, currency}'                as currency_code
                       from events
                       where aggregate_name = 'PROJECT'
                         and payload -> 'BudgetLinked' is not null)
update accounting.account_books_events abe
set timestamp = bl.timestamp
from budget_linked bl
         join budget_allocated ba on bl.budget_id = ba.budget_id
         join currencies c on c.code ilike bl.currency_code
         join accounting.sponsor_accounts sa on sa.sponsor_id = ba.sponsor_id and sa.currency_id = c.id
         join accounting.account_books ab on ab.currency_id = c.id
where abe.account_book_id = ab.id
  and CAST(abe.payload #>> '{event,from,id}' AS UUID) = sa.id
  and CAST(abe.payload #>> '{event,to,id}' AS UUID) = bl.project_id
  and abe.payload #>> '{event,from,type}' = 'SPONSOR_ACCOUNT'
  and abe.payload #>> '{event,to,type}' = 'PROJECT'
  and abe.payload #>> '{event, @type}' = 'Transfer'
  and abe.timestamp < (select min(timestamp) + interval '1 day' from accounting.account_books_events);

-- Transfer from project to reward = reward creation date
update accounting.account_books_events abe
set timestamp = r.requested_at
from rewards r
         join accounting.account_books ab on ab.currency_id = r.currency_id
where abe.account_book_id = ab.id
  and CAST(abe.payload #>> '{event,from,id}' AS UUID) = r.project_id
  and CAST(abe.payload #>> '{event,to,id}' AS UUID) = r.id
  and abe.payload #>> '{event,from,type}' = 'PROJECT'
  and abe.payload #>> '{event,to,type}' = 'REWARD'
  and abe.payload #>> '{event, @type}' = 'Transfer'
  and abe.timestamp < (select min(timestamp) + interval '1 day' from accounting.account_books_events);

-- Burn of reward = reward process date
update accounting.account_books_events abe
set timestamp = rsd.paid_at
from rewards r
         join accounting.reward_status_data rsd on rsd.reward_id = r.id
         join accounting.account_books ab on ab.currency_id = r.currency_id
where abe.account_book_id = ab.id
  and CAST(abe.payload #>> '{event,account,id}' AS UUID) = r.id
  and abe.payload #>> '{event,account,type}' = 'REWARD'
  and abe.payload #>> '{event, @type}' = 'Burn'
  and abe.timestamp < (select min(timestamp) + interval '1 day' from accounting.account_books_events);

-- Transfer from reward to payment = avg(reward creation date, reward process date)
update accounting.account_books_events abe
set timestamp = r.requested_at + (age(rsd.paid_at, r.requested_at) / 2)
from rewards r
         join accounting.reward_status_data rsd on rsd.reward_id = r.id
         join accounting.account_books ab on ab.currency_id = r.currency_id
where abe.account_book_id = ab.id
  and CAST(abe.payload #>> '{event,from,id}' AS UUID) = r.id
  and abe.payload #>> '{event,from,type}' = 'REWARD'
  and abe.payload #>> '{event,to,type}' = 'PAYMENT'
  and abe.payload #>> '{event, @type}' = 'Transfer'
  and abe.timestamp < (select min(timestamp) + interval '1 day' from accounting.account_books_events);

-- Burn of payment = reward process date
update accounting.account_books_events abe
set timestamp = rsd.paid_at
from accounting.batch_payments p
         join accounting.batch_payment_rewards bpr on bpr.batch_payment_id = p.id
         join accounting.reward_status_data rsd on rsd.reward_id = bpr.reward_id
         join rewards r on r.id = bpr.reward_id
         join accounting.account_books ab on ab.currency_id = r.currency_id
where abe.account_book_id = ab.id
  and CAST(abe.payload #>> '{event,account,id}' AS UUID) = p.id
  and abe.payload #>> '{event,account,type}' = 'PAYMENT'
  and abe.payload #>> '{event, @type}' = 'Burn'
  and abe.timestamp < (select min(timestamp) + interval '1 day' from accounting.account_books_events);


-- ------------------------------------------------------------------------
-- New table for sponsor account allowance transactions (projected from graph)
create table accounting.sponsor_account_allowance_transactions
(
    id              UUID                        not null primary key,
    account_id      uuid                        not null references accounting.sponsor_accounts (id),
    timestamp       timestamp                   not null,
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

-- ------------------------------------------------------------------------
-- Insert data from events
WITH data AS (SELECT timestamp                                             AS timestamp,
                     CAST(payload #>> '{event, from, id}' AS UUID)         AS sponsor_account_id,
                     CAST(payload #>> '{event, to, id}' AS UUID)           AS project_id,
                     CAST(payload #>> '{event, amount, value}' AS numeric) AS amount
              FROM accounting.account_books_events
              WHERE payload #>> '{event, @type}' = 'Transfer'
                AND payload #>> '{event, from, type}' = 'SPONSOR_ACCOUNT'
                AND payload #>> '{event, to, type}' = 'PROJECT')
INSERT
INTO accounting.sponsor_account_allowance_transactions(id, account_id, amount, type, timestamp, project_id)
SELECT gen_random_uuid(), sponsor_account_id, amount, 'TRANSFER', timestamp, project_id
FROM data
;

WITH data AS (SELECT timestamp                                             AS timestamp,
                     CAST(payload #>> '{event, from, id}' AS UUID)         AS project_id,
                     CAST(payload #>> '{event, to, id}' AS UUID)           AS sponsor_account_id,
                     CAST(payload #>> '{event, amount, value}' AS numeric) AS amount
              FROM accounting.account_books_events
              WHERE payload #>> '{event, @type}' = 'Refund'
                AND payload #>> '{event, from, type}' = 'PROJECT'
                AND payload #>> '{event, to, type}' = 'SPONSOR_ACCOUNT')
INSERT
INTO accounting.sponsor_account_allowance_transactions(id, account_id, amount, type, timestamp, project_id)
SELECT gen_random_uuid(), sponsor_account_id, amount, 'REFUND', timestamp, project_id
FROM data
;

WITH data AS (SELECT timestamp                                             AS timestamp,
                     CAST(payload #>> '{event, account, id}' AS UUID)      AS sponsor_account_id,
                     CAST(payload #>> '{event, amount, value}' AS numeric) AS amount
              FROM accounting.account_books_events
              WHERE payload #>> '{event, @type}' = 'Mint'
                AND payload #>> '{event, account, type}' = 'SPONSOR_ACCOUNT')
INSERT
INTO accounting.sponsor_account_allowance_transactions(id, account_id, amount, type, timestamp)
SELECT gen_random_uuid(), sponsor_account_id, amount, 'MINT', timestamp
FROM data
;


WITH data AS (SELECT timestamp                                             AS timestamp,
                     CAST(payload #>> '{event, account, id}' AS UUID)      AS sponsor_account_id,
                     CAST(payload #>> '{event, amount, value}' AS numeric) AS amount
              FROM accounting.account_books_events
              WHERE payload #>> '{event, @type}' = 'Burn'
                AND payload #>> '{event, account, type}' = 'SPONSOR_ACCOUNT')
INSERT
INTO accounting.sponsor_account_allowance_transactions(id, account_id, amount, type, timestamp)
SELECT gen_random_uuid(), sponsor_account_id, amount, 'BURN', timestamp
FROM data
;

-- ------------------------------------------------------------------------
-- Add timestamp in sponsor account transactions
ALTER TABLE accounting.sponsor_account_transactions
    ADD COLUMN timestamp timestamp;

UPDATE accounting.sponsor_account_transactions
SET timestamp = tech_created_at;

ALTER TABLE accounting.sponsor_account_transactions
    ALTER COLUMN timestamp SET NOT NULL;

-- ------------------------------------------------------------------------
-- New view for all sponsor account transactions (physical and virtual)
CREATE VIEW accounting.all_sponsor_account_transactions AS
SELECT id         AS id,
       account_id AS sponsor_account_id,
       timestamp  AS timestamp,
       type       AS type,
       amount     AS amount,
       NULL       AS project_id
FROM accounting.sponsor_account_transactions
UNION
SELECT id         AS id,
       account_id AS sponsor_account_id,
       timestamp  AS timestamp,
       type       AS type,
       amount     AS amount,
       project_id AS project_id
FROM accounting.sponsor_account_allowance_transactions;

-- ------------------------------------------------------------------------
-- Update sponsor account transactions to have the correct type and always positive amounts
UPDATE accounting.sponsor_account_transactions
SET type = 'WITHDRAW'
WHERE type = 'DEPOSIT'
  AND amount < 0;

UPDATE accounting.sponsor_account_transactions
SET amount = -amount
WHERE type IN ('SPEND', 'WITHDRAW');