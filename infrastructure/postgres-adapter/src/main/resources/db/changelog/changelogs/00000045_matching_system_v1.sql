--------------------------------------------------------------------------------
-- Helpers ---------------------------------------------------------------------
--------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION sum_func(
    bigint, pg_catalog.anyelement, bigint
)
    RETURNS bigint AS
$body$
SELECT case when $3 is not null then COALESCE($1, 0) + $3 else $1 end
$body$
    LANGUAGE 'sql';

CREATE AGGREGATE dist_sum (
    pg_catalog."any",
    bigint)
    (
    SFUNC = sum_func,
    STYPE = int8
    );



create or replace function common_entries_count(arr1 uuid[], arr2 uuid[])
    returns int
    stable parallel restricted
    language sql
as
$$
SELECT COUNT(DISTINCT a.elem)
FROM unnest(arr1) a(elem)
WHERE a.elem = ANY (arr2);
$$;



CREATE OR REPLACE FUNCTION clamp(
    value anyelement,
    min_val anyelement,
    max_val anyelement
)
    RETURNS anyelement
    LANGUAGE sql
    IMMUTABLE
AS
$$
SELECT LEAST(GREATEST(value, min_val), max_val);
$$;



CREATE OR REPLACE FUNCTION normalize_minmax(
    value anyelement,
    min_val anyelement,
    max_val anyelement
) RETURNS numeric AS
$$
SELECT case when (max_val - min_val) = 0 then 1 else (value - min_val)::numeric / (max_val - min_val)::numeric end;
$$ LANGUAGE sql IMMUTABLE;



--------------------------------------------------------------------------------
-- Metrics ---------------------------------------------------------------------
--------------------------------------------------------------------------------
create or replace function reco.get_project_repo_metrics(projectId uuid)
    returns TABLE
            (
                project_id  uuid,
                repo_count  bigint,
                start_count numeric
            )
    stable parallel restricted
    language sql
as
$$
SELECT pgr.project_id,
       count(gr.id)                     AS repo_count,
       COALESCE(sum(gr.stars_count), 0) AS start_count
FROM project_github_repos pgr
         JOIN indexer_exp.github_repos gr ON gr.id = pgr.github_repo_id AND gr.visibility = 'PUBLIC'::indexer_exp.github_repo_visibility
WHERE pgr.project_id = projectId
GROUP BY pgr.project_id
$$;



create or replace function reco.get_project_reward_metrics(projectId uuid, fromDate timestamptz)
    returns TABLE
            (
                project_id       uuid,
                recipient_count  bigint,
                total_usd_amount numeric
            )
    stable parallel restricted
    language sql
as
$$
SELECT r.project_id,
       count(DISTINCT r.contributor_id) AS recipient_count,
       COALESCE(sum(r.usd_amount), 0)   AS total_usd_amount
FROM bi.p_reward_data r
WHERE r.project_id = projectId
  AND r.timestamp > fromDate
GROUP BY r.project_id
$$;



create or replace function reco.get_project_contributions_metrics(projectId uuid, fromDate timestamptz)
    returns TABLE
            (
                project_id                            uuid,
                done_pr_count                         bigint,
                in_progress_pr_count                  bigint,
                done_issue_count                      bigint,
                assigned_issue_count                  bigint,
                not_assigned_issue_count              bigint,
                done_gfi_count                        bigint,
                assigned_gfi_count                    bigint,
                not_assigned_gfi_count                bigint,
                contributor_count                     bigint,
                total_contributors_contribution_count bigint
            )
    stable parallel restricted
    language sql
as
$$
WITH contribution_count_per_contributor AS
         (select c.contributor_id, count(c.contribution_uuid) as contribution_count
          from bi.p_per_contributor_contribution_data c
          group by c.contributor_id)
SELECT c.project_id,
       COALESCE(count(distinct c.contribution_uuid)
                FILTER (WHERE c.contribution_type = 'PULL_REQUEST' AND c.activity_status = 'DONE'), 0)                            AS done_pr_count,
       COALESCE(count(distinct c.contribution_uuid)
                FILTER (WHERE c.contribution_type = 'PULL_REQUEST' AND c.activity_status = 'IN_PROGRESS'), 0)                     AS in_progress_pr_count,
       COALESCE(count(distinct c.contribution_uuid)
                FILTER (WHERE c.contribution_type = 'ISSUE' AND c.activity_status = 'DONE'), 0)                                   AS done_issue_count,
       COALESCE(count(distinct c.contribution_uuid)
                FILTER (WHERE c.contribution_type = 'ISSUE' AND c.activity_status = 'IN_PROGRESS'), 0)                            AS assigned_issue_count,
       COALESCE(count(distinct c.contribution_uuid)
                FILTER (WHERE c.contribution_type = 'ISSUE' AND c.activity_status = 'NOT_ASSIGNED'), 0)                           AS not_assigned_issue_count,
       COALESCE(count(distinct c.contribution_uuid)
                FILTER (WHERE c.contribution_type = 'ISSUE' AND c.is_good_first_issue AND c.activity_status = 'DONE'), 0)         AS done_gfi_count,
       COALESCE(count(distinct c.contribution_uuid)
                FILTER (WHERE c.contribution_type = 'ISSUE' AND c.is_good_first_issue AND c.activity_status = 'IN_PROGRESS'), 0)  AS assigned_gfi_count,
       COALESCE(count(distinct c.contribution_uuid)
                FILTER (WHERE c.contribution_type = 'ISSUE' AND c.is_good_first_issue AND c.activity_status = 'NOT_ASSIGNED'), 0) AS not_assigned_gfi_count,
       COALESCE(count(distinct cd.contributor_id), 0)                                                                             AS contributor_count,
       COALESCE(dist_sum(distinct ccpc.contributor_id, ccpc.contribution_count), 0)                                               AS total_contributors_contribution_count
FROM bi.p_contribution_data c
         LEFT JOIN bi.p_contribution_contributors_data cc ON cc.contribution_uuid = c.contribution_uuid
         LEFT JOIN unnest(cc.contributor_ids) cd(contributor_id) ON TRUE
         LEFT JOIN contribution_count_per_contributor ccpc ON ccpc.contributor_id = cd.contributor_id
WHERE c.project_id = projectId
  AND c.timestamp > fromDate
GROUP BY c.project_id
$$;



CREATE OR REPLACE VIEW reco.projects_computed_data AS
SELECT p.project_id,
       p.created_at,
       p.language_ids,
       p.ecosystem_ids,
       p.project_category_ids,
       coalesce(b.available_budget_usd, 0)                                                       AS available_budget_usd,
       coalesce(prm.repo_count, 0)                                                               AS repo_count,
       coalesce(prm.start_count, 0)                                                              AS start_count,
       coalesce(rm.recipient_count, 0)                                                           AS reward_recipient_count,
       coalesce(rm.total_usd_amount, 0)                                                          AS reward_total_usd_amount,
       coalesce(rm_last_week.recipient_count, 0)                                                 AS reward_recipient_count_last_week,
       coalesce(rm_last_week.total_usd_amount, 0)                                                AS reward_total_usd_amount_last_week,
       coalesce(rm_last_month.recipient_count, 0)                                                AS reward_recipient_count_last_month,
       coalesce(rm_last_month.total_usd_amount, 0)                                               AS reward_total_usd_amount_last_month,
       coalesce(rm_last_3months.recipient_count, 0)                                              AS reward_recipient_count_last_3months,
       coalesce(rm_last_3months.total_usd_amount, 0)                                             AS reward_total_usd_amount_last_3months,
       coalesce(cm.done_pr_count, 0)                                                             AS done_pr_count,
       coalesce(cm.in_progress_pr_count, 0)                                                      AS in_progress_pr_count,
       coalesce(cm.done_issue_count, 0)                                                          AS done_issue_count,
       coalesce(cm.assigned_issue_count, 0)                                                      AS assigned_issue_count,
       coalesce(cm.not_assigned_issue_count, 0)                                                  AS not_assigned_issue_count,
       coalesce(cm.done_gfi_count, 0)                                                            AS done_gfi_count,
       coalesce(cm.assigned_gfi_count, 0)                                                        AS assigned_gfi_count,
       coalesce(cm.not_assigned_gfi_count, 0)                                                    AS not_assigned_gfi_count,
       coalesce(cm.contributor_count, 0)                                                         AS contributor_count,
       coalesce(cm.total_contributors_contribution_count / greatest(cm.contributor_count, 1), 0) AS avg_contribution_count_per_contributor,
       coalesce(cm_last_week.done_pr_count, 0)                                                   AS done_pr_count_last_week,
       coalesce(cm_last_week.in_progress_pr_count, 0)                                            AS in_progress_pr_count_last_week,
       coalesce(cm_last_week.done_issue_count, 0)                                                AS done_issue_count_last_week,
       coalesce(cm_last_week.assigned_issue_count, 0)                                            AS assigned_issue_count_last_week,
       coalesce(cm_last_week.not_assigned_issue_count, 0)                                        AS not_assigned_issue_count_last_week,
       coalesce(cm_last_week.done_gfi_count, 0)                                                  AS done_gfi_count_last_week,
       coalesce(cm_last_week.assigned_gfi_count, 0)                                              AS assigned_gfi_count_last_week,
       coalesce(cm_last_week.not_assigned_gfi_count, 0)                                          AS not_assigned_gfi_count_last_week,
       coalesce(cm_last_week.contributor_count, 0)                                               AS contributor_count_last_week,
       coalesce(cm_last_week.total_contributors_contribution_count / greatest(cm_last_week.contributor_count, 1),
                0)                                                                               AS avg_contribution_count_per_contributor_last_week,
       coalesce(cm_last_month.done_pr_count, 0)                                                  AS done_pr_count_last_month,
       coalesce(cm_last_month.in_progress_pr_count, 0)                                           AS in_progress_pr_count_last_month,
       coalesce(cm_last_month.done_issue_count, 0)                                               AS done_issue_count_last_month,
       coalesce(cm_last_month.assigned_issue_count, 0)                                           AS assigned_issue_count_last_month,
       coalesce(cm_last_month.not_assigned_issue_count, 0)                                       AS not_assigned_issue_count_last_month,
       coalesce(cm_last_month.done_gfi_count, 0)                                                 AS done_gfi_count_last_month,
       coalesce(cm_last_month.assigned_gfi_count, 0)                                             AS assigned_gfi_count_last_month,
       coalesce(cm_last_month.not_assigned_gfi_count, 0)                                         AS not_assigned_gfi_count_last_month,
       coalesce(cm_last_month.contributor_count, 0)                                              AS contributor_count_last_month,
       coalesce(cm_last_month.total_contributors_contribution_count / greatest(cm_last_month.contributor_count, 1),
                0)                                                                               AS avg_contribution_count_per_contributor_last_month,
       coalesce(cm_last_3months.done_pr_count, 0)                                                AS done_pr_count_last_3months,
       coalesce(cm_last_3months.in_progress_pr_count, 0)                                         AS in_progress_pr_count_last_3months,
       coalesce(cm_last_3months.done_issue_count, 0)                                             AS done_issue_count_last_3months,
       coalesce(cm_last_3months.assigned_issue_count, 0)                                         AS assigned_issue_count_last_3months,
       coalesce(cm_last_3months.not_assigned_issue_count, 0)                                     AS not_assigned_issue_count_last_3months,
       coalesce(cm_last_3months.done_gfi_count, 0)                                               AS done_gfi_count_last_3months,
       coalesce(cm_last_3months.assigned_gfi_count, 0)                                           AS assigned_gfi_count_last_3months,
       coalesce(cm_last_3months.not_assigned_gfi_count, 0)                                       AS not_assigned_gfi_count_last_3months,
       coalesce(cm_last_3months.contributor_count, 0)                                            AS contributor_count_last_3months,
       coalesce(cm_last_3months.total_contributors_contribution_count / greatest(cm_last_3months.contributor_count, 1),
                0)                                                                               AS avg_contribution_count_per_contributor_last_3months
FROM bi.p_project_global_data p
         LEFT JOIN bi.p_project_budget_data b ON p.project_id = b.project_id
         LEFT JOIN reco.get_project_repo_metrics(p.project_id) prm ON true
         LEFT JOIN reco.get_project_reward_metrics(p.project_id, '2010-01-01'::timestamptz) rm ON true
         LEFT JOIN reco.get_project_reward_metrics(p.project_id, now() - '1 week'::interval) rm_last_week ON true
         LEFT JOIN reco.get_project_reward_metrics(p.project_id, now() - '1 month'::interval) rm_last_month ON true
         LEFT JOIN reco.get_project_reward_metrics(p.project_id, now() - '3 month'::interval) rm_last_3months ON true
         LEFT JOIN reco.get_project_contributions_metrics(p.project_id, '2010-01-01'::timestamptz) cm ON true
         LEFT JOIN reco.get_project_contributions_metrics(p.project_id, now() - '1 week'::interval) cm_last_week ON true
         LEFT JOIN reco.get_project_contributions_metrics(p.project_id, now() - '1 month'::interval) cm_last_month ON true
         LEFT JOIN reco.get_project_contributions_metrics(p.project_id, now() - '3 month'::interval) cm_last_3months ON true
;



CREATE DOMAIN reco.skill_level AS integer
    CHECK (VALUE >= 1 AND VALUE <= 4);

CREATE TABLE reco.project_skill_levels
(
    project_id uuid PRIMARY KEY,
    level      reco.skill_level
);



CREATE OR REPLACE VIEW reco.projects_extrapolated_data AS
WITH raw_scores AS (SELECT p.project_id,
                           -- Raw scores
                           clamp(now()::date - p.created_at::date, 0, 500) +
                           5 * clamp(p.contributor_count_last_3months::int, 0, 100) +
                           clamp(p.start_count::int, 0, 1000)                                                 AS well_established_score,

                           -clamp(now()::date - p.created_at::date, 0, 100) +
                           100 * p.contributor_count::float / (1 + p.contributor_count::float -
                                                               p.contributor_count_last_month::float)         AS emerging_score,

                           p.contributor_count_last_3months / clamp(now()::date - p.created_at::date, 1, 100) AS active_community_score,
                           psl.level                                                                          AS skill_level
                    FROM reco.projects_computed_data p
                             LEFT JOIN reco.project_skill_levels psl on p.project_id = psl.project_id)
SELECT project_id,
       -- Raw scores
       well_established_score,
       emerging_score,
       active_community_score,
       skill_level,
       -- Normalized scores (0-1 range)
       normalize_minmax(
               well_established_score,
               MIN(well_established_score) OVER (),
               MAX(well_established_score) OVER ()
       ) AS well_established_score_normalized,
       normalize_minmax(
               emerging_score,
               MIN(emerging_score) OVER (),
               MAX(emerging_score) OVER ()
       ) AS emerging_score_normalized,
       normalize_minmax(
               active_community_score,
               MIN(active_community_score) OVER (),
               MAX(active_community_score) OVER ()
       ) AS active_community_score_normalized
FROM raw_scores;



CREATE OR REPLACE VIEW reco.user_answers_to_projects_data AS
WITH raw_scores AS (select p.project_id,
                           ua.user_id,
                           common_entries_count(p.language_ids, ua.languages)   as common_languages_count,
                           common_entries_count(p.ecosystem_ids, ua.ecosystems) as common_ecosystems_count,
                           coalesce(ua.experience_level - pe.skill_level, 2)    as experience_level_diff
                    from bi.p_project_global_data p
                             join reco.projects_extrapolated_data pe on p.project_id = pe.project_id
                             cross join reco.user_answers_v1 ua)
SELECT project_id,
       user_id,
       -- Raw scores
       common_languages_count,
       common_ecosystems_count,
       experience_level_diff,
       -- Normalized scores (0-1 range)
       normalize_minmax(
               common_languages_count,
               MIN(common_languages_count) OVER (),
               MAX(common_languages_count) OVER ()
       ) AS common_languages_count_normalized,
       normalize_minmax(
               common_ecosystems_count,
               MIN(common_ecosystems_count) OVER (),
               MAX(common_ecosystems_count) OVER ()
       ) AS common_ecosystems_count_normalized,
       normalize_minmax(
               experience_level_diff,
               MIN(experience_level_diff) OVER (),
               MAX(experience_level_diff) OVER ()
       ) AS experience_level_diff_normalized
FROM raw_scores;



