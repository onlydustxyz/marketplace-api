create or replace view project_tag_scores as
with hot_community as (SELECT pgr.project_id,
                              COUNT(DISTINCT c.contributor_id) AS active_contributors_count
                       FROM indexer_exp.contributions c
                                JOIN
                            project_github_repos pgr ON c.repo_id = pgr.github_repo_id
                                LEFT JOIN
                            project_details pd ON pgr.project_id = pd.project_id
                       WHERE c.created_at > CURRENT_DATE - INTERVAL '4 weeks'
                       GROUP BY pgr.project_id, pd.name
                       HAVING COUNT(DISTINCT c.contributor_id) > 10
                       ORDER BY active_contributors_count DESC),
     newbies_welcome as (WITH first_contribution AS (SELECT c.contributor_id,
                                                            pgr.project_id,
                                                            MIN(c.created_at) AS first_contribution_date
                                                     FROM indexer_exp.contributions c
                                                              JOIN
                                                          project_github_repos pgr ON c.repo_id = pgr.github_repo_id
                                                     GROUP BY c.contributor_id, pgr.project_id)
                         SELECT fc.project_id,
                                pd.name                  AS project_name,
                                COUNT(fc.contributor_id) AS new_contributors_last_4_weeks
                         FROM first_contribution fc
                                  LEFT JOIN
                              project_details pd ON fc.project_id = pd.project_id
                         WHERE fc.first_contribution_date > CURRENT_DATE - INTERVAL '4 weeks'
                         GROUP BY fc.project_id, pd.name
                         HAVING COUNT(fc.contributor_id) > 5
                         ORDER BY new_contributors_last_4_weeks DESC),
     likely_to_reward as (SELECT pgr.project_id,
                                 COUNT(DISTINCT pr.recipient_id) AS recipients_last_month
                          FROM project_github_repos pgr
                                   JOIN
                               project_details pd ON pgr.project_id = pd.project_id
                                   JOIN
                               payment_requests pr ON pgr.project_id = pr.project_id
                          WHERE pr.requested_at > CURRENT_DATE - INTERVAL '1 month'
                          GROUP BY pgr.project_id, pd.name
                          HAVING COUNT(DISTINCT pr.recipient_id) > 3
                          ORDER BY recipients_last_month DESC),
     work_in_progress as (SELECT pgr.project_id,
                                 COUNT(gi.id) AS issues_count
                          FROM project_github_repos pgr
                                   LEFT JOIN
                               project_details pd ON pgr.project_id = pd.project_id
                                   JOIN
                               indexer_exp.github_issues gi ON pgr.github_repo_id = gi.repo_id
                          WHERE gi.status = 'OPEN'
                            AND gi.created_at > CURRENT_DATE - INTERVAL '4 weeks'
                            AND LENGTH(gi.body) > 500
                            AND NOT EXISTS (SELECT 1
                                            FROM indexer_exp.github_issues_assignees gia
                                            WHERE gia.issue_id = gi.id)
                          GROUP BY pgr.project_id, pd.name
                          ORDER BY issues_count DESC),
     fast_and_furious as (SELECT pgr.project_id,
                                 COUNT(c.id) AS contributions_last_4_weeks
                          FROM indexer_exp.contributions c
                                   JOIN
                               project_github_repos pgr ON c.repo_id = pgr.github_repo_id
                                   LEFT JOIN
                               project_details pd ON pgr.project_id = pd.project_id
                          WHERE c.created_at > CURRENT_DATE - INTERVAL '4 weeks'
                          GROUP BY pgr.project_id, pd.name
                          ORDER BY contributions_last_4_weeks DESC),
     big_whale as (SELECT pr.project_id,
                          SUM(
                                  CASE
                                      WHEN pr.currency = 'usd' THEN pr.amount
                                      ELSE pr.amount * COALESCE(cuq.price, 0)
                                      END
                          ) AS total_rewards_in_usd
                   FROM payment_requests pr
                            LEFT JOIN
                        project_details pd ON pr.project_id = pd.project_id
                            LEFT JOIN
                        crypto_usd_quotes cuq ON pr.currency = cuq.currency
                   WHERE pr.requested_at > CURRENT_DATE - INTERVAL '3 months'
                   GROUP BY pr.project_id, pd.name
                   HAVING SUM(
                                  CASE
                                      WHEN pr.currency = 'usd' THEN pr.amount
                                      ELSE pr.amount * COALESCE(cuq.price, 0)
                                      END
                          ) > 5000
                   ORDER BY total_rewards_in_usd DESC)
select all_p.project_id,
       coalesce(hc.active_contributors_count, 0)     hot_community_score,
       coalesce(nw.new_contributors_last_4_weeks, 0) newbies_welcome_score,
       coalesce(ltr.recipients_last_month, 0)        likely_to_reward_score,
       coalesce(wip.issues_count, 0)                 work_in_progress_score,
       coalesce(faf.contributions_last_4_weeks, 0)   fast_and_furious_score,
       coalesce(bw.total_rewards_in_usd, 0)          big_whale_score
from project_details all_p
         left join hot_community hc on hc.project_id = all_p.project_id
         left join newbies_welcome nw on nw.project_id = all_p.project_id
         left join likely_to_reward ltr on ltr.project_id = all_p.project_id
         left join work_in_progress wip on wip.project_id = all_p.project_id
         left join fast_and_furious faf on faf.project_id = all_p.project_id
         left join big_whale bw on bw.project_id = all_p.project_id;