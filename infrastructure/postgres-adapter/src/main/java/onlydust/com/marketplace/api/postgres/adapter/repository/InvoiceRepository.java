package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<InvoiceEntity, UUID> {
    List<InvoiceEntity> findAllByBillingProfileIdAndStatus(final @NonNull UUID billingProfileId, final @NonNull InvoiceEntity.Status status);

    Page<InvoiceEntity> findAllByBillingProfileIdAndStatusNot(final @NonNull UUID billingProfileId, final @NonNull InvoiceEntity.Status exceptStatus,
                                                              final @NonNull Pageable pageable);

    Integer countByBillingProfileIdAndStatusNot(final @NonNull UUID billingProfileId, final @NonNull InvoiceEntity.Status exceptStatus);
}
