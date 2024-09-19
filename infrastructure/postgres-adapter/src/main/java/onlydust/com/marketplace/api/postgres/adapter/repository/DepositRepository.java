package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.DepositEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface DepositRepository extends JpaRepository<DepositEntity, UUID> {
    @Query("""
            SELECT d
            FROM DepositEntity d
            JOIN FETCH d.transaction t
            WHERE t.reference = :transactionReference
            """)
    Optional<DepositEntity> findByTransactionReference(@NonNull String transactionReference);
}
