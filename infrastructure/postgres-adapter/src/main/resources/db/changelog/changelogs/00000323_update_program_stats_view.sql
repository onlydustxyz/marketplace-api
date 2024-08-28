drop view bi.sponsor_stats_per_currency_per_program;
drop view bi.program_stats_per_currency;

create view bi.program_stats_per_currency as
select abt.program_id                                                                                                            as program_id,
       abt.currency_id                                                                                                           as currency_id,

       coalesce(sum(amount) filter ( where type = 'TRANSFER' and abt.program_id is not null and abt.project_id is null ), 0)
           - coalesce(sum(amount) filter ( where type = 'REFUND' and abt.program_id is not null and abt.project_id is null ), 0) as total_allocated,

       coalesce(sum(amount) filter ( where type = 'TRANSFER' and abt.project_id is not null and abt.reward_id is null ), 0)
           - coalesce(sum(amount) filter ( where type = 'REFUND' and abt.project_id is not null and abt.reward_id is null ), 0)  as total_granted,

       coalesce(sum(amount) filter ( where type = 'TRANSFER' and abt.reward_id is not null and abt.payment_id is null ), 0)
           - coalesce(sum(amount) filter ( where type = 'REFUND' and abt.reward_id is not null and abt.payment_id is null ), 0)  as total_rewarded

from accounting.account_book_transactions abt
group by abt.program_id,
         abt.currency_id;
