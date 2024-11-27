drop view project_stats_for_ranking_computation;

create view project_stats_for_ranking_computation
as
WITH reward_stats AS (SELECT r.project_id,
                             count(DISTINCT r.contributor_id) AS distinct_recipient_number_last_1_month,
                             COALESCE(sum(r.usd_amount), 0)   AS total_dollars_equivalent_spent_last_1_month
                      FROM bi.p_reward_data r
                      WHERE r.timestamp > (now() - '1 mon'::interval)
                      GROUP BY r.project_id),
     contribution_stats AS (SELECT c.project_id,
                                   COALESCE(sum(1) FILTER (WHERE c.contribution_type = 'PULL_REQUEST'), 0)                    AS pr_count,
                                   COALESCE(sum(1) FILTER (WHERE c.contribution_type = 'PULL_REQUEST' AND
                                                                 c.created_at > (now() - '3 mons'::interval)), 0)             AS pr_count_last_3_months,
                                   COALESCE(sum(1) FILTER (WHERE c.contribution_type = 'PULL_REQUEST' AND
                                                                 c.contribution_status = 'IN_PROGRESS'), 0)                   AS open_pr_count,
                                   COALESCE(sum(1) FILTER (WHERE c.contribution_type = 'ISSUE'), 0)                           AS issue_count,
                                   COALESCE(sum(1) FILTER (WHERE c.contribution_type = 'ISSUE' AND
                                                                 c.created_at > (now() - '3 mons'::interval)), 0)             AS issue_count_last_3_months,
                                   COALESCE(sum(1) FILTER (WHERE c.contribution_type = 'ISSUE' AND
                                                                 c.contribution_status = 'IN_PROGRESS'), 0)                   AS open_issue_count,
                                   COALESCE(sum(1) FILTER (WHERE c.contribution_type = 'CODE_REVIEW'), 0)                     AS cr_count,
                                   COALESCE(sum(1) FILTER (WHERE c.contribution_type = 'CODE_REVIEW' AND
                                                                 c.created_at > (now() - '3 mons'::interval)), 0)             AS cr_count_last_3_months,
                                   COALESCE(sum(1) FILTER (WHERE c.contribution_type = 'CODE_REVIEW' AND
                                                                 c.contribution_status = 'IN_PROGRESS'), 0)                   AS open_cr_count,
                                   count(DISTINCT c.contributor_id) FILTER (WHERE c.created_at > (now() - '1 mon'::interval)) AS contributor_count
                            FROM bi.p_per_contributor_contribution_data c
                            GROUP BY c.project_id)
SELECT pd2.id                                                      AS project_id,
       pd2.created_at                                              AS project_created_at,
       COALESCE(cs.pr_count, 0)                                    AS pr_count,
       COALESCE(cs.pr_count_last_3_months, 0)                      AS pr_count_last_3_months,
       COALESCE(cs.open_pr_count, 0)                               AS open_pr_count,
       COALESCE(cs.issue_count, 0)                                 AS issue_count,
       COALESCE(cs.issue_count_last_3_months, 0)                   AS issue_count_last_3_months,
       COALESCE(cs.open_issue_count, 0)                            AS open_issue_count,
       COALESCE(cs.cr_count, 0)                                    AS cr_count,
       COALESCE(cs.cr_count_last_3_months, 0)                      AS cr_count_last_3_months,
       COALESCE(cs.open_cr_count, 0)                               AS open_cr_count,
       COALESCE(cs.contributor_count, 0)                           AS contributor_count,
       COALESCE(rs.distinct_recipient_number_last_1_month, 0)      AS distinct_recipient_number_last_1_months,
       COALESCE(rs.total_dollars_equivalent_spent_last_1_month, 0) AS total_dollars_equivalent_spent_last_1_month,
       COALESCE(bs.available_budget_usd, 0)                        AS total_dollars_equivalent_remaining_amount
FROM projects pd2
         LEFT JOIN contribution_stats cs ON cs.project_id = pd2.id
         LEFT JOIN reward_stats rs ON rs.project_id = pd2.id
         LEFT JOIN bi.p_project_budget_data bs ON bs.project_id = pd2.id
WHERE (EXISTS (SELECT 1
               FROM project_github_repos pgr2
                        JOIN indexer_exp.github_repos gr2
                             ON gr2.id = pgr2.github_repo_id AND pgr2.project_id = pd2.id AND gr2.visibility = 'PUBLIC'::indexer_exp.github_repo_visibility));

