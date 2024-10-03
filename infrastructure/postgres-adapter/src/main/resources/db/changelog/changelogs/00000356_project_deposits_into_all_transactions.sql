INSERT INTO accounting.all_transactions (id, timestamp, type, currency_id, amount, sponsor_id, program_id, project_id, reward_id, payment_id, deposit_status)
SELECT d.id,
       tt.timestamp,
       'DEPOSIT',
       d.currency_id,
       tt.amount,
       d.sponsor_id,
       null,
       null,
       null,
       null,
       d.status
FROM accounting.deposits d
         join accounting.transfer_transactions tt on tt.id = d.transaction_id
where d.status != 'DRAFT';
