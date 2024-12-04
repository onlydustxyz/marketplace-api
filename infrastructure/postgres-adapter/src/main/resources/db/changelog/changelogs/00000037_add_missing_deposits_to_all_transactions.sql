insert into accounting.all_transactions (id, timestamp, currency_id, type, sponsor_id, program_id, project_id, reward_id, payment_id, amount, deposit_status)
select d.id,
       t.timestamp,
       d.currency_id,
       'DEPOSIT',
       d.sponsor_id,
       null,
       null,
       null,
       null,
       t.amount,
       d.status
from accounting.deposits d
         join accounting.transfer_transactions t on d.transaction_id = t.id
         left join accounting.all_transactions at on at.id = d.id
where at.id is null