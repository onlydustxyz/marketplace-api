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
            where p.id = :programId and
            (:search is null or element(gp).name ilike concat('%', cast(:search as String), '%'))
            """)
    Page<ProjectReadEntity> findGrantedProjects(UUID programId, String search, Pageable pageable);

    @Query("""
            select p
            from ProjectReadEntity p
            left join fetch p.globalStatsPerCurrency
            where p.id = :projectId
            """)
    Optional<ProjectReadEntity> findStatsById(UUID projectId);

    @Query("""
            select p
            from ProjectReadEntity p
            left join fetch p.globalStatsPerCurrency
            where p.slug = :projectSlug
            """)
    Optional<ProjectReadEntity> findStatsBySlug(String projectSlug);

    @Query(value = """
            SELECT distinct p
            FROM ProjectReadEntity p
            LEFT JOIN FETCH p.globalStatsPerCurrency spc
            LEFT JOIN FETCH spc.currency c
            LEFT JOIN FETCH c.latestUsdQuote
            JOIN p.leads l WITH l.userId = :leadId
            """)
    Page<ProjectReadEntity> findAllByLead(final UUID leadId, final Pageable pageable);
}
