package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.project.ProjectStatReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface ProjectStatsReadRepository extends Repository<ProjectStatReadEntity, UUID> {
    @Query(value = """
            select distinct ps.*
            from bi.project_stats ps
            join bi.program_stats_per_currency_per_project pspcp on
                ps.project_id = pspcp.project_id and
                pspcp.total_granted > 0 and
                pspcp.program_id = :programId
            """, nativeQuery = true)
    Page<ProjectStatReadEntity> findGrantedProject(UUID programId, Pageable pageable);

    Optional<ProjectStatReadEntity> findByProjectId(UUID projectId);
}
