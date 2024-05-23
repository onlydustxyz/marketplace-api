package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectInfosQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ProjectInfosViewRepository extends JpaRepository<ProjectInfosQueryEntity, UUID> {

    @Query(nativeQuery = true, value = """
            select p.id,
                   p.slug,
                   p.name,
                   p.logo_url,
                   p.short_description,
                   p.long_description,
                   p.visibility,
                   project_leads.pl_json project_leads,
                   COALESCE(ac.count_active_contributors, 0)   active_contributors,
                   COALESCE(das.total_dollars_sent, 0)         amount_sent_in_usd,
                   COALESCE(rc.count_rewarded_contributors, 0) contributors_rewarded,
                   COALESCE(tc.count_contributions, 0)         contributions_completed,
                   COALESCE(nc.count_new_contributors, 0)      new_contributors,
                   COALESCE(poi.open_issues, 0)                open_issue
                   from projects p
                   LEFT JOIN LATERAL (select pl.project_id,
                                                      jsonb_agg(jsonb_build_object('id', pl.user_id,
                                                                                   'login', u.github_login,
                                                                                   'githubId', u.github_user_id,
                                                                                   'avatarUrl', u.github_avatar_url,
                                                                                   'url', null)) as pl_json
                                               from project_leads pl
                                                        join iam.users u on u.id = pl.user_id
                                               group by pl.project_id) project_leads on project_leads.project_id = p.id
                   LEFT JOIN (SELECT project_id, jsonb_agg(user_id) user_ids
                               FROM project_leads pl
                               GROUP BY project_id) as leads ON leads.project_id = p.id
                   LEFT JOIN (SELECT project_id,
                                      COUNT(DISTINCT c.contributor_id) AS count_active_contributors
                               FROM indexer_exp.contributions c
                                        INNER JOIN projects_contributors pc ON c.contributor_id = pc.github_user_id
                               WHERE c.created_at > CURRENT_DATE - INTERVAL '3 months'
                               GROUP BY project_id) ac on ac.project_id = p.id
                   LEFT JOIN (with contributions_stats as (select contributor_id,
                                                                   project_id,
                                                                   count(distinct id)
                                                                   filter ( where completed_at < CURRENT_DATE - INTERVAL '3 months' )  old_contributions_count,
                                                                   count(distinct id)
                                                                   filter ( where completed_at >= CURRENT_DATE - INTERVAL '3 months' ) new_contributions_count
                                                            from indexer_exp.contributions c
                                                                     join project_github_repos pgr on pgr.github_repo_id = c.repo_id
                                                            where status = 'COMPLETED'
                                                            group by contributor_id, project_id)
                               select project_id, count(contributor_id) as count_new_contributors
                               from contributions_stats
                               where old_contributions_count = 0
                                 and new_contributions_count > 0
                               group by project_id) nc ON p.id = nc.project_id
                   LEFT JOIN (SELECT r.project_id,
                                      COUNT(DISTINCT r.recipient_id) AS count_rewarded_contributors
                               FROM rewards r
                               WHERE r.requested_at > CURRENT_DATE - INTERVAL '3 months'
                               GROUP BY r.project_id) rc ON p.id = rc.project_id
                   LEFT JOIN (SELECT pgr.project_id,
                                      COALESCE(COUNT(*), 0) AS open_issues
                               FROM indexer_exp.github_issues gi
                                        LEFT JOIN project_github_repos pgr on pgr.github_repo_id = gi.repo_id
                               WHERE gi.status = 'OPEN'
                                 AND gi.created_at > CURRENT_DATE - INTERVAL '3 months'
                               GROUP BY pgr.project_id) poi ON p.id = poi.project_id
                   LEFT JOIN (SELECT pc.project_id,
                                      COUNT(c.id) AS count_contributions
                               FROM indexer_exp.contributions c
                                        INNER JOIN
                                    projects_contributors pc ON c.contributor_id = pc.github_user_id
                               WHERE c.created_at > CURRENT_DATE - INTERVAL '3 months'
                               GROUP BY pc.project_id) tc ON p.id = tc.project_id
                   LEFT JOIN (SELECT r.project_id,
                                      ROUND(SUM(rsd.amount_usd_equivalent), 2) AS total_dollars_sent
                               FROM rewards r
                                        JOIN accounting.reward_status_data rsd on r.id = rsd.reward_id
                                        JOIN currencies c on r.currency_id = c.id
                               WHERE r.requested_at > CURRENT_DATE - INTERVAL '3 months'
                               GROUP BY r.project_id) das ON p.id = das.project_id
            where p.id = :projectId
            """)
    ProjectInfosQueryEntity findByProjectId(UUID projectId);


    @Query(nativeQuery = true, value = """
            select p.id,
                   p.slug,
                   p.name,
                   p.logo_url,
                   p.short_description,
                   p.long_description,
                   p.visibility,
                   project_leads.pl_json project_leads,
                   0                 active_contributors,
                   0                 amount_sent_in_usd,
                   0                 contributors_rewarded,
                   0                 contributions_completed,
                   0                 new_contributors,
                   0                 open_issue
                   from projects p
                   LEFT JOIN LATERAL (select pl.project_id,
                                                      jsonb_agg(jsonb_build_object('id', pl.user_id,
                                                                                   'login', u.github_login,
                                                                                   'githubId', u.github_user_id,
                                                                                   'avatarUrl', u.github_avatar_url,
                                                                                   'url', null)) as pl_json
                                               from project_leads pl
                                                        join iam.users u on u.id = pl.user_id
                                               group by pl.project_id) project_leads on project_leads.project_id = p.id
                   LEFT JOIN (SELECT project_id, jsonb_agg(user_id) user_ids
                               FROM project_leads pl
                               GROUP BY project_id) as leads ON leads.project_id = p.id
            where p.id = :projectId
            """)
    ProjectInfosQueryEntity findByProjectIdWithoutMetrics(UUID projectId);
}
