DROP VIEW accounting.all_sponsor_account_transactions;

CREATE VIEW accounting.all_transactions AS
SELECT abt.id,
       abt.timestamp,
       abt.type,
       abt.currency_id,
       abt.amount,
       abt.sponsor_id,
       abt.program_id,
       abt.project_id,
       abt.reward_id,
       abt.payment_id,
       NULL AS deposit_status
FROM accounting.account_book_transactions abt
UNION
SELECT d.id                                   AS id,
       tt.timestamp                           AS timestamp,
       'DEPOSIT'::accounting.transaction_type AS type,
       d.currency_id                          AS currency_id,
       tt.amount                              AS amount,
       d.sponsor_id                           AS sponsor_id,
       NULL                                   AS program_id,
       NULL                                   AS project_id,
       NULL                                   AS reward_id,
       NULL                                   AS payment_id,
       d.status                               AS deposit_status
FROM accounting.deposits d
         JOIN accounting.transfer_transactions tt on tt.id = d.transaction_id
WHERE d.status = 'COMPLETED'
   OR d.status = 'PENDING';