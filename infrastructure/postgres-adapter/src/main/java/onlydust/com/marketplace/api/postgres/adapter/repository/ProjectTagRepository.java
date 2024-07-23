package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;

public interface ProjectTagRepository extends JpaRepository<ProjectTagEntity, ProjectTagEntity.Id> {

    @Modifying
    @Query(value = """
            with hot_community as (SELECT pgr.project_id,
                                          COUNT(DISTINCT c.contributor_id) AS active_contributors_count
                                   FROM indexer_exp.contributions c
                                            JOIN
                                        project_github_repos pgr ON c.repo_id = pgr.github_repo_id
                                            LEFT JOIN
                                        projects p ON pgr.project_id = p.id
                                   WHERE c.created_at > cast(:now as timestamp)   - INTERVAL '4 weeks'
                                   GROUP BY pgr.project_id, p.name
                                   HAVING COUNT(DISTINCT c.contributor_id) >= 10
                                   ORDER BY active_contributors_count DESC)
            insert into projects_tags (project_id, tag)
            select hc.project_id, 'HOT_COMMUNITY'
            from hot_community hc
            """, nativeQuery = true)
    void updateHotCommunityTag(@Param("now") Date now);

    @Modifying
    @Query(value = """
            with newbies_welcome as (WITH first_contribution AS (SELECT c.contributor_id,
                                                                         pgr.project_id,
                                                                         MIN(c.created_at) AS first_contribution_date
                                                                  FROM indexer_exp.contributions c
                                                                           JOIN
                                                                       project_github_repos pgr ON c.repo_id = pgr.github_repo_id
                                                                  GROUP BY c.contributor_id, pgr.project_id)
                                      SELECT fc.project_id,
                                             p.name                  AS project_name,
                                             COUNT(fc.contributor_id) AS new_contributors_last_4_weeks
                                      FROM first_contribution fc
                                               LEFT JOIN
                                           projects p ON fc.project_id = p.id
                                      WHERE fc.first_contribution_date > cast(:now as timestamp) - INTERVAL '4 weeks'
                                      GROUP BY fc.project_id, p.name
                                      HAVING COUNT(fc.contributor_id) >= 5
                                      ORDER BY new_contributors_last_4_weeks DESC)
             insert into projects_tags (project_id, tag)
             select nw.project_id, 'NEWBIES_WELCOME'
             from newbies_welcome nw
            """, nativeQuery = true)
    void updateNewbiesWelcome(@Param("now") Date now);

    @Modifying
    @Query(value = """
            with likely_to_reward as (SELECT pgr.project_id,
                                       COUNT(DISTINCT r.recipient_id) AS recipients_last_month
                                FROM project_github_repos pgr
                                         JOIN
                                     projects p ON pgr.project_id = p.id
                                         JOIN
                                     rewards r ON pgr.project_id = r.project_id
                                WHERE r.requested_at > cast(:now as timestamp) - INTERVAL '1 month'
                                GROUP BY pgr.project_id, p.name
                                HAVING COUNT(DISTINCT r.recipient_id) >= 3
                                ORDER BY recipients_last_month DESC)
             insert into projects_tags (project_id, tag)
             select nw.project_id, 'LIKELY_TO_REWARD'
             from likely_to_reward nw
            """, nativeQuery = true)
    void updateLikelyToReward(@Param("now") Date now);

    @Modifying
    @Query(value = """
            with work_in_progress as (SELECT pgr.project_id,
                                             COUNT(gi.id) AS issues_count
                                      FROM project_github_repos pgr
                                               LEFT JOIN
                                           projects p ON pgr.project_id = p.id
                                               JOIN
                                           indexer_exp.github_issues gi ON pgr.github_repo_id = gi.repo_id
                                      WHERE gi.status = 'OPEN'
                                        AND gi.created_at > cast(:now as timestamp) - INTERVAL '4 weeks'
                                        AND LENGTH(gi.body) > 500
                                        AND NOT EXISTS (SELECT 1
                                                        FROM indexer_exp.github_issues_assignees gia
                                                        WHERE gia.issue_id = gi.id)
                                      GROUP BY pgr.project_id, p.name
                                      HAVING count(gi.id) >= 3
                                      ORDER BY issues_count DESC)
             insert into projects_tags (project_id, tag)
             select wip.project_id, 'WORK_IN_PROGRESS'
             from work_in_progress wip
            """, nativeQuery = true)
    void updateWorkInProgress(@Param("now") Date now);

    @Modifying
    @Query(value = """
            with fast_and_furious as (SELECT pgr.project_id,
                                              COUNT(c.id) AS contributions_last_4_weeks
                                       FROM indexer_exp.contributions c
                                                JOIN
                                            project_github_repos pgr ON c.repo_id = pgr.github_repo_id
                                                LEFT JOIN
                                            projects p ON pgr.project_id = p.id
                                       WHERE c.created_at > cast(:now as timestamp) - INTERVAL '4 weeks'
                                       GROUP BY pgr.project_id, p.name
                                       HAVING COUNT(c.id) >= 80
                                       ORDER BY contributions_last_4_weeks DESC)
             insert into projects_tags (project_id, tag)
             select faf.project_id, 'FAST_AND_FURIOUS'
             from fast_and_furious faf
            """, nativeQuery = true)
    void updateFastAndFurious(@Param("now") Date now);

    @Modifying
    @Query(value = """
            with big_whale as (SELECT r.project_id,
                                      SUM(rsd.amount_usd_equivalent) AS total_rewards_in_usd
                               FROM rewards r
                               JOIN accounting.reward_status_data rsd ON r.id = rsd.reward_id
                                        LEFT JOIN
                                    projects p ON r.project_id = p.id
                               WHERE r.requested_at > cast(:now as timestamp) - INTERVAL '3 months'
                               GROUP BY r.project_id, p.name
                               HAVING SUM(rsd.amount_usd_equivalent) > 5000
                               ORDER BY total_rewards_in_usd DESC)
             insert into projects_tags (project_id, tag)
             select bw.project_id, 'BIG_WHALE'
             from big_whale bw
            """, nativeQuery = true)
    void updateBigWhale(@Param("now") Date now);

    @Modifying
    @Query(value = """
            insert into projects_tags (project_id, tag)
            select distinct pgfi.project_id, cast('HAS_GOOD_FIRST_ISSUES' as project_tag)
            from projects_good_first_issues pgfi
            """, nativeQuery = true)
    void updateHasGoodFirstIssues();

}
