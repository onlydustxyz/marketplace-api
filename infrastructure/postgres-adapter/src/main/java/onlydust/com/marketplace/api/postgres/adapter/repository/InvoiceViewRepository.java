package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.InvoiceViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvoiceViewRepository extends JpaRepository<InvoiceViewEntity, UUID> {
    List<InvoiceViewEntity> findAllByBillingProfileIdAndStatus(final @NonNull UUID billingProfileId, final @NonNull InvoiceEntity.Status status);

    Page<InvoiceViewEntity> findAllByBillingProfileIdAndStatusNot(final @NonNull UUID billingProfileId, final @NonNull InvoiceEntity.Status exceptStatus,
                                                                  final @NonNull Pageable pageable);
}
