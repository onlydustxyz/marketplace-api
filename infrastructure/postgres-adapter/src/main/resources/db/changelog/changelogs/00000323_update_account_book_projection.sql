drop view accounting.all_sponsor_account_transactions;
drop view bi.sponsor_stats_per_currency;
drop view bi.sponsor_stats_per_currency_per_program;
drop view bi.program_stats;
drop view bi.program_stats_per_currency;
drop view bi.program_stats_per_currency_per_project;
drop view bi.project_stats_per_currency;
drop view programs_projects;


drop table accounting.account_book_transactions;

create table accounting.account_book_transactions
(
    id          uuid primary key,
    timestamp   timestamp                   not null,
    currency_id uuid                        not null references currencies (id),
    type        accounting.transaction_type not null,
    sponsor_id  uuid,
    program_id  uuid,
    project_id  uuid,
    reward_id   uuid,
    payment_id  uuid,
    amount      numeric                     not null
);


create view bi.sponsor_stats_per_currency as
with virtual_stats as (select abt.sponsor_id                                                                                                 as sponsor_id,
                              abt.currency_id                                                                                                as currency_id,
                              sum(abt.amount) filter (where abt.type = 'MINT' and abt.program_id is null)                                    as minted,
                              sum(abt.amount) filter (where abt.type = 'REFUND' and abt.program_id is null)                                  as refunded,
                              sum(abt.amount) filter (where abt.type = 'TRANSFER' and abt.program_id is not null and abt.project_id is null) as allocated,
                              sum(abt.amount) filter (where abt.type = 'REFUND' and abt.program_id is not null and abt.project_id is null)   as unallocated,
                              sum(abt.amount) filter (where abt.type = 'TRANSFER' and abt.project_id is not null and abt.reward_id is null)  as granted,
                              sum(abt.amount) filter (where abt.type = 'REFUND' and abt.project_id is not null and abt.reward_id is null)    as ungranted,
                              sum(abt.amount) filter (where abt.type = 'TRANSFER' and abt.reward_id is not null and abt.payment_id is null)  as rewarded,
                              sum(abt.amount) filter (where abt.type = 'REFUND' and abt.reward_id is not null and abt.payment_id is null)    as canceled,
                              sum(abt.amount) filter (where abt.type = 'BURN' and abt.reward_id is not null)                                 as paid
                       from accounting.account_book_transactions abt
                       group by abt.sponsor_id,
                                abt.currency_id),
     physical_stats as (select sa.sponsor_id                                     as sponsor_id,
                               sa.currency_id                                    as currency_id,
                               sum(sat.amount) filter ( where type = 'DEPOSIT')  as deposited,
                               sum(sat.amount) filter ( where type = 'WITHDRAW') as withdrawn,
                               sum(sat.amount) filter ( where type = 'SPEND')    as spent
                        from accounting.sponsor_account_transactions sat
                                 join accounting.sponsor_accounts sa on sa.id = sat.account_id
                        group by sa.sponsor_id,
                                 sa.currency_id)
select coalesce(vs.sponsor_id, ps.sponsor_id)                  as sponsor_id,
       coalesce(vs.currency_id, ps.currency_id)                as currency_id,
       coalesce(vs.minted, 0) - coalesce(vs.refunded, 0)       as initial_allowance,
       coalesce(vs.allocated, 0) - coalesce(vs.unallocated, 0) as total_allocated,
       coalesce(vs.granted, 0) - coalesce(vs.ungranted, 0)     as total_granted,
       coalesce(vs.rewarded, 0) - coalesce(vs.canceled, 0)     as total_rewarded,
       coalesce(vs.paid, 0)                                    as total_paid,
       coalesce(ps.deposited, 0) - coalesce(ps.withdrawn, 0)   as initial_balance,
       coalesce(ps.spent, 0)                                   as total_spent
from virtual_stats vs
         full join physical_stats ps on vs.sponsor_id = ps.sponsor_id and vs.currency_id = ps.currency_id;


create view programs_projects as
with allocations as (select abt.currency_id,
                            abt.program_id,
                            abt.project_id,
                            coalesce(sum(abt.amount) filter ( where abt.type = 'TRANSFER' ), 0)
                                - coalesce(sum(abt.amount) filter ( where abt.type = 'REFUND' ), 0) as amount
                     from accounting.account_book_transactions abt
                     where abt.project_id is not null
                       and abt.reward_id is null
                     group by abt.currency_id, abt.program_id, abt.project_id)
select distinct program_id, project_id
from allocations
where amount > 0;


create view bi.project_stats_per_currency as
select abt.project_id                                                                            as project_id,
       abt.currency_id                                                                           as currency_id,

       coalesce(sum(amount) filter ( where type = 'TRANSFER' and reward_id is null ), 0)
           - coalesce(sum(amount) filter ( where type = 'REFUND' and reward_id is null ), 0)     as total_granted,

       coalesce(sum(amount) filter ( where type = 'TRANSFER' and reward_id is not null ), 0)
           - coalesce(sum(amount) filter ( where type = 'REFUND' and reward_id is not null ), 0) as total_rewarded

from accounting.account_book_transactions abt
where abt.project_id is not null
  and payment_id is null
group by abt.project_id,
         abt.currency_id;


create view bi.program_stats_per_currency_per_project as
select abt.program_id                                                                                      as program_id,
       abt.currency_id                                                                                     as currency_id,
       abt.project_id                                                                                      as project_id,
       coalesce(sum(abt.amount) filter (where abt.type = 'TRANSFER' and abt.reward_id is null), 0)
           - coalesce(sum(abt.amount) filter (where abt.type = 'REFUND' and abt.reward_id is null), 0)     as total_granted,
       coalesce(sum(abt.amount) filter (where abt.type = 'TRANSFER' and abt.reward_id is not null), 0)
           - coalesce(sum(abt.amount) filter (where abt.type = 'REFUND' and abt.reward_id is not null), 0) as total_rewarded
from accounting.account_book_transactions abt
where abt.program_id is not null
  and abt.project_id is not null
  and abt.payment_id is null
group by abt.program_id,
         abt.currency_id,
         abt.project_id;


create view bi.program_stats_per_currency as
select abt.program_id                                                                                                           as program_id,
       abt.currency_id                                                                                                          as currency_id,

       coalesce(sum(amount) filter ( where type = 'TRANSFER' and abt.project_id is null ), 0)
           - coalesce(sum(amount) filter ( where type = 'REFUND' and abt.project_id is null ), 0)
           - coalesce(sum(amount) filter ( where type = 'TRANSFER' and abt.project_id is not null and abt.reward_id is null ), 0)
           + coalesce(sum(amount) filter ( where type = 'REFUND' and abt.project_id is not null and abt.reward_id is null ), 0) as total_available,

       coalesce(sum(amount) filter ( where type = 'TRANSFER' and abt.project_id is not null and abt.reward_id is null ), 0)
           - coalesce(sum(amount) filter ( where type = 'REFUND' and abt.project_id is not null and abt.reward_id is null ), 0) as total_granted,

       coalesce(sum(amount) filter ( where type = 'TRANSFER' and abt.reward_id is not null ), 0)
           - coalesce(sum(amount) filter ( where type = 'REFUND' and abt.reward_id is not null ), 0)                            as total_rewarded

from accounting.account_book_transactions abt
where abt.program_id is not null
  and abt.payment_id is null
group by abt.program_id,
         abt.currency_id;


create view bi.program_stats as
select p.id                                      as program_id,
       coalesce(count(distinct s.project_id), 0) as granted_project_count
from programs p
         left join bi.program_stats_per_currency_per_project s on p.id = s.program_id and s.total_granted > 0
group by p.id;


create view bi.sponsor_stats_per_currency_per_program as
select abt.sponsor_id                                                                                       as sponsor_id,
       abt.currency_id                                                                                      as currency_id,
       abt.program_id                                                                                       as program_id,
       coalesce(sum(abt.amount) filter (where abt.type = 'TRANSFER' and abt.project_id is null), 0)
           - coalesce(sum(abt.amount) filter (where abt.type = 'REFUND' and abt.project_id is null), 0)     as total_allocated,
       coalesce(sum(abt.amount) filter (where abt.type = 'TRANSFER' and abt.project_id is not null), 0)
           - coalesce(sum(abt.amount) filter (where abt.type = 'REFUND' and abt.project_id is not null), 0) as total_granted
from accounting.account_book_transactions abt
where abt.sponsor_id is not null
  and abt.program_id is not null
  and abt.reward_id is null
group by abt.sponsor_id,
         abt.currency_id,
         abt.program_id;

CREATE VIEW accounting.all_sponsor_account_transactions AS
SELECT sat.id         AS id,
       sa.sponsor_id  AS sponsor_id,
       sat.timestamp  AS timestamp,
       sat.type       AS type,
       sat.amount     AS amount,
       sa.currency_id AS currency_id,
       NULL           AS program_id,
       sat.network    AS network
FROM accounting.sponsor_account_transactions sat
         JOIN accounting.sponsor_accounts sa on sa.id = sat.account_id
UNION
SELECT gen_random_uuid() AS id,
       abt.sponsor_id    AS sponsor_id,
       abt.timestamp     AS timestamp,
       abt.type          AS type,
       abt.amount        AS amount,
       abt.currency_id   AS currency_id,
       abt.program_id    AS program_id,
       NULL              AS network
FROM accounting.account_book_transactions abt
WHERE abt.sponsor_id IS NOT NULL
  AND abt.project_id IS NULL
  AND abt.reward_id IS NULL
  AND abt.payment_id IS NULL;

