package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.accounting.DepositReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface DepositReadRepository extends Repository<DepositReadEntity, UUID> {
    @Query("""
            SELECT d
            FROM DepositReadEntity d
            WHERE d.sponsor.id = :sponsorId AND d.status != 'DRAFT'
            """)
    Page<DepositReadEntity> findAllBySponsorId(UUID sponsorId, Pageable pageable);

    Optional<DepositReadEntity> findById(UUID depositId);

    @Query("""
            SELECT d
            FROM DepositReadEntity d
            JOIN d.transaction t
            WHERE d.sponsor.id = :sponsorId AND
                  d.status != 'DRAFT'
            ORDER BY t.timestamp DESC
            LIMIT 1
            """)
    Optional<DepositReadEntity> findBySponsorIdOrderByTimestampDesc(UUID sponsorId);
}
