package onlydust.com.marketplace.api.postgres.adapter.repository.old;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.SponsorViewEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SponsorViewRepository extends JpaRepository<SponsorViewEntity, UUID> {
    Page<SponsorViewEntity> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query(value = """
            SELECT
            	s
            FROM
            	SponsorViewEntity s
            	LEFT JOIN s.projects p
            WHERE
                (COALESCE(:sponsorIds) IS NULL OR s.id IN (:sponsorIds))
                AND (COALESCE(:projectIds) IS NULL OR p.projectId IN (:projectIds))
            GROUP BY s.id
            """)
    @NotNull
    Page<SponsorViewEntity> findAll(final List<UUID> projectIds, final List<UUID> sponsorIds, final @NotNull Pageable pageable);

}
