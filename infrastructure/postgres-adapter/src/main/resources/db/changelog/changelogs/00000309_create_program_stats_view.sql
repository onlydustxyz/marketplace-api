create view accounting.program_stats_per_project as
select s.id                                                                                                   as program_id,
       ab.currency_id                                                                                         as currency_id,
       abt.project_id                                                                                         as project_id,
       coalesce(sum(abt.amount) filter (where abt.type in ('MINT', 'TRANSFER') and reward_id is null), 0)
           - coalesce(sum(abt.amount) filter (where abt.type in ('REFUND', 'BURN') and reward_id is null), 0) as total_granted
from sponsors s
         join accounting.sponsor_accounts sa on sa.sponsor_id = s.id
         join accounting.account_book_transactions abt
              on abt.sponsor_account_id = sa.id and abt.project_id is not null and abt.reward_id is not null and abt.payment_id is null
         join accounting.account_books ab on abt.account_book_id = ab.id
group by s.id,
         ab.currency_id,
         abt.project_id;
