package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProgramLeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProgramLeadRepository extends JpaRepository<ProgramLeadEntity, ProgramLeadEntity.PrimaryKey> {
    @Query(value = """
            select pl.*
            from program_leads pl
            join accounting.all_transactions abt on pl.program_id = abt.program_id
            where pl.user_id = :userId
              and abt.project_id = :projectId
            """, nativeQuery = true)
    Optional<ProgramLeadEntity> findByProjectId(UUID userId, UUID projectId);

    @Query(value = """
            select pl.*
            from program_leads pl
            join accounting.all_transactions abt on pl.program_id = abt.program_id
            join projects p on abt.project_id = p.id
            where pl.user_id = :userId
              and p.slug = :projectSlug
            """, nativeQuery = true)
    Optional<ProgramLeadEntity> findByProjectSlug(UUID userId, String projectSlug);

    List<ProgramLeadEntity> findByProgramId(UUID programId);

    List<ProgramLeadEntity> findByUserId(UUID userId);
}
