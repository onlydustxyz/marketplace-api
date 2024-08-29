create table accounting.transfer_transactions
(
    id                             uuid primary key,
    blockchain                     accounting.network       not null,
    reference                      text                     not null,
    index                          serial                   not null,
    timestamp                      timestamp with time zone not null,
    sender_address                 text                     not null,
    recipient_address              text                     not null,
    amount                         numeric                  not null,
    contract_address               text,
    sponsor_account_transaction_id uuid                     not null references accounting.sponsor_account_transactions (id),
    tech_created_at                timestamp with time zone not null default now(),
    tech_updated_at                timestamp with time zone not null default now(),
    unique (blockchain, reference, index)
);

create trigger accounting_transfer_transactions_set_tech_updated_at
    before update
    on accounting.transfer_transactions
    for each row
execute function set_tech_updated_at();

create type accounting.deposit_status as enum ('DRAFT', 'PENDING', 'COMPLETED', 'REJECTED');

create table accounting.deposits
(
    id                  uuid primary key,
    transaction_id      uuid                      not null references accounting.transfer_transactions (id),
    sponsor_id          uuid                      not null references sponsors (id),
    currency_id         uuid                      not null references currencies (id),
    status              accounting.deposit_status not null,
    billing_information jsonb,
    tech_created_at     timestamp with time zone  not null default now(),
    tech_updated_at     timestamp with time zone  not null default now()
);

create trigger accounting_deposits_set_tech_updated_at
    before update
    on accounting.deposits
    for each row
execute function set_tech_updated_at();

alter table accounting.sponsor_account_transactions
    add column transaction_id uuid unique references accounting.transfer_transactions (id);

insert into accounting.transfer_transactions(id, blockchain, reference, timestamp, sender_address, recipient_address, amount, contract_address,
                                             sponsor_account_transaction_id)
select gen_random_uuid(),
       sat.network,
       sat.reference,
       sat.timestamp,
       case when sat.type = 'DEPOSIT' then sat.third_party_account_number else '0x' end,
       case when sat.type in ('SPEND', 'WITHDRAW') then sat.third_party_account_number else '0x' end,
       sat.amount,
       erc20.address,
       sat.id
from accounting.sponsor_account_transactions sat
         join accounting.sponsor_accounts sa on sa.id = sat.account_id
         join currencies c on sa.currency_id = c.id
         left join erc20 on c.id = erc20.currency_id and erc20.blockchain = sat.network;

update accounting.sponsor_account_transactions
set transaction_id = tt.id
from accounting.transfer_transactions tt
where tt.sponsor_account_transaction_id = sponsor_account_transactions.id;

alter table accounting.transfer_transactions
    drop column sponsor_account_transaction_id;

insert into accounting.deposits(id, transaction_id, sponsor_id, currency_id, status, billing_information)
select gen_random_uuid(),
       tt.id,
       sa.sponsor_id,
       sa.currency_id,
       'COMPLETED',
       null
from accounting.sponsor_account_transactions sat
         join accounting.sponsor_accounts sa on sa.id = sat.account_id
         join accounting.transfer_transactions tt on tt.reference = sat.reference and sat.amount = tt.amount;
