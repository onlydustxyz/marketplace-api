drop materialized view global_users_ranks;

create materialized view global_users_ranks AS
with ranks as (select coalesce(c.github_user_id, rr.github_user_id, rs.github_user_id) as github_user_id,
                      rank() over (order by c.rank)                                    as contributions_rank,
                      rank() over (order by rs.rank)                                   as rewards_sent_rank,
                      rank() over (order by rr.rank)                                   as rewards_received_rank
               from users_rank_per_contribution c
                        full join users_rank_per_reward_received rr on rr.github_user_id = c.github_user_id
                        full join users_rank_per_reward_sent rs on rs.github_user_id = c.github_user_id),
     max_ranks as (select max(contributions_rank)    as contributions_rank,
                          max(rewards_sent_rank)     as rewards_sent_rank,
                          max(rewards_received_rank) as rewards_received_rank
                   from ranks),
     normalized_ranks as (select r.github_user_id                                                     as github_user_id,
                                 round(100.0 * r.contributions_rank / mr.contributions_rank, 2)       as contributions_rank,
                                 round(100.0 * r.rewards_sent_rank / mr.rewards_sent_rank, 2)         as rewards_sent_rank,
                                 round(100.0 * r.rewards_received_rank / mr.rewards_received_rank, 2) as rewards_received_rank
                          from ranks r
                                   join max_ranks mr on true)
select rank()
       over (order by 100 * r.contributions_rank + 2 * r.rewards_sent_rank + 1.5 * r.rewards_received_rank) as rank,
       percent_rank()
       over (order by 100 * r.contributions_rank + 2 * r.rewards_sent_rank + 1.5 * r.rewards_received_rank) as rank_percentile,
       r.*
from normalized_ranks r
order by rank
;

DROP VIEW users_languages_ranks;

CREATE VIEW users_languages_ranks AS
(
select lfe.language_id                                                                       as language_id,
       c.contributor_id                                                                      as contributor_id,
       rank() over (partition by lfe.language_id order by count(distinct c.id) desc)         as rank,
       percent_rank() over (partition by lfe.language_id order by count(distinct c.id) desc) as rank_percentile
from language_file_extensions lfe
         join indexer_exp.contributions c on lfe.extension = any (c.main_file_extensions)
         join indexer_exp.github_repos gr on gr.id = c.repo_id and gr.visibility = 'PUBLIC'
where exists (select 1
              from project_github_repos pgr
                       join projects p on p.id = pgr.project_id and p.visibility = 'PUBLIC'
              where pgr.github_repo_id = c.repo_id)
group by lfe.language_id, c.contributor_id
    );


DROP VIEW users_ecosystems_ranks;

CREATE VIEW users_ecosystems_ranks AS
(
select pe.ecosystem_id                                                                       as ecosystem_id,
       c.contributor_id                                                                      as contributor_id,
       rank() over (partition by pe.ecosystem_id order by count(distinct c.id) desc)         as rank,
       percent_rank() over (partition by pe.ecosystem_id order by count(distinct c.id) desc) as rank_percentile
from projects_ecosystems pe
         join project_github_repos pgr on pe.project_id = pgr.project_id
         join projects p on p.id = pe.project_id and p.visibility = 'PUBLIC'
         join indexer_exp.github_repos gr on gr.id = pgr.github_repo_id and gr.visibility = 'PUBLIC'
         join indexer_exp.contributions c on c.repo_id = pgr.github_repo_id
         join indexer_exp.github_accounts ga on ga.id = c.contributor_id and ga.type = 'USER'
group by pe.ecosystem_id, c.contributor_id
    );