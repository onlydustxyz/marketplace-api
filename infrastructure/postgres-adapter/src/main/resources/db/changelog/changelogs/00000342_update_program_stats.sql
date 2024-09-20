drop view bi.program_stats;
drop materialized view bi.program_contribution_stats;

create view bi.program_stats as
with project_users as (select pc.project_id, pc.github_user_id as contributor_id
                       from projects_contributors pc
                       union
                       select pl.project_id, u.github_user_id as contributor_id
                       from project_leads pl
                                join iam.users u on u.id = pl.user_id)
select p.id                                                                                  as program_id,
       coalesce(count(distinct s.project_id), 0)                                             as granted_project_count,
       coalesce(count(distinct abt.reward_id) filter ( where abt.type = 'TRANSFER' ), 0)
           - coalesce(count(distinct abt.reward_id) filter ( where abt.type = 'REFUND' ), 0) as reward_count,
       coalesce(count(distinct pu.contributor_id), 0)                                        as user_count
from programs p
         left join bi.program_stats_per_currency_per_project s on p.id = s.program_id and s.total_granted > 0
         left join accounting.account_book_transactions abt on abt.program_id = s.program_id and
                                                               abt.project_id = s.project_id and
                                                               abt.payment_id is null and
                                                               abt.reward_id is not null
         left join project_users pu on pu.project_id = s.project_id
group by p.id;
