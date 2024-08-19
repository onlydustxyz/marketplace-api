package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.project.ProjectReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface ProjectReadRepository extends Repository<ProjectReadEntity, UUID> {
    Optional<ProjectReadEntity> findById(UUID id);

    Optional<ProjectReadEntity> findBySlug(String slug);

    @Query("""
            select gp
            from ProgramReadEntity p
            join p.grantedProjects gp
            left join fetch gp.perProgramStatsPerCurrency
            left join fetch gp.contributionStats
            left join fetch gp.rewardStats
            where p.id = :programId
            """)
    Page<ProjectReadEntity> findGrantedProjects(UUID programId, Pageable pageable);

    @Query("""
            select p
            from ProjectReadEntity p
            left join fetch p.globalStatsPerCurrency
            where p.id = :projectId
            """)
    Optional<ProjectReadEntity> findStatsById(UUID projectId);
}
