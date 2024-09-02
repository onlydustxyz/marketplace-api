package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.TransferTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransferTransactionRepository extends JpaRepository<TransferTransactionEntity, UUID> {
    boolean existsByReference(String transactionReference);
}
