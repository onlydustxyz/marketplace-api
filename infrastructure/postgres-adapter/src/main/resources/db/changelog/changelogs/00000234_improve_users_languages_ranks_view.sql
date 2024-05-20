DROP MATERIALIZED VIEW global_users_ranks;
DROP VIEW users_ecosystems_ranks;
DROP VIEW users_languages_ranks;
DROP VIEW users_rank_per_contribution;
DROP VIEW users_rank_per_reward_received;


-- CONTRIBUTIONS #######################################################################################################

CREATE VIEW public_contributions AS
select c.*,
       array_agg(distinct p.id) as project_ids
from indexer_exp.contributions c
         join indexer_exp.github_repos gr on gr.id = c.repo_id and gr.visibility = 'PUBLIC'
         join project_github_repos pgr on pgr.github_repo_id = gr.id
         join projects p on p.id = pgr.project_id and p.visibility = 'PUBLIC'
group by c.id
;



CREATE MATERIALIZED VIEW contributions_stats_per_user AS
SELECT c.contributor_id,
       count(DISTINCT c.id)                                      AS contribution_count,
       array_agg(DISTINCT unnested.project_ids)                  AS project_ids,
       array_length(array_agg(DISTINCT unnested.project_ids), 1) AS project_count,
       count(DISTINCT date_trunc('week'::text, c.created_at))    AS contributed_week_count
FROM public_contributions c
         CROSS JOIN unnest(c.project_ids) unnested(project_ids)
GROUP BY c.contributor_id
;
CREATE UNIQUE INDEX contributions_stats_per_user_pk ON contributions_stats_per_user (contributor_id);
REFRESH MATERIALIZED VIEW contributions_stats_per_user;



CREATE MATERIALIZED VIEW contributions_stats_per_ecosystem_per_user AS
SELECT c.contributor_id,
       pe.ecosystem_id,
       count(DISTINCT c.id)                                   AS contribution_count,
       array_agg(DISTINCT pe.project_id)                      AS project_ids,
       array_length(array_agg(DISTINCT pe.project_id), 1)     AS project_count,
       count(DISTINCT date_trunc('week'::text, c.created_at)) AS contributed_week_count
FROM public_contributions c
         JOIN projects_ecosystems pe
              on pe.project_id = any (c.project_ids)
GROUP BY pe.ecosystem_id, c.contributor_id
;
CREATE UNIQUE INDEX contributions_stats_per_ecosystem_per_user_pk ON contributions_stats_per_ecosystem_per_user (ecosystem_id, contributor_id);
CREATE UNIQUE INDEX contributions_stats_per_ecosystem_per_user_rpk ON contributions_stats_per_ecosystem_per_user (contributor_id, ecosystem_id);
REFRESH MATERIALIZED VIEW contributions_stats_per_ecosystem_per_user;



CREATE MATERIALIZED VIEW contributions_stats_per_language_per_user AS
SELECT c.contributor_id,
       lfe.language_id,
       count(DISTINCT c.id)                                      AS contribution_count,
       array_agg(DISTINCT unnested.project_ids)                  AS project_ids,
       array_length(array_agg(DISTINCT unnested.project_ids), 1) AS project_count,
       count(DISTINCT date_trunc('week'::text, c.created_at))    AS contributed_week_count
FROM public_contributions c
         CROSS JOIN unnest(c.project_ids) unnested(project_ids)
         JOIN language_file_extensions lfe
              on lfe.extension = any (c.main_file_extensions)
GROUP BY lfe.language_id, c.contributor_id
;
CREATE UNIQUE INDEX contributions_stats_per_language_per_user_pk ON contributions_stats_per_language_per_user (language_id, contributor_id);
CREATE UNIQUE INDEX contributions_stats_per_language_per_user_rpk ON contributions_stats_per_language_per_user (contributor_id, language_id);
REFRESH MATERIALIZED VIEW contributions_stats_per_language_per_user;



CREATE VIEW users_rank_per_contribution as
WITH ranks AS (SELECT u.github_user_id                                                AS github_user_id,
                      rank() OVER (ORDER BY s.contribution_count DESC NULLS LAST)     AS contribution_count,
                      rank() OVER (ORDER BY s.contributed_week_count DESC NULLS LAST) AS contributed_week_count,
                      rank() OVER (ORDER BY s.project_count DESC NULLS LAST)          AS project_count
               FROM iam.all_users u
                        LEFT JOIN contributions_stats_per_user s ON s.contributor_id = u.github_user_id)
SELECT r.github_user_id                                                                                    AS github_user_id,
       rank() over (order by 2 * r.contribution_count + r.contributed_week_count + 0.05 * r.project_count) AS rank,
       stats.contribution_count                                                                            AS contributions_count,
       stats.contributed_week_count                                                                        AS contributed_week_count,
       stats.project_count                                                                                 AS projects_count,
       stats.project_ids                                                                                   AS project_ids
FROM ranks r
         LEFT JOIN contributions_stats_per_user stats ON stats.contributor_id = r.github_user_id
         JOIN indexer_exp.github_accounts ga on ga.id = r.github_user_id and ga.type = 'USER'
;



CREATE VIEW users_languages_ranks AS
select stats.language_id                                                                       as language_id,
       stats.contributor_id                                                                    as contributor_id,
       rank()
       OVER (PARTITION BY stats.language_id ORDER BY stats.contribution_count DESC NULLS LAST) as rank,
       percent_rank()
       OVER (PARTITION BY stats.language_id ORDER BY stats.contribution_count DESC NULLS LAST) as rank_percentile
from contributions_stats_per_language_per_user stats
;



CREATE VIEW users_ecosystems_ranks AS
select stats.ecosystem_id                                                                       as ecosystem_id,
       stats.contributor_id                                                                     as contributor_id,
       rank()
       OVER (PARTITION BY stats.ecosystem_id ORDER BY stats.contribution_count DESC NULLS LAST) as rank,
       percent_rank()
       OVER (PARTITION BY stats.ecosystem_id ORDER BY stats.contribution_count DESC NULLS LAST) as rank_percentile
from contributions_stats_per_ecosystem_per_user stats
;


-- REWARDS #############################################################################################################

CREATE VIEW public_received_rewards AS
select r.*,
       rsd.amount_usd_equivalent,
       rsd.usd_conversion_rate,
       rsd.invoice_received_at,
       rsd.paid_at,
       rsd.networks,
       coalesce(array_agg(distinct unnested.main_file_extensions)
                filter (where unnested.main_file_extensions is not null), '{}') as main_file_extensions
from rewards r
         join reward_items ri on ri.reward_id = r.id
         join projects p on p.id = r.project_id and p.visibility = 'PUBLIC'
         join accounting.reward_status_data rsd ON rsd.reward_id = r.id
         left join indexer_exp.contributions c
                   on ((CAST(c.pull_request_id AS TEXT) = ri.id
                       or CAST(c.issue_id AS TEXT) = ri.id
                       or c.code_review_id = ri.id)
                       and c.contributor_id = ri.recipient_id)
         left join unnest(c.main_file_extensions) unnested(main_file_extensions) on true
group by r.id, rsd.reward_id;
;


CREATE MATERIALIZED VIEW received_rewards_stats_per_user AS
SELECT r.recipient_id,
       count(r.id)                                               AS reward_count,
       count(DISTINCT r.project_id)                              AS project_count,
       round(sum(r.amount_usd_equivalent), 2)                    AS usd_total,
       count(DISTINCT date_trunc('month'::text, r.requested_at)) AS rewarded_month_count,
       array_agg(distinct r.project_id)                          AS project_ids
FROM public_received_rewards r
GROUP BY r.recipient_id
;
CREATE UNIQUE INDEX received_rewards_stats_per_user_pk ON received_rewards_stats_per_user (recipient_id);
REFRESH MATERIALIZED VIEW received_rewards_stats_per_user;



CREATE MATERIALIZED VIEW received_rewards_stats_per_ecosystem_per_user AS
SELECT r.recipient_id,
       pe.ecosystem_id,
       count(r.id)                                               AS reward_count,
       count(DISTINCT r.project_id)                              AS project_count,
       round(sum(r.amount_usd_equivalent), 2)                    AS usd_total,
       count(DISTINCT date_trunc('month'::text, r.requested_at)) AS rewarded_month_count,
       array_agg(distinct r.project_id)                          AS project_ids
FROM public_received_rewards r
         JOIN projects_ecosystems pe
              on pe.project_id = r.project_id
GROUP BY pe.ecosystem_id, r.recipient_id
;
CREATE UNIQUE INDEX received_rewards_stats_per_ecosystem_per_user_pk ON received_rewards_stats_per_ecosystem_per_user (ecosystem_id, recipient_id);
CREATE UNIQUE INDEX received_rewards_stats_per_ecosystem_per_user_rpk ON received_rewards_stats_per_ecosystem_per_user (recipient_id, ecosystem_id);
REFRESH MATERIALIZED VIEW received_rewards_stats_per_ecosystem_per_user;



CREATE MATERIALIZED VIEW received_rewards_stats_per_language_per_user AS
SELECT r.recipient_id,
       lfe.language_id,
       count(distinct r.id)                                      AS reward_count,
       count(DISTINCT r.project_id)                              AS project_count,
       round(sum(r.amount_usd_equivalent), 2)                    AS usd_total,
       count(DISTINCT date_trunc('month'::text, r.requested_at)) AS rewarded_month_count,
       array_agg(distinct r.project_id)                          AS project_ids
FROM public_received_rewards r
         left JOIN language_file_extensions lfe
                   on lfe.extension = any (r.main_file_extensions)
GROUP BY lfe.language_id, r.recipient_id
;
CREATE UNIQUE INDEX received_rewards_stats_per_language_per_user_pk ON received_rewards_stats_per_language_per_user (language_id, recipient_id);
CREATE UNIQUE INDEX received_rewards_stats_per_language_per_user_rpk ON received_rewards_stats_per_language_per_user (recipient_id, language_id);
REFRESH MATERIALIZED VIEW received_rewards_stats_per_language_per_user;



CREATE MATERIALIZED VIEW received_rewards_stats_per_project_per_user AS
SELECT r.recipient_id,
       r.project_id,
       coalesce(array_agg(distinct pe.ecosystem_id)
                filter (where pe.ecosystem_id is not null), '{}') AS ecosystem_ids,
       count(r.id)                                                AS reward_count,
       round(sum(r.amount_usd_equivalent), 2)                     AS usd_total,
       count(DISTINCT date_trunc('month'::text, r.requested_at))  AS rewarded_month_count
FROM public_received_rewards r
         LEFT JOIN projects_ecosystems pe ON pe.project_id = r.project_id
GROUP BY r.project_id, r.recipient_id
;
CREATE UNIQUE INDEX received_rewards_stats_per_project_per_user_pk ON received_rewards_stats_per_project_per_user (project_id, recipient_id);
CREATE UNIQUE INDEX received_rewards_stats_per_project_per_user_rpk ON received_rewards_stats_per_project_per_user (recipient_id, project_id);
REFRESH MATERIALIZED VIEW received_rewards_stats_per_project_per_user;



CREATE VIEW users_rank_per_reward_received as
WITH ranks AS (SELECT u.github_user_id                                               AS github_user_id,
                      rank() OVER (ORDER BY rr.reward_count DESC NULLS LAST)         AS reward_count,
                      rank() OVER (ORDER BY rr.usd_total DESC NULLS LAST)            AS usd_total,
                      rank() OVER (ORDER BY rr.rewarded_month_count DESC NULLS LAST) AS rewarded_month_count,
                      rank() OVER (ORDER BY rr.project_count DESC NULLS LAST)        AS project_count
               FROM iam.all_users u
                        LEFT JOIN received_rewards_stats_per_user rr ON rr.recipient_id = u.github_user_id)
SELECT r.github_user_id                                                                                       AS github_user_id,
       rank()
       over (order by 5 * r.usd_total + 2 * r.rewarded_month_count + r.reward_count + 0.05 * r.project_count) AS rank,
       stats.usd_total                                                                                        AS usd_total,
       stats.rewarded_month_count                                                                             AS rewarded_month_count,
       stats.reward_count                                                                                     AS reward_count,
       stats.project_count                                                                                    AS project_count
FROM ranks r
         JOIN received_rewards_stats_per_user stats ON stats.recipient_id = r.github_user_id
         JOIN indexer_exp.github_accounts ga ON ga.id = r.github_user_id AND ga.type = 'USER'
ORDER BY rank;


-- RE-CREATE global_users_ranks without any modification
CREATE MATERIALIZED VIEW global_users_ranks AS
with ranks as (select coalesce(c.github_user_id, rr.github_user_id, rs.github_user_id) as github_user_id,
                      rank() over (order by c.rank)                                    as contributions_rank,
                      rank() over (order by rs.rank)                                   as rewards_sent_rank,
                      rank() over (order by rr.rank)                                   as rewards_received_rank,
                      c.contributions_count                                            as contribution_count,
                      c.projects_count                                                 as contributed_project_count,
                      rr.reward_count                                                  as reward_count
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
                                 round(100.0 * r.rewards_received_rank / mr.rewards_received_rank, 2) as rewards_received_rank,
                                 r.contribution_count,
                                 r.contributed_project_count,
                                 r.reward_count
                          from ranks r
                                   join max_ranks mr on true),
     leaded_projects as (select u.github_user_id, count(distinct pl.project_id) as count
                         from project_leads pl
                                  join iam.users u on u.id = pl.user_id
                                  join projects p on p.id = pl.project_id and p.visibility = 'PUBLIC'
                         group by u.github_user_id)
select rank()
       over (order by 100 * r.contributions_rank + 2 * r.rewards_sent_rank + 1.5 * r.rewards_received_rank) as rank,
       percent_rank()
       over (order by 100 * r.contributions_rank + 2 * r.rewards_sent_rank + 1.5 *
                                                                             r.rewards_received_rank)       as rank_percentile,
       r.*,
       lp.count                                                                                             as leaded_project_count
from normalized_ranks r
         left join leaded_projects lp on lp.github_user_id = r.github_user_id
order by rank
;
CREATE UNIQUE INDEX global_users_ranks_pk ON global_users_ranks (github_user_id);
REFRESH MATERIALIZED VIEW global_users_ranks;
