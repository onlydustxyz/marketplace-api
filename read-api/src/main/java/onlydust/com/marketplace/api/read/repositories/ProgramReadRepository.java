package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.program.ProgramReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface ProgramReadRepository extends Repository<ProgramReadEntity, UUID> {
    @Query(value = """
            SELECT p
            FROM ProgramReadEntity p
            JOIN p.leads l WITH l.userId = :leadId
            """)
    Page<ProgramReadEntity> findAllByLead(final UUID leadId, final Pageable pageable);

    Optional<ProgramReadEntity> findById(UUID programId);
}
