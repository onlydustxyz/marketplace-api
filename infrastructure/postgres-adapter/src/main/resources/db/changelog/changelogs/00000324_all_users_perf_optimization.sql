create or replace view iam.all_users as
select u.id                                                         as user_id,
       coalesce(ga.id, u.github_user_id)                            as github_user_id,
       coalesce(ga.login, u.github_login)                           as login,
       coalesce(upi.avatar_url, ga.avatar_url, u.github_avatar_url) as avatar_url,
       u.email
from iam.users u
         full outer join indexer_exp.github_accounts ga on ga.id = u.github_user_id
         left join user_profile_info upi on u.id = upi.id;



drop view bi.project_contribution_stats;

create materialized view bi.project_contribution_stats as
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

create unique index project_contribution_stats_pk on bi.project_contribution_stats (project_id);
refresh materialized view bi.project_contribution_stats;



drop view bi.project_reward_stats;

create materialized view bi.project_reward_stats as
select r.project_id, avg(rsd.amount_usd_equivalent) as average_reward_usd_amount
from rewards r
         join accounting.reward_status_data rsd on rsd.reward_id = r.id
group by r.project_id;

create unique index project_reward_stats_pk on bi.project_reward_stats (project_id);
refresh materialized view bi.project_reward_stats;