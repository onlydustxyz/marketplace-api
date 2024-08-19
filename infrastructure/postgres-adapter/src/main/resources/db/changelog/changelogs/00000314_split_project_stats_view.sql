drop view bi.project_stats;

create view bi.project_contribution_stats as
with contributors_stats as (select contributor_id, min(created_at) as first
                            from indexer_exp.contributions
                            where status = 'COMPLETED'
                            group by contributor_id)
select pgr.project_id                                                                                                                    as project_id,
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
group by pgr.project_id;


create view bi.project_reward_stats as
select r.project_id, avg(rsd.amount_usd_equivalent) as average_reward_usd_amount
from rewards r
         join accounting.reward_status_data rsd on rsd.reward_id = r.id
group by r.project_id;
