DROP VIEW accounting.all_sponsor_account_transactions;

DROP table accounting.sponsor_account_allowance_transactions;

drop table accounting.account_book_transactions;

create table accounting.account_book_transactions
(
    id                 uuid primary key,
    timestamp          timestamp                   not null,
    account_book_id    uuid                        not null references accounting.account_books (id) deferrable initially deferred,
    type               accounting.transaction_type not null,
    sponsor_account_id uuid,
    project_id         uuid,
    reward_id          uuid,
    payment_id         uuid,
    amount             numeric                     not null
);

CREATE VIEW accounting.all_sponsor_account_transactions AS
SELECT id         AS id,
       account_id AS sponsor_account_id,
       timestamp  AS timestamp,
       type       AS type,
       amount     AS amount,
       NULL       AS project_id,
       network    AS network
FROM accounting.sponsor_account_transactions
UNION
SELECT gen_random_uuid()  AS id,
       sponsor_account_id AS sponsor_account_id,
       timestamp          AS timestamp,
       type               AS type,
       amount             AS amount,
       project_id         AS project_id,
       NULL               AS network
FROM accounting.account_book_transactions
WHERE sponsor_account_id IS NOT NULL
  AND reward_id IS NULL
  AND payment_id IS NULL;