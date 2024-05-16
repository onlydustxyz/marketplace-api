package onlydust.com.marketplace.api.postgres.adapter.repository.backoffice;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.backoffice.BoProjectQueryEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BoProjectRepository extends JpaRepository<BoProjectQueryEntity, UUID> {

    @Query(value = """
            SELECT p.id,
                   p.name,
                   p.short_description,
                   p.long_description,
                   more_infos.urls                             AS more_info_links,
                   p.logo_url,
                   p.hiring,
                   p.rank,
                   p.visibility,
                   leads.user_ids                              AS project_lead_ids,
                   p.created_at,
                   COALESCE(ac.count_active_contributors, 0)   AS active_contributors,
                   COALESCE(nc.count_new_contributors, 0)      AS new_contributors,
                   COALESCE(rc.count_rewarded_contributors, 0) AS unique_rewarded_contributors,
                   COALESCE(poi.open_issues, 0)                AS opened_issues,
                   COALESCE(tc.count_contributions, 0)         AS contributions,
                   COALESCE(das.total_dollars_sent, 0)         AS dollars_equivalent_amount_sent,
                   COALESCE(sat.total_strk_sent, 0)            AS strk_amount_sent
            FROM projects p
                     LEFT JOIN (SELECT project_id, jsonb_agg(user_id) user_ids
                                FROM project_leads pl
                                GROUP BY project_id) as leads ON leads.project_id = p.id
                     LEFT JOIN (SELECT project_id, jsonb_agg(url) urls
                                FROM project_more_infos pmi
                                GROUP BY project_id) as more_infos ON more_infos.project_id = p.id
                     LEFT JOIN (SELECT project_id,
                                       COUNT(DISTINCT c.contributor_id) AS count_active_contributors
                                FROM indexer_exp.contributions c
                                         INNER JOIN projects_contributors pc ON c.contributor_id = pc.github_user_id
                                WHERE c.created_at > CURRENT_DATE - INTERVAL '3 months'
                                GROUP BY project_id) ac on ac.project_id = p.id
                     LEFT JOIN (with contributions_stats as (
                                    select contributor_id, project_id,
                                           count(distinct id) filter ( where completed_at < CURRENT_DATE - INTERVAL '3 months' )  old_contributions_count,
                                           count(distinct id) filter ( where completed_at >= CURRENT_DATE - INTERVAL '3 months' ) new_contributions_count
                                    from indexer_exp.contributions c
                                    join project_github_repos pgr on pgr.github_repo_id = c.repo_id
                                    where status = 'COMPLETED'
                                    group by contributor_id, project_id
                                )
                                  select project_id, count(contributor_id) as count_new_contributors
                                  from contributions_stats
                                  where old_contributions_count = 0 and new_contributions_count > 0
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
                                       SUM(rsd.amount_usd_equivalent) AS total_dollars_sent
                                FROM rewards r
                                JOIN accounting.reward_status_data rsd on r.id = rsd.reward_id
                                JOIN currencies c on r.currency_id = c.id
                                WHERE r.requested_at > CURRENT_DATE - INTERVAL '3 months'
                                  and c.code != 'STRK'
                                GROUP BY r.project_id) das ON p.id = das.project_id
                     LEFT JOIN (SELECT r.project_id,
                                       SUM(r.amount) AS total_strk_sent
                                FROM rewards r
                                JOIN currencies c on r.currency_id = c.id
                                WHERE r.requested_at > CURRENT_DATE - INTERVAL '3 months'
                                  and c.code = 'STRK'
                                GROUP BY r.project_id) sat ON p.id = sat.project_id
            WHERE (COALESCE(:projectIds) IS NULL OR p.id IN (:projectIds))
            ORDER BY p.name
            """, nativeQuery = true)
    @NotNull
    Page<BoProjectQueryEntity> findAll(final List<UUID> projectIds, final @NotNull Pageable pageable);
}
