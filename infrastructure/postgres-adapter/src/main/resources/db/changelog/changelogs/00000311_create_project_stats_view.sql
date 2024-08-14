create view bi.project_stats_per_currency as
select abt.project_id                                                                            as project_id,
       ab.currency_id                                                                            as currency_id,

       coalesce(sum(amount) filter ( where type = 'TRANSFER' and reward_id is null ), 0)
           - coalesce(sum(amount) filter ( where type = 'REFUND' and reward_id is null ), 0)     as total_granted,

       coalesce(sum(amount) filter ( where type = 'TRANSFER' and reward_id is not null ), 0)
           - coalesce(sum(amount) filter ( where type = 'REFUND' and reward_id is not null ), 0) as total_rewarded

from accounting.account_book_transactions abt
         join accounting.account_books ab on abt.account_book_id = ab.id
where abt.project_id is not null
  and payment_id is null
group by abt.project_id,
         ab.currency_id;


create view bi.project_stats as
with contributors_stats as (select contributor_id, min(created_at) as first
                            from indexer_exp.contributions
                            where status = 'COMPLETED'
                            group by contributor_id),
     reward_stats as (select r.project_id, avg(rsd.amount_usd_equivalent) as average_usd_amount
                      from rewards r
                               join accounting.reward_status_data rsd on rsd.reward_id = r.id
                      group by r.project_id),
     contributions_stats as (select pgr.project_id                                                                                                                    as project_id,
                                    count(distinct c.id)
                                    filter ( where c.status = 'COMPLETED' and c.type = 'PULL_REQUEST' and c.created_at > now() - interval '3 months')                 as current_period_merged_pr_count,

                                    count(distinct c.id) filter ( where c.status = 'COMPLETED' and c.type = 'PULL_REQUEST' and
                                                                        c.created_at between now() - interval '6 months' and now() - interval '3 months')             as last_period_merged_pr_count,

                                    count(distinct c.contributor_id)
                                    filter ( where c.status = 'COMPLETED' and c.created_at > now() - interval '3 months')                                             as current_period_active_contributor_count,

                                    count(distinct c.contributor_id) filter ( where c.status = 'COMPLETED' and
                                                                                    c.created_at between now() - interval '6 months' and now() - interval '3 months') as last_period_active_contributor_count,

                                    count(distinct c.contributor_id) filter ( where c.status = 'COMPLETED' and cs.first > now() - interval '3 months' and
                                                                                    c.created_at > now() -
                                                                                                   interval '3 months')                                               as current_period_new_contributor_count,

                                    count(distinct c.contributor_id)
                                    filter ( where c.status = 'COMPLETED' and cs.first between now() - interval '6 months' and now() - interval '3 months' and
                                                   c.created_at between now() - interval '6 months' and now() - interval '3 months')                                  as last_period_new_contributor_count
                             from indexer_exp.contributions c
                                      join contributors_stats cs on cs.contributor_id = c.contributor_id
                                      join project_github_repos pgr on pgr.github_repo_id = c.repo_id
                             group by pgr.project_id)
select coalesce(cs.project_id, rs.project_id)                  as project_id,
       coalesce(cs.current_period_merged_pr_count, 0)          as current_period_merged_pr_count,
       coalesce(cs.last_period_merged_pr_count, 0)             as last_period_merged_pr_count,
       coalesce(cs.current_period_active_contributor_count, 0) as current_period_active_contributor_count,
       coalesce(cs.last_period_active_contributor_count, 0)    as last_period_active_contributor_count,
       coalesce(cs.current_period_new_contributor_count, 0)    as current_period_new_contributor_count,
       coalesce(cs.last_period_new_contributor_count, 0)       as last_period_new_contributor_count,
       coalesce(rs.average_usd_amount, 0)                      as average_reward_usd_amount
from contributions_stats cs
         full join reward_stats rs on rs.project_id = cs.project_id;
