drop view if exists bi.sponsor_stats_per_currency;

create view bi.sponsor_stats_per_currency as
with virtual_stats as (select sa.sponsor_id                                                                                                  as sponsor_id,
                              sa.currency_id                                                                                                 as currency_id,
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
                                join accounting.sponsor_accounts sa on sa.id = abt.sponsor_account_id
                       group by sa.sponsor_id,
                                sa.currency_id),
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