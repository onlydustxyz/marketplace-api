update accounting.sponsor_account_transactions
set transaction_id = (select tt.id
                      from accounting.transfer_transactions tt
                               left join accounting.deposits d on d.transaction_id = tt.id
                      where tt.reference = accounting.sponsor_account_transactions.reference
                        and tt.amount = accounting.sponsor_account_transactions.amount
                      order by d.id desc nulls last
                      limit 1)
where transaction_id is null
  and type = 'DEPOSIT';


insert into accounting.transfer_transactions(id, blockchain, reference, timestamp, sender_address, recipient_address, amount, contract_address)
select gen_random_uuid(),
       sat.network,
       sat.reference,
       sat.timestamp,
       case when sat.type = 'DEPOSIT' then sat.third_party_account_number else '0x' end,
       case when sat.type in ('SPEND', 'WITHDRAW') then sat.third_party_account_number else '0x' end,
       sat.amount,
       erc20.address
from accounting.sponsor_account_transactions sat
         join accounting.sponsor_accounts sa on sa.id = sat.account_id
         join currencies c on sa.currency_id = c.id
         left join erc20 on c.id = erc20.currency_id and erc20.blockchain = sat.network
where sat.transaction_id is null
  and sat.type = 'DEPOSIT';


update accounting.sponsor_account_transactions
set transaction_id = (select tt.id
                      from accounting.transfer_transactions tt
                      where tt.reference = accounting.sponsor_account_transactions.reference
                        and tt.amount = accounting.sponsor_account_transactions.amount
                      limit 1)
where transaction_id is null
  and type = 'DEPOSIT';


select sat.type, count(*)
from accounting.deposits
         join accounting.sponsor_account_transactions sat on sat.transaction_id = accounting.deposits.transaction_id and sat.type != 'DEPOSIT'
group by sat.type;

delete
from accounting.deposits
where transaction_id in (select transaction_id from accounting.sponsor_account_transactions where type != 'DEPOSIT');

-- keep only one deposit per transaction_id
delete
from accounting.deposits
where id not in (select distinct on (d.transaction_id) d.id
                 from accounting.deposits d
                 group by d.transaction_id, d.id);



insert into accounting.deposits(id, transaction_id, sponsor_id, currency_id, status, billing_information)
select gen_random_uuid(),
       sat.transaction_id,
       sa.sponsor_id,
       sa.currency_id,
       'COMPLETED',
       null
from accounting.sponsor_account_transactions sat
         join accounting.sponsor_accounts sa on sa.id = sat.account_id
where sat.type = 'DEPOSIT'
  and not exists(select 1
                 from accounting.deposits d
                 where d.transaction_id = sat.transaction_id
                   and d.status = 'COMPLETED');


delete
from accounting.all_transactions
where type = 'DEPOSIT'
  and id not in (select d.id from accounting.deposits d);


insert into accounting.all_transactions(id, timestamp, currency_id, type, sponsor_id, program_id, project_id, reward_id, payment_id, amount, deposit_status)
select d.id,
       sat.timestamp,
       d.currency_id,
       'DEPOSIT',
       d.sponsor_id,
       null,
       null,
       null,
       null,
       sat.amount,
       d.status
from accounting.deposits d
         join accounting.sponsor_account_transactions sat on sat.transaction_id = d.transaction_id
where not exists(select 1
                 from accounting.all_transactions at
                 where at.id = d.id);


insert into accounting.all_transactions(id, timestamp, currency_id, type, sponsor_id, program_id, project_id, reward_id, payment_id, amount, deposit_status)
select sat.id,
       sat.timestamp,
       sa.currency_id,
       sat.type,
       sa.sponsor_id,
       null,
       null,
       null,
       null,
       sat.amount,
       null
from accounting.sponsor_account_transactions sat
         join accounting.sponsor_accounts sa on sa.id = sat.account_id
where sat.type = 'WITHDRAW';

