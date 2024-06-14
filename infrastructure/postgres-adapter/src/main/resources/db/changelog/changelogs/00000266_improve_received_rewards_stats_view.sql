DROP MATERIALIZED VIEW global_users_ranks;
DROP VIEW users_rank_per_reward_received;
DROP MATERIALIZED VIEW received_rewards_stats_per_user;
DROP MATERIALIZED VIEW received_rewards_stats_per_ecosystem_per_user;
DROP MATERIALIZED VIEW received_rewards_stats_per_language_per_user;
DROP MATERIALIZED VIEW received_rewards_stats_per_project_per_user;



CREATE MATERIALIZED VIEW received_rewards_stats_per_user AS
SELECT r.recipient_id,
       count(DISTINCT r.id)                                                AS reward_count,
       count(DISTINCT r.project_id)                                        AS project_count,
       round(sum(r.amount_usd_equivalent), 2)                              AS usd_total,
       count(DISTINCT date_trunc('month'::text, r.requested_at))           AS rewarded_month_count,
       array_agg(DISTINCT r.project_id)                                    AS project_ids,
       round(max(r.amount_usd_equivalent), 2)                              AS max_usd,
       count(DISTINCT r.id) FILTER ( WHERE rs.status = 'PENDING_REQUEST' ) AS pending_request_reward_count
FROM public_received_rewards r
         JOIN accounting.reward_statuses rs ON rs.reward_id = r.id
GROUP BY r.recipient_id
;
CREATE UNIQUE INDEX received_rewards_stats_per_user_pk ON received_rewards_stats_per_user (recipient_id);
REFRESH MATERIALIZED VIEW received_rewards_stats_per_user;



CREATE MATERIALIZED VIEW received_rewards_stats_per_ecosystem_per_user AS
SELECT r.recipient_id,
       pe.ecosystem_id,
       count(DISTINCT r.id)                                                AS reward_count,
       count(DISTINCT r.project_id)                                        AS project_count,
       round(sum(r.amount_usd_equivalent), 2)                              AS usd_total,
       count(DISTINCT date_trunc('month'::text, r.requested_at))           AS rewarded_month_count,
       array_agg(DISTINCT r.project_id)                                    AS project_ids,
       round(max(r.amount_usd_equivalent), 2)                              AS max_usd,
       count(DISTINCT r.id) FILTER ( WHERE rs.status = 'PENDING_REQUEST' ) AS pending_request_reward_count
FROM public_received_rewards r
         JOIN projects_ecosystems pe
              on pe.project_id = r.project_id
         JOIN accounting.reward_statuses rs ON rs.reward_id = r.id
GROUP BY pe.ecosystem_id, r.recipient_id
;
CREATE UNIQUE INDEX received_rewards_stats_per_ecosystem_per_user_pk ON received_rewards_stats_per_ecosystem_per_user (ecosystem_id, recipient_id);
CREATE UNIQUE INDEX received_rewards_stats_per_ecosystem_per_user_rpk ON received_rewards_stats_per_ecosystem_per_user (recipient_id, ecosystem_id);
REFRESH MATERIALIZED VIEW received_rewards_stats_per_ecosystem_per_user;



CREATE MATERIALIZED VIEW received_rewards_stats_per_language_per_user AS
SELECT r.recipient_id,
       lfe.language_id,
       count(DISTINCT r.id)                                                AS reward_count,
       count(DISTINCT r.project_id)                                        AS project_count,
       round(sum(r.amount_usd_equivalent), 2)                              AS usd_total,
       count(DISTINCT date_trunc('month'::text, r.requested_at))           AS rewarded_month_count,
       array_agg(DISTINCT r.project_id)                                    AS project_ids,
       round(max(r.amount_usd_equivalent), 2)                              AS max_usd,
       count(DISTINCT r.id) FILTER ( WHERE rs.status = 'PENDING_REQUEST' ) AS pending_request_reward_count
FROM public_received_rewards r
         JOIN language_file_extensions lfe
              on lfe.extension = any (r.main_file_extensions)
         JOIN accounting.reward_statuses rs ON rs.reward_id = r.id
GROUP BY lfe.language_id, r.recipient_id
;
CREATE UNIQUE INDEX received_rewards_stats_per_language_per_user_pk ON received_rewards_stats_per_language_per_user (language_id, recipient_id);
CREATE UNIQUE INDEX received_rewards_stats_per_language_per_user_rpk ON received_rewards_stats_per_language_per_user (recipient_id, language_id);
REFRESH MATERIALIZED VIEW received_rewards_stats_per_language_per_user;



CREATE MATERIALIZED VIEW received_rewards_stats_per_project_per_user AS
SELECT r.recipient_id,
       r.project_id,
       coalesce((select array_agg(distinct pe.ecosystem_id) filter (where pe.ecosystem_id is not null)
                 from projects_ecosystems pe
                 where pe.project_id = r.project_id), '{}')                AS ecosystem_ids,
       count(r.id)                                                         AS reward_count,
       round(sum(r.amount_usd_equivalent), 2)                              AS usd_total,
       count(DISTINCT date_trunc('month'::text, r.requested_at))           AS rewarded_month_count,
       round(max(r.amount_usd_equivalent), 2)                              AS max_usd,
       count(DISTINCT r.id) FILTER ( WHERE rs.status = 'PENDING_REQUEST' ) AS pending_request_reward_count
FROM public_received_rewards r
         JOIN accounting.reward_statuses rs ON rs.reward_id = r.id
GROUP BY r.project_id, r.recipient_id
;
CREATE UNIQUE INDEX received_rewards_stats_per_project_per_user_pk ON received_rewards_stats_per_project_per_user (project_id, recipient_id);
CREATE UNIQUE INDEX received_rewards_stats_per_project_per_user_rpk ON received_rewards_stats_per_project_per_user (recipient_id, project_id);
REFRESH MATERIALIZED VIEW received_rewards_stats_per_project_per_user;



-- ##################################################################################################
-- re-create existing views as-is
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
select rank() over (order by 100 * r.contributions_rank +
                             2 * r.rewards_sent_rank +
                             1.5 * r.rewards_received_rank)
                             as rank,
       cume_dist() over (order by 100 * r.contributions_rank +
                                  2 * r.rewards_sent_rank +
                                  1.5 * r.rewards_received_rank)
                             as rank_percentile,
       r.*,
       coalesce(lp.count, 0) as leaded_project_count
from normalized_ranks r
         left join leaded_projects lp on lp.github_user_id = r.github_user_id
order by rank
;
CREATE UNIQUE INDEX global_users_ranks_pk ON global_users_ranks (github_user_id);
REFRESH MATERIALIZED VIEW global_users_ranks;



-- ##################################################################################################
-- New RSQL-specific views
CREATE VIEW united_stats_per_language_per_user AS
SELECT coalesce(c.contributor_id, r.recipient_id, ranks.contributor_id) as github_user_id,
       coalesce(c.language_id, r.language_id, ranks.language_id)        as language_id,
       c.contribution_count,
       r.reward_count,
       r.pending_request_reward_count,
       r.usd_total,
       r.max_usd,
       ranks.rank
FROM contributions_stats_per_language_per_user c
         FULL JOIN received_rewards_stats_per_language_per_user r
                   ON r.recipient_id = c.contributor_id AND r.language_id = c.language_id
         FULL JOIN users_languages_ranks ranks
                   ON ranks.contributor_id = coalesce(c.contributor_id, r.recipient_id) AND
                      ranks.language_id = coalesce(c.language_id, r.language_id);



CREATE VIEW united_stats_per_ecosystem_per_user AS
SELECT coalesce(c.contributor_id, r.recipient_id, ranks.contributor_id) as github_user_id,
       coalesce(c.ecosystem_id, r.ecosystem_id, ranks.ecosystem_id)     as ecosystem_id,
       c.contribution_count,
       r.reward_count,
       r.pending_request_reward_count,
       r.usd_total,
       r.max_usd,
       ranks.rank
FROM contributions_stats_per_ecosystem_per_user c
         FULL JOIN received_rewards_stats_per_ecosystem_per_user r
                   ON r.recipient_id = c.contributor_id AND r.ecosystem_id = c.ecosystem_id
         FULL JOIN users_ecosystems_ranks ranks
                   ON ranks.contributor_id = coalesce(c.contributor_id, r.recipient_id) AND
                      ranks.ecosystem_id = coalesce(c.ecosystem_id, r.ecosystem_id);



CREATE VIEW united_stats_per_user AS
SELECT coalesce(c.contributor_id, r.recipient_id, ranks.github_user_id) as github_user_id,
       c.contribution_count,
       r.reward_count,
       r.pending_request_reward_count,
       r.usd_total,
       r.max_usd,
       ranks.rank
FROM contributions_stats_per_user c
         FULL JOIN received_rewards_stats_per_user r
                   ON r.recipient_id = c.contributor_id
         FULL JOIN global_users_ranks ranks
                   ON ranks.github_user_id = coalesce(c.contributor_id, r.recipient_id);
