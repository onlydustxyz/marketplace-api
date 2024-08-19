package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.SumsubRejectionReasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SumsubRejectionReasonRepository extends JpaRepository<SumsubRejectionReasonEntity, UUID> {

    Optional<SumsubRejectionReasonEntity> findByGroupIdAndButtonIdAndAssociatedRejectionLabel(String groupId, String buttonId, String rejectionLabel);
}
