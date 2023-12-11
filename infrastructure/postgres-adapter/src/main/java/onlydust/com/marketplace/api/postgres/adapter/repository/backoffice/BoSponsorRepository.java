package onlydust.com.marketplace.api.postgres.adapter.repository.backoffice;

import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.BoPaymentEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.BoSponsorEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BoSponsorRepository extends JpaRepository<BoSponsorEntity, UUID> {

    @Query(value = """
            SELECT
            	s.id,
            	s.name,
            	s.url,
            	s.logo_url,
            	projects.project_ids
            FROM
            	sponsors s
            	JOIN (
            	    SELECT 
            	        sponsor_id,
            	        jsonb_agg(project_id) as project_ids
                    FROM
                        projects_sponsors
                    WHERE
                        COALESCE(:projectIds) IS NULL OR project_id IN (:projectIds)
                    GROUP BY 
                        sponsor_id
            	) projects on (projects.sponsor_id = s.id)
            """, nativeQuery = true)
    @NotNull
    Page<BoSponsorEntity> findAll(final List<UUID> projectIds, final @NotNull Pageable pageable);
}
