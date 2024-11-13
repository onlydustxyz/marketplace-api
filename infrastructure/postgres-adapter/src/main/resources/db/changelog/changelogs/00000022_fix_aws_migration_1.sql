drop view project_stats_for_ranking_computation;

create view project_stats_for_ranking_computation
            (project_id, created_at, pr_count, pr_count_last_3_months, open_pr_count, issue_count,
             issue_count_last_3_months, open_issue_count, cr_count, cr_count_last_3_months, open_cr_count,
             contributor_count, distinct_recipient_number_last_1_months, usd_spent_amount, usdc_spent_amount,
             usdc_spent_amount_dollars_equivalent, op_spent_amount, op_spent_amount_dollars_equivalent,
             eth_spent_amount, eth_spent_amount_dollars_equivalent, apt_spent_amount,
             apt_spent_amount_dollars_equivalent, stark_spent_amount, stark_spent_amount_dollars_equivalent,
             lords_spent_amount, lords_spent_amount_dollars_equivalent, total_dollars_equivalent_spent_last_1_month,
             usd_remaining_amount, op_remaining_amount, stark_remaining_amount, apt_remaining_amount,
             eth_remaining_amount, lords_remaining_amount, usdc_remaining_amount,
             total_dollars_equivalent_remaining_amount)
as
WITH budget_stats AS (SELECT pa.project_id,
                             COALESCE(sum(pa.current_allowance) FILTER (WHERE c.code = 'USD'::text),
                                      0::numeric) AS usd_remaining_amount,
                             COALESCE(sum(pa.current_allowance) FILTER (WHERE c.code = 'USDC'::text),
                                      0::numeric) AS usdc_remaining_amount,
                             COALESCE(sum(pa.current_allowance * luq.price) FILTER (WHERE c.code = 'USDC'::text),
                                      0::numeric) AS usdc_dollars_equivalent_remaining_amount,
                             COALESCE(sum(pa.current_allowance) FILTER (WHERE c.code = 'OP'::text),
                                      0::numeric) AS op_remaining_amount,
                             COALESCE(sum(pa.current_allowance * luq.price) FILTER (WHERE c.code = 'OP'::text),
                                      0::numeric) AS op_dollars_equivalent_remaining_amount,
                             COALESCE(sum(pa.current_allowance) FILTER (WHERE c.code = 'STRK'::text),
                                      0::numeric) AS stark_remaining_amount,
                             COALESCE(sum(pa.current_allowance * luq.price) FILTER (WHERE c.code = 'STRK'::text),
                                      0::numeric) AS stark_dollars_equivalent_remaining_amount,
                             COALESCE(sum(pa.current_allowance) FILTER (WHERE c.code = 'APT'::text),
                                      0::numeric) AS apt_remaining_amount,
                             COALESCE(sum(pa.current_allowance * luq.price) FILTER (WHERE c.code = 'APT'::text),
                                      0::numeric) AS apt_dollars_equivalent_remaining_amount,
                             COALESCE(sum(pa.current_allowance) FILTER (WHERE c.code = 'ETH'::text),
                                      0::numeric) AS eth_remaining_amount,
                             COALESCE(sum(pa.current_allowance * luq.price) FILTER (WHERE c.code = 'ETH'::text),
                                      0::numeric) AS eth_dollars_equivalent_remaining_amount,
                             COALESCE(sum(pa.current_allowance) FILTER (WHERE c.code = 'LORDS'::text),
                                      0::numeric) AS lords_remaining_amount,
                             COALESCE(sum(pa.current_allowance * luq.price) FILTER (WHERE c.code = 'LORDS'::text),
                                      0::numeric) AS lords_dollars_equivalent_remaining_amount
                      FROM project_allowances pa
                               JOIN currencies c ON c.id = pa.currency_id
                               LEFT JOIN accounting.latest_usd_quotes luq ON luq.currency_id = pa.currency_id
                      GROUP BY pa.project_id),
     reward_stats AS (SELECT r.project_id,
                             count(DISTINCT r.recipient_id) AS distinct_recipient_number_last_1_month,
                             COALESCE(sum(r.amount) FILTER (WHERE c.code = 'USD'::text),
                                      0::numeric)           AS usd_spent_amount_last_1_month,
                             COALESCE(sum(r.amount) FILTER (WHERE c.code = 'USDC'::text),
                                      0::numeric)           AS usdc_spent_amount_last_1_month,
                             COALESCE(sum(r.amount * luq.price) FILTER (WHERE c.code = 'USDC'::text),
                                      0::numeric)           AS usdc_spent_amount_dollars_equivalent_last_1_month,
                             COALESCE(sum(r.amount) FILTER (WHERE c.code = 'OP'::text),
                                      0::numeric)           AS op_spent_amount_last_1_month,
                             COALESCE(sum(r.amount * luq.price) FILTER (WHERE c.code = 'OP'::text),
                                      0::numeric)           AS op_spent_amount_dollars_equivalent_last_1_month,
                             COALESCE(sum(r.amount) FILTER (WHERE c.code = 'ETH'::text),
                                      0::numeric)           AS eth_spent_amount_last_1_month,
                             COALESCE(sum(r.amount * luq.price) FILTER (WHERE c.code = 'ETH'::text),
                                      0::numeric)           AS eth_spent_amount_dollars_equivalent_last_1_month,
                             COALESCE(sum(r.amount) FILTER (WHERE c.code = 'APT'::text),
                                      0::numeric)           AS apt_spent_amount_last_1_month,
                             COALESCE(sum(r.amount * luq.price) FILTER (WHERE c.code = 'APT'::text),
                                      0::numeric)           AS apt_spent_amount_dollars_equivalent_last_1_month,
                             COALESCE(sum(r.amount) FILTER (WHERE c.code = 'STRK'::text),
                                      0::numeric)           AS stark_spent_amount_last_1_month,
                             COALESCE(sum(r.amount * luq.price) FILTER (WHERE c.code = 'STRK'::text),
                                      0::numeric)           AS stark_spent_amount_dollars_equivalent_last_1_month,
                             COALESCE(sum(r.amount) FILTER (WHERE c.code = 'LORDS'::text),
                                      0::numeric)           AS lords_spent_amount_last_1_month,
                             COALESCE(sum(r.amount * luq.price) FILTER (WHERE c.code = 'LORDS'::text),
                                      0::numeric)           AS lords_spent_amount_dollars_equivalent_last_1_month
                      FROM rewards r
                               JOIN currencies c ON c.id = r.currency_id
                               LEFT JOIN accounting.latest_usd_quotes luq ON luq.currency_id = r.currency_id
                      WHERE r.requested_at > (CURRENT_DATE - '1 mon'::interval)
                      GROUP BY r.project_id),
     contribution_stats AS (SELECT pgr.project_id,
                                   COALESCE(sum(1)
                                            FILTER (WHERE c.type = 'PULL_REQUEST'::indexer_exp.contribution_type),
                                            0::bigint)                                              AS pr_count,
                                   COALESCE(sum(1)
                                            FILTER (WHERE c.type = 'PULL_REQUEST'::indexer_exp.contribution_type AND
                                                          c.created_at > (CURRENT_DATE - '3 mons'::interval)),
                                            0::bigint)                                              AS pr_count_last_3_months,
                                   COALESCE(sum(1)
                                            FILTER (WHERE c.type = 'PULL_REQUEST'::indexer_exp.contribution_type AND
                                                          c.status = 'IN_PROGRESS'::indexer_exp.contribution_status),
                                            0::bigint)                                              AS open_pr_count,
                                   COALESCE(sum(1) FILTER (WHERE c.type = 'ISSUE'::indexer_exp.contribution_type),
                                            0::bigint)                                              AS issue_count,
                                   COALESCE(sum(1) FILTER (WHERE c.type = 'ISSUE'::indexer_exp.contribution_type AND
                                                                 c.created_at > (CURRENT_DATE - '3 mons'::interval)),
                                            0::bigint)                                              AS issue_count_last_3_months,
                                   COALESCE(sum(1) FILTER (WHERE c.type = 'ISSUE'::indexer_exp.contribution_type AND
                                                                 c.status =
                                                                 'IN_PROGRESS'::indexer_exp.contribution_status),
                                            0::bigint)                                              AS open_issue_count,
                                   COALESCE(sum(1) FILTER (WHERE c.type = 'CODE_REVIEW'::indexer_exp.contribution_type),
                                            0::bigint)                                              AS cr_count,
                                   COALESCE(sum(1)
                                            FILTER (WHERE c.type = 'CODE_REVIEW'::indexer_exp.contribution_type AND
                                                          c.created_at > (CURRENT_DATE - '3 mons'::interval)),
                                            0::bigint)                                              AS cr_count_last_3_months,
                                   COALESCE(sum(1)
                                            FILTER (WHERE c.type = 'CODE_REVIEW'::indexer_exp.contribution_type AND
                                                          c.status = 'IN_PROGRESS'::indexer_exp.contribution_status),
                                            0::bigint)                                              AS open_cr_count,
                                   count(DISTINCT c.contributor_id)
                                   FILTER (WHERE c.created_at > (CURRENT_DATE - '1 mon'::interval)) AS contributor_count
                            FROM project_github_repos pgr
                                     JOIN indexer_exp.github_repos gr ON pgr.github_repo_id = gr.id AND gr.visibility =
                                                                                                        'PUBLIC'::indexer_exp.github_repo_visibility
                                     JOIN indexer_exp.contributions c ON c.repo_id = gr.id
                            GROUP BY pgr.project_id)
SELECT pd2.id                                                         AS project_id,
       pd2.created_at,
       COALESCE(cs.pr_count, 0::bigint)                               AS pr_count,
       COALESCE(cs.pr_count_last_3_months, 0::bigint)                 AS pr_count_last_3_months,
       COALESCE(cs.open_pr_count, 0::bigint)                          AS open_pr_count,
       COALESCE(cs.issue_count, 0::bigint)                            AS issue_count,
       COALESCE(cs.issue_count_last_3_months, 0::bigint)              AS issue_count_last_3_months,
       COALESCE(cs.open_issue_count, 0::bigint)                       AS open_issue_count,
       COALESCE(cs.cr_count, 0::bigint)                               AS cr_count,
       COALESCE(cs.cr_count_last_3_months, 0::bigint)                 AS cr_count_last_3_months,
       COALESCE(cs.open_cr_count, 0::bigint)                          AS open_cr_count,
       COALESCE(cs.contributor_count, 0::bigint)                      AS contributor_count,
       COALESCE(rs.distinct_recipient_number_last_1_month, 0::bigint) AS distinct_recipient_number_last_1_months,
       COALESCE(rs.usd_spent_amount_last_1_month, 0::numeric)         AS usd_spent_amount,
       COALESCE(rs.usdc_spent_amount_last_1_month, 0::numeric)        AS usdc_spent_amount,
       COALESCE(rs.usdc_spent_amount_dollars_equivalent_last_1_month,
                0::numeric)                                           AS usdc_spent_amount_dollars_equivalent,
       COALESCE(rs.op_spent_amount_last_1_month, 0::numeric)          AS op_spent_amount,
       COALESCE(rs.op_spent_amount_dollars_equivalent_last_1_month,
                0::numeric)                                           AS op_spent_amount_dollars_equivalent,
       COALESCE(rs.eth_spent_amount_last_1_month, 0::numeric)         AS eth_spent_amount,
       COALESCE(rs.eth_spent_amount_dollars_equivalent_last_1_month,
                0::numeric)                                           AS eth_spent_amount_dollars_equivalent,
       COALESCE(rs.apt_spent_amount_last_1_month, 0::numeric)         AS apt_spent_amount,
       COALESCE(rs.apt_spent_amount_dollars_equivalent_last_1_month,
                0::numeric)                                           AS apt_spent_amount_dollars_equivalent,
       COALESCE(rs.stark_spent_amount_last_1_month, 0::numeric)       AS stark_spent_amount,
       COALESCE(rs.stark_spent_amount_dollars_equivalent_last_1_month,
                0::numeric)                                           AS stark_spent_amount_dollars_equivalent,
       COALESCE(rs.lords_spent_amount_last_1_month, 0::numeric)       AS lords_spent_amount,
       COALESCE(rs.lords_spent_amount_dollars_equivalent_last_1_month,
                0::numeric)                                           AS lords_spent_amount_dollars_equivalent,
       COALESCE(rs.stark_spent_amount_dollars_equivalent_last_1_month +
                rs.op_spent_amount_dollars_equivalent_last_1_month +
                rs.lords_spent_amount_dollars_equivalent_last_1_month +
                rs.apt_spent_amount_dollars_equivalent_last_1_month + rs.usd_spent_amount_last_1_month +
                rs.usdc_spent_amount_dollars_equivalent_last_1_month +
                rs.eth_spent_amount_dollars_equivalent_last_1_month,
                0::numeric)                                           AS total_dollars_equivalent_spent_last_1_month,
       COALESCE(bs.usd_remaining_amount, 0::numeric)                  AS usd_remaining_amount,
       COALESCE(bs.op_remaining_amount, 0::numeric)                   AS op_remaining_amount,
       COALESCE(bs.stark_remaining_amount, 0::numeric)                AS stark_remaining_amount,
       COALESCE(bs.apt_remaining_amount, 0::numeric)                  AS apt_remaining_amount,
       COALESCE(bs.eth_remaining_amount, 0::numeric)                  AS eth_remaining_amount,
       COALESCE(bs.lords_remaining_amount, 0::numeric)                AS lords_remaining_amount,
       COALESCE(bs.usd_remaining_amount, 0::numeric)                  AS usdc_remaining_amount,
       COALESCE(bs.apt_dollars_equivalent_remaining_amount + bs.usd_remaining_amount +
                bs.usdc_dollars_equivalent_remaining_amount + bs.op_dollars_equivalent_remaining_amount +
                bs.eth_dollars_equivalent_remaining_amount + bs.lords_dollars_equivalent_remaining_amount +
                bs.stark_dollars_equivalent_remaining_amount,
                0::numeric)                                           AS total_dollars_equivalent_remaining_amount
FROM projects pd2
         LEFT JOIN contribution_stats cs ON cs.project_id = pd2.id
         LEFT JOIN reward_stats rs ON rs.project_id = pd2.id
         LEFT JOIN budget_stats bs ON bs.project_id = pd2.id
WHERE (EXISTS (SELECT 1
               FROM project_github_repos pgr2
                        JOIN indexer_exp.github_repos gr2
                             ON gr2.id = pgr2.github_repo_id AND pgr2.project_id = pd2.id AND
                                gr2.visibility = 'PUBLIC'::indexer_exp.github_repo_visibility));