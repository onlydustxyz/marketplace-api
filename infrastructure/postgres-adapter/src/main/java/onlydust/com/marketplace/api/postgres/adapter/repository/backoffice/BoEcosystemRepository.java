package onlydust.com.marketplace.api.postgres.adapter.repository.backoffice;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.backoffice.BoEcosystemQueryEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BoEcosystemRepository extends JpaRepository<BoEcosystemQueryEntity, UUID> {

    @Query(value = """
            SELECT
            	s.id,
            	s.name,
            	s.url,
            	s.logo_url,
            	projects.project_ids
            FROM
            	ecosystems s
            	JOIN (
            	    SELECT 
            	        ecosystem_id,
            	        jsonb_agg(project_id) as project_ids
                    FROM
                        projects_ecosystems
                    WHERE
                        COALESCE(:projectIds) IS NULL OR project_id IN (:projectIds)
                    GROUP BY 
                        ecosystem_id
            	) projects on (projects.ecosystem_id = s.id)
             WHERE
                COALESCE(:ecosystemIds) IS NULL OR s.id IN (:ecosystemIds)
            """, nativeQuery = true)
    @NotNull
    Page<BoEcosystemQueryEntity> findAll(final List<UUID> projectIds, final List<UUID> ecosystemIds, final @NotNull Pageable pageable);
}
