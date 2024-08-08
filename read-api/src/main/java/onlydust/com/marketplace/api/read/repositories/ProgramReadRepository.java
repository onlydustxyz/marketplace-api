package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.program.sponsor.ProgramReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface ProgramReadRepository extends Repository<ProgramReadEntity, UUID> {
    @Query(value = """
            SELECT s.*
            FROM sponsors s
            JOIN sponsors_users su ON s.id = su.sponsor_id AND su.user_id = :leadId
            """, nativeQuery = true)
    Page<ProgramReadEntity> findAllByLead(final UUID leadId, final Pageable pageable);
}
