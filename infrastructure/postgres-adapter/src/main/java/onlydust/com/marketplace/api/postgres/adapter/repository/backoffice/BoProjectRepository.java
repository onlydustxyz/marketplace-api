package onlydust.com.marketplace.api.postgres.adapter.repository.backoffice;

import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.BoProjectEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BoProjectRepository extends JpaRepository<BoProjectEntity, UUID> {

    @Query(value = """
            SELECT p.project_id                                as id,
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
            FROM project_details p
                     LEFT JOIN (SELECT project_id, jsonb_agg(user_id) user_ids
                                FROM project_leads pl
                                GROUP BY project_id) as leads ON leads.project_id = p.project_id
                     LEFT JOIN (SELECT project_id, jsonb_agg(url) urls
                                FROM project_more_infos pmi
                                GROUP BY project_id) as more_infos ON more_infos.project_id = p.project_id
                     LEFT JOIN (SELECT project_id,
                                       COUNT(DISTINCT c.contributor_id) AS count_active_contributors
                                FROM indexer_exp.contributions c
                                         INNER JOIN projects_contributors pc ON c.contributor_id = pc.github_user_id
                                WHERE c.created_at > CURRENT_DATE - INTERVAL '3 months'
                                GROUP BY project_id) ac on ac.project_id = p.project_id
                     LEFT JOIN (SELECT pc.project_id,
                                       COUNT(DISTINCT c.contributor_id) AS count_new_contributors
                                FROM indexer_exp.contributions c
                                         INNER JOIN projects_contributors pc ON c.contributor_id = pc.github_user_id
                                WHERE c.created_at > CURRENT_DATE - INTERVAL '3 months'
                                  AND NOT EXISTS (SELECT 1
                                                  FROM indexer_exp.contributions c2
                                                  WHERE c2.contributor_id = c.contributor_id
                                                    AND c2.created_at < CURRENT_DATE - INTERVAL '3 months')
                                GROUP BY pc.project_id) nc ON p.project_id = nc.project_id
                     LEFT JOIN (SELECT pr.project_id,
                                       COUNT(DISTINCT pr.recipient_id) AS count_rewarded_contributors
                                FROM payment_requests pr
                                WHERE pr.requested_at > CURRENT_DATE - INTERVAL '3 months'
                                GROUP BY pr.project_id) rc ON p.project_id = rc.project_id
                     LEFT JOIN (SELECT pgr.project_id,
                                       COALESCE(COUNT(*), 0) AS open_issues
                                FROM indexer_exp.github_issues gi
                                         LEFT JOIN project_github_repos pgr on pgr.github_repo_id = gi.repo_id
                                WHERE gi.status = 'OPEN'
                                  AND gi.created_at > CURRENT_DATE - INTERVAL '3 months'
                                GROUP BY pgr.project_id) poi ON p.project_id = poi.project_id
                     LEFT JOIN (SELECT pc.project_id,
                                       COUNT(c.id) AS count_contributions
                                FROM indexer_exp.contributions c
                                         INNER JOIN
                                     projects_contributors pc ON c.contributor_id = pc.github_user_id
                                WHERE c.created_at > CURRENT_DATE - INTERVAL '3 months'
                                GROUP BY pc.project_id) tc ON p.project_id = tc.project_id
                     LEFT JOIN (SELECT pr.project_id,
                                       SUM(pr.amount * COALESCE(cuq.price, 1)) AS total_dollars_sent
                                FROM payment_requests pr
                                         LEFT JOIN crypto_usd_quotes cuq ON pr.currency = cuq.currency
                                WHERE pr.requested_at > CURRENT_DATE - INTERVAL '3 months'
                                  and pr.currency != 'strk'
                                GROUP BY pr.project_id) das ON p.project_id = das.project_id
                     LEFT JOIN (SELECT pr.project_id,
                                       SUM(pr.amount) AS total_strk_sent
                                FROM payment_requests pr
                                WHERE pr.requested_at > CURRENT_DATE - INTERVAL '3 months'
                                  and pr.currency = 'strk'
                                GROUP BY pr.project_id) sat ON p.project_id = sat.project_id
            WHERE (COALESCE(:projectIds) IS NULL OR p.project_id IN (:projectIds))
            ORDER BY p.name
            """, nativeQuery = true)
    @NotNull
    Page<BoProjectEntity> findAll(final List<UUID> projectIds, final @NotNull Pageable pageable);
}
