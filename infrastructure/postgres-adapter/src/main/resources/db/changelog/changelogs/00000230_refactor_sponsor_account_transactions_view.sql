CREATE OR REPLACE VIEW accounting.all_sponsor_account_transactions AS
SELECT id         AS id,
       account_id AS sponsor_account_id,
       timestamp  AS timestamp,
       type       AS type,
       amount     AS amount,
       NULL       AS project_id,
       network    AS network
FROM accounting.sponsor_account_transactions
UNION
SELECT id         AS id,
       account_id AS sponsor_account_id,
       timestamp  AS timestamp,
       type       AS type,
       amount     AS amount,
       project_id AS project_id,
       NULL       AS network
FROM accounting.sponsor_account_allowance_transactions;