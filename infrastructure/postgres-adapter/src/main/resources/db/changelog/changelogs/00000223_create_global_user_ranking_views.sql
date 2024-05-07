create view users_rank_per_contribution as
WITH contributions_stats_per_user AS (SELECT c.contributor_id,
                                             count(DISTINCT c.id)                                   AS contributions_count,
                                             count(DISTINCT repos.project_id)                       AS projects_count,
                                             count(DISTINCT date_trunc('week'::text, c.created_at)) AS contributed_week_count
                                      FROM indexer_exp.contributions c
                                               JOIN LATERAL ( SELECT pgr.*
                                                              FROM project_github_repos pgr
                                                                       JOIN indexer_exp.github_repos gr
                                                                            ON pgr.github_repo_id = gr.id AND gr.visibility = 'PUBLIC'
                                                                       JOIN projects p ON pgr.project_id = p.id AND p.visibility = 'PUBLIC'
                                          ) repos ON repos.github_repo_id = c.repo_id
                                      GROUP BY c.contributor_id),
     ranks AS (SELECT u.github_user_id                                                as github_user_id,
                      rank() OVER (ORDER BY s.contributions_count DESC NULLS LAST)    AS contributions_count,
                      rank() OVER (ORDER BY s.contributed_week_count DESC NULLS LAST) AS contributed_week_count,
                      rank() OVER (ORDER BY s.projects_count DESC NULLS LAST)         AS projects_count
               FROM iam.all_users u
                        LEFT JOIN contributions_stats_per_user s ON s.contributor_id = u.github_user_id)
SELECT r.github_user_id                                                                                      AS github_user_id,
       rank() over (order by 2 * r.contributions_count + r.contributed_week_count + 0.05 * r.projects_count) AS rank,
       stats.contributions_count                                                                             AS contributions_count,
       stats.contributed_week_count                                                                          AS contributed_week_count,
       stats.projects_count                                                                                  AS projects_count
FROM ranks r
         LEFT JOIN contributions_stats_per_user stats ON stats.contributor_id = r.github_user_id
         JOIN indexer_exp.github_accounts ga on ga.id = r.github_user_id and ga.type = 'USER'
ORDER BY rank;

-- ########################################################################
create view users_rank_per_reward_received as
WITH rewards_stats_per_recipient AS (SELECT r.recipient_id,
                                            count(DISTINCT r.project_id)                              AS project_count,
                                            count(r.id)                                               AS reward_count,
                                            round(sum(rsd.amount_usd_equivalent), 2)                  AS usd_total,
                                            count(DISTINCT date_trunc('month'::text, r.requested_at)) AS rewarded_month_count
                                     FROM rewards r
                                              JOIN accounting.reward_status_data rsd ON r.id = rsd.reward_id
                                     GROUP BY r.recipient_id),
     ranks AS (SELECT u.github_user_id                                               AS github_user_id,
                      rank() OVER (ORDER BY rr.reward_count DESC NULLS LAST)         AS reward_count,
                      rank() OVER (ORDER BY rr.usd_total DESC NULLS LAST)            AS usd_total,
                      rank() OVER (ORDER BY rr.rewarded_month_count DESC NULLS LAST) AS rewarded_month_count,
                      rank() OVER (ORDER BY rr.project_count DESC NULLS LAST)        AS project_count
               FROM iam.all_users u
                        LEFT JOIN rewards_stats_per_recipient rr ON rr.recipient_id = u.github_user_id)
SELECT r.github_user_id                                                                                       AS github_user_id,
       rank()
       over (order by 5 * r.usd_total + 2 * r.rewarded_month_count + r.reward_count + 0.05 * r.project_count) AS rank,
       stats.usd_total                                                                                        AS usd_total,
       stats.rewarded_month_count                                                                             AS rewarded_month_count,
       stats.reward_count                                                                                     AS reward_count,
       stats.project_count                                                                                    AS project_count
FROM ranks r
         JOIN rewards_stats_per_recipient stats ON stats.recipient_id = r.github_user_id
         JOIN indexer_exp.github_accounts ga ON ga.id = r.github_user_id AND ga.type = 'USER'
ORDER BY rank;

-- ########################################################################
create view users_rank_per_reward_sent as
WITH rewards_stats_per_requestor AS (SELECT r.requestor_id,
                                            count(DISTINCT r.project_id)                                     AS project_count,
                                            count(DISTINCT r.recipient_id)                                   AS rewarded_user_count,
                                            round(sum(rsd.amount_usd_equivalent), 2)                         AS usd_total,
                                            count(DISTINCT date_trunc('month'::text, r.requested_at))
                                            FILTER ( WHERE date_trunc('month', r.requested_at) >
                                                           date_trunc('month', now() - interval '6 month') ) AS rewarded_month_count
                                     FROM rewards r
                                              JOIN accounting.reward_status_data rsd ON r.id = rsd.reward_id
                                     GROUP BY r.requestor_id),
     ranks AS (SELECT u.user_id                                                      AS user_id,
                      u.github_user_id                                               AS github_user_id,
                      rank() OVER (ORDER BY rr.usd_total DESC NULLS LAST)            AS usd_total,
                      rank() OVER (ORDER BY rr.rewarded_user_count DESC NULLS LAST)  AS rewarded_user_count,
                      rank() OVER (ORDER BY rr.rewarded_month_count DESC NULLS LAST) AS rewarded_month_count,
                      rank() OVER (ORDER BY rr.project_count DESC NULLS LAST)        AS project_count
               FROM iam.all_users u
                        LEFT JOIN rewards_stats_per_requestor rr ON rr.requestor_id = u.user_id)
SELECT r.github_user_id                              AS github_user_id,
       rank() over (order by 5 * r.usd_total + 3 * r.rewarded_user_count + 2 * r.rewarded_month_count +
                             0.05 * r.project_count) AS rank,
       stats.usd_total                               AS usd_total,
       stats.rewarded_user_count                     AS rewarded_user_count,
       stats.rewarded_month_count                    AS rewarded_month_count,
       stats.project_count                           AS project_count
FROM ranks r
         JOIN rewards_stats_per_requestor stats ON stats.requestor_id = r.user_id
ORDER BY rank;

-- ########################################################################
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
       r.*
from normalized_ranks r
order by rank
;

CREATE UNIQUE INDEX global_users_ranks_github_user_id_uindex
    ON global_users_ranks (github_user_id);