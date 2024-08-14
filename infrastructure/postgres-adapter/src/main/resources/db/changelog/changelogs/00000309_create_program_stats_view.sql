create schema if not exists bi;

create view bi.program_stats_per_currency_per_project as
select sa.sponsor_id                                                                                              as program_id,
       ab.currency_id                                                                                             as currency_id,
       abt.project_id                                                                                             as project_id,
       coalesce(sum(abt.amount) filter (where abt.type in ('MINT', 'TRANSFER') and reward_id is null), 0)
           - coalesce(sum(abt.amount) filter (where abt.type in ('REFUND', 'BURN') and reward_id is null), 0)     as total_granted,
       coalesce(sum(abt.amount) filter (where abt.type in ('MINT', 'TRANSFER') and reward_id is not null), 0)
           - coalesce(sum(abt.amount) filter (where abt.type in ('REFUND', 'BURN') and reward_id is not null), 0) as total_rewarded
from accounting.sponsor_accounts sa
         join accounting.account_book_transactions abt
              on abt.sponsor_account_id = sa.id and
                 abt.project_id is not null and
                 abt.payment_id is null
         join accounting.account_books ab on abt.account_book_id = ab.id
group by sa.sponsor_id,
         ab.currency_id,
         abt.project_id;


create view bi.program_stats_per_currency as
select sa.sponsor_id                                                                                                    as program_id,
       ab.currency_id                                                                                                   as currency_id,

       coalesce(sum(amount) filter ( where type in ('MINT', 'TRANSFER') and project_id is null ), 0)
           - coalesce(sum(amount) filter ( where type in ('REFUND', 'BURN') and project_id is null ), 0)
           - coalesce(sum(amount) filter ( where type = 'TRANSFER' and project_id is not null and reward_id is null ), 0)
           + coalesce(sum(amount) filter ( where type = 'REFUND' and project_id is not null and reward_id is null ), 0) as total_available,

       coalesce(sum(amount) filter ( where type = 'TRANSFER' and project_id is not null and reward_id is null ), 0)
           - coalesce(sum(amount) filter ( where type = 'REFUND' and project_id is not null and reward_id is null ), 0) as total_granted,

       coalesce(sum(amount) filter ( where type = 'TRANSFER' and reward_id is not null ), 0)
           - coalesce(sum(amount) filter ( where type = 'REFUND' and reward_id is not null ), 0)                        as total_rewarded

from accounting.sponsor_accounts sa
         join accounting.account_book_transactions abt
              on abt.sponsor_account_id = sa.id and payment_id is null
         join accounting.account_books ab on abt.account_book_id = ab.id
group by sa.sponsor_id,
         ab.currency_id;


create view bi.program_stats as
select s.id                                     as program_id,
       coalesce(stats.granted_project_count, 0) as granted_project_count
from sponsors s
         left join lateral ( select count(distinct project_id) as granted_project_count
                             from bi.program_stats_per_currency_per_project
                             where program_id = s.id
                               and total_granted > 0) stats on true;
