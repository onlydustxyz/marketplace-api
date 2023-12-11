package onlydust.com.marketplace.api.postgres.adapter.repository.backoffice;

import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.BoPaymentEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.BoProjectEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface BoProjectRepository extends JpaRepository<BoProjectEntity, UUID> {

    @Query(value = """
            SELECT p.project_id as id,
                   p.name,
                   p.short_description,
                   p.long_description,
                   more_infos.urls AS more_info_links,
                   p.logo_url,
                   p.hiring,
                   p.rank,
                   p.visibility,
                   leads.user_ids AS project_lead_ids,
                   p.created_at
            FROM project_details p
                     LEFT JOIN (SELECT project_id, jsonb_agg(user_id) user_ids
                                FROM project_leads pl
                                GROUP BY project_id) as leads ON leads.project_id = p.project_id
                     LEFT JOIN (SELECT project_id, jsonb_agg(url) urls
                                FROM project_more_infos pmi
                                GROUP BY project_id) as more_infos ON more_infos.project_id = p.project_id
            """, nativeQuery = true)
    @NotNull
    Page<BoProjectEntity> findAll(final @NotNull Pageable pageable);
}
