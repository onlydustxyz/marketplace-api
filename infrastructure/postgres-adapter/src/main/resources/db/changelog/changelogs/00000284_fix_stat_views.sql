-- Drop all depending objects
DROP VIEW united_stats_per_language_per_user;
DROP VIEW users_languages_ranks;

-- recreate faulty views
DROP MATERIALIZED VIEW received_rewards_stats_per_language_per_user;
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
         JOIN LATERAL (SELECT distinct language_id
                       from language_file_extensions lfe
                       WHERE lfe.extension = ANY (r.main_file_extensions)) lfe on true
         JOIN accounting.reward_statuses rs ON rs.reward_id = r.id
GROUP BY lfe.language_id, r.recipient_id
;
CREATE UNIQUE INDEX received_rewards_stats_per_language_per_user_pk ON received_rewards_stats_per_language_per_user (language_id, recipient_id);
CREATE UNIQUE INDEX received_rewards_stats_per_language_per_user_rpk ON received_rewards_stats_per_language_per_user (recipient_id, language_id);
REFRESH MATERIALIZED VIEW received_rewards_stats_per_language_per_user;



drop materialized view contributions_stats_per_language_per_user;
create materialized view contributions_stats_per_language_per_user as
SELECT c.contributor_id,
       lfe.language_id,
       count(DISTINCT c.id)                                                                       AS contribution_count,
       array_agg(DISTINCT unnested.project_ids)                                                   AS project_ids,
       array_length(array_agg(DISTINCT unnested.project_ids), 1)                                  AS project_count,
       count(DISTINCT date_trunc('week'::text, c.created_at))                                     AS contributed_week_count,
       count(DISTINCT c.id) FILTER (WHERE c.type = 'CODE_REVIEW'::indexer_exp.contribution_type)  AS code_review_count,
       count(DISTINCT c.id) FILTER (WHERE c.type = 'ISSUE'::indexer_exp.contribution_type)        AS issue_count,
       count(DISTINCT c.id) FILTER (WHERE c.type = 'PULL_REQUEST'::indexer_exp.contribution_type) AS pull_request_count
FROM public_contributions c
         CROSS JOIN LATERAL unnest(c.project_ids) unnested(project_ids)
         JOIN LATERAL (SELECT distinct language_id
                       from language_file_extensions lfe
                       WHERE lfe.extension = ANY (c.main_file_extensions)) lfe on true
GROUP BY lfe.language_id, c.contributor_id;

create unique index contributions_stats_per_language_per_user_pk
    on contributions_stats_per_language_per_user (language_id, contributor_id);

create unique index contributions_stats_per_language_per_user_rpk
    on contributions_stats_per_language_per_user (contributor_id, language_id);

-- refresh the materialized views
REFRESH MATERIALIZED VIEW received_rewards_stats_per_language_per_user;
REFRESH MATERIALIZED VIEW contributions_stats_per_language_per_user;

-- recreate the depending objects
CREATE VIEW users_languages_ranks AS
select stats.language_id                                                                       as language_id,
       stats.contributor_id                                                                    as contributor_id,
       rank()
       OVER (PARTITION BY stats.language_id ORDER BY stats.contribution_count DESC NULLS LAST) as rank,
       percent_rank()
       OVER (PARTITION BY stats.language_id ORDER BY stats.contribution_count DESC NULLS LAST) as rank_percentile
from contributions_stats_per_language_per_user stats
;

CREATE OR REPLACE VIEW united_stats_per_language_per_user AS
SELECT coalesce(c.contributor_id, r.recipient_id, ranks.contributor_id) as github_user_id,
       l.id                                                             as language_id,
       coalesce(c.contribution_count, 0)                                as contribution_count,
       coalesce(r.reward_count, 0)                                      as reward_count,
       coalesce(r.pending_request_reward_count, 0)                      as pending_request_reward_count,
       coalesce(r.usd_total, 0)                                         as usd_total,
       coalesce(r.max_usd, 0)                                           as max_usd,
       ranks.rank                                                       as rank,
       l.name                                                           as language_name
FROM contributions_stats_per_language_per_user c
         FULL JOIN received_rewards_stats_per_language_per_user r
                   ON r.recipient_id = c.contributor_id AND r.language_id = c.language_id
         FULL JOIN users_languages_ranks ranks
                   ON ranks.contributor_id = coalesce(c.contributor_id, r.recipient_id) AND
                      ranks.language_id = coalesce(c.language_id, r.language_id)
         JOIN languages l ON l.id = coalesce(c.language_id, r.language_id, ranks.language_id);
