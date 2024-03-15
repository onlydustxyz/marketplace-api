package onlydust.com.marketplace.api.postgres.adapter.repository.backoffice;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.BatchPaymentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;
import java.util.UUID;

public interface BatchPaymentRepository extends JpaRepository<BatchPaymentEntity, UUID> {
    Page<BatchPaymentEntity> findAllByStatusIsIn(Set<BatchPaymentEntity.Status> statuses, Pageable pageable);
}
