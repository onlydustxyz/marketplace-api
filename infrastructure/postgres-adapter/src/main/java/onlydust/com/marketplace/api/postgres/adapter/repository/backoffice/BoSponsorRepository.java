package onlydust.com.marketplace.api.postgres.adapter.repository.backoffice;

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
            	s
            FROM
            	BoSponsorEntity s
            	LEFT JOIN s.projects p
            WHERE
                (COALESCE(:sponsorIds) IS NULL OR s.id IN (:sponsorIds))
                AND (COALESCE(:projectIds) IS NULL OR p.projectId IN (:projectIds))
            GROUP BY s.id
            """)
    @NotNull
    Page<BoSponsorEntity> findAll(final List<UUID> projectIds, final List<UUID> sponsorIds, final @NotNull Pageable pageable);
    
}
